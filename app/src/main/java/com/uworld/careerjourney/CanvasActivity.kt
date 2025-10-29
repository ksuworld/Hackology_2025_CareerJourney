package com.uworld.careerjourney

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.toColorInt
import com.uworld.careerjourney.dashboard.GlobalThemeManager
import androidx.navigation.compose.composable
import com.uworld.careerjourney.old.MarkerData
import com.uworld.careerjourney.old.SegmentData
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// ... (Existing Constants and Utility functions remain the same)
private const val COLOR_TOLERANCE = 70
private const val BASE_DRAG_VELOCITY = 1.0f
private const val ACCELERATION_MULTIPLIER = 1.5f


class CanvasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                val startDate = LocalDate.now().minusDays(100)
                val endDate = LocalDate.now().plusDays(100)

                InteractiveRoadmapScreenCanvas(
                    startDate = startDate,
                    endDate = endDate
                )
            }
        }
    }
}

fun getBitmapFromVectorDrawableCanvas(
    context: Context,
    @DrawableRes drawableId: Int,
    width: Int,
    height: Int
): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

// ----------------------------------------------------------------------
// 2. MAIN COMPOSABLE (Canvas Version)
// ----------------------------------------------------------------------

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveRoadmapScreenCanvas(
    startDate: LocalDate,
    endDate: LocalDate
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // STATE VARIABLES
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    var initialScale by remember { mutableFloatStateOf(1f) }
    val initialOffsetY = remember { Animatable(0f) }

    var roadHexColor by remember { mutableIntStateOf(0xFF0162BA.toInt()) }

    var isAddingMarker by remember { mutableStateOf(false) }
    var touchPoint by remember { mutableStateOf(Offset.Zero) } // In Image Pixel Coordinates!
    val markers = remember { mutableStateListOf<MarkerData>() }

    // Convert DPs for marker drawing (e.g., marker radius or half size)
    val markerHalfSizePx = with(density) { 8.dp.toPx() }
    val markerFontSizeSp = 12.sp
    val textPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { markerFontSizeSp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    // SEGMENTATION DATA (same as before)
    val segments: List<SegmentData> = remember(startDate, endDate) {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toFloat()
        if (totalDays <= 0) return@remember emptyList()
        val daysList = (0..totalDays.toInt()).map { startDate.plusDays(it.toLong()) }
        daysList.mapIndexed { index, date ->
            val startY = index / totalDays
            val endY = min(1f, (index + 1) / totalDays)
            SegmentData(date, startY, endY)
        }
    }


    // ----------------------------------------------------------------------
    // 3. DYNAMIC SVG LOADING AND INITIAL ZOOM
    // ----------------------------------------------------------------------

    LaunchedEffect(boxSize) {
        val imageResId = GlobalThemeManager.themeResIds[GlobalThemeManager.selectedThemeId]
        if (boxSize.width > 0) {
            val drawable = ContextCompat.getDrawable(context, imageResId)
            val drawableWidth = drawable?.intrinsicWidth ?: 1
            val drawableHeight = drawable?.intrinsicHeight ?: 1

            if (drawableWidth > 0 && drawableHeight > 0) {
                val aspectRatio = drawableHeight.toFloat() / drawableWidth.toFloat()
                val targetBitmapWidth = boxSize.width
                val targetBitmapHeight = (targetBitmapWidth.toFloat() * aspectRatio).toInt()

                val renderedBitmap = getBitmapFromVectorDrawableCanvas(
                    context = context,
                    drawableId = imageResId,
                    width = targetBitmapWidth,
                    height = targetBitmapHeight
                )

                if (renderedBitmap != null) {
                    originalBitmap = renderedBitmap
                    imageSize = IntSize(renderedBitmap.width, renderedBitmap.height)
                    roadHexColor = if (GlobalThemeManager.selectedThemeId == 0) {
                        "#0164BE".toColorInt()
                    } else {
                        "#F37301".toColorInt()
                    }

                    val calculatedInitialScale = 1f
                    val scaledHeight = renderedBitmap.height * calculatedInitialScale
                    val verticalCenterOffset = (boxSize.height - scaledHeight) / 2f

                    scale.snapTo(calculatedInitialScale)
                    initialScale = calculatedInitialScale
                    initialOffsetY.snapTo(verticalCenterOffset)
                    offsetY.snapTo(verticalCenterOffset)
                    offsetX.snapTo(0f)
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // 4. TOUCH & DRAG LOGIC (Transformed to Canvas context)
    // ----------------------------------------------------------------------

    val onDrag: (Offset, Float) -> Unit = { dragAmount, zoomChange ->
        // (Drag logic remains the same)
        val currentScale = scale.value * zoomChange
        val newScale = max(initialScale, currentScale)

        val scaledWidth = imageSize.width * newScale
        val scaledHeight = imageSize.height * newScale

        val dynamicVelocityFactor = BASE_DRAG_VELOCITY * ACCELERATION_MULTIPLIER

        val acceleratedDragAmount = dragAmount * dynamicVelocityFactor

        val maxOffsetX = max(0f, (scaledWidth - boxSize.width) / 2f)
        val maxOffsetY = max(0f, (scaledHeight - boxSize.height) / 2f) + initialOffsetY.value

        val newOffsetX = (offsetX.value + acceleratedDragAmount.x).coerceIn(-maxOffsetX, maxOffsetX)
        val newOffsetY = (offsetY.value + acceleratedDragAmount.y).coerceIn(-maxOffsetY, maxOffsetY)

        scope.launch {
            scale.snapTo(newScale)
            offsetX.snapTo(newOffsetX)
            offsetY.snapTo(newOffsetY)
        }
    }

    val onSingleTap: (Offset) -> Unit = onSingleTap@{ tapOffset ->
        val bitmap = originalBitmap ?: return@onSingleTap

        val currentScale = scale.value
        val currentOffsetX = offsetX.value
        val currentOffsetY = offsetY.value

        // **KEY CHANGE**: Inverse Transformation: Map screen touch coordinates to bitmap pixel coordinates.
        // ImagePixelPos = (ScreenPos - PanOffset) / Scale
        val imageX = (tapOffset.x - currentOffsetX) / currentScale
        val imageY = (tapOffset.y - currentOffsetY) / currentScale

        // Coerce to integer coordinates
        val pixelX = imageX.toInt().coerceIn(0, bitmap.width - 1)
        val pixelY = imageY.toInt().coerceIn(0, bitmap.height - 1)

        val pixelColor = bitmap[pixelX, pixelY]

        // Color comparison logic (same as before)
        val alpha = android.graphics.Color.alpha(pixelColor)
        val rDiff = abs(android.graphics.Color.red(pixelColor) - android.graphics.Color.red(roadHexColor))
        val gDiff = abs(android.graphics.Color.green(pixelColor) - android.graphics.Color.green(roadHexColor))
        val bDiff = abs(android.graphics.Color.blue(pixelColor) - android.graphics.Color.blue(roadHexColor))

        val isRoad = alpha > 200 &&
                rDiff <= COLOR_TOLERANCE &&
                gDiff <= COLOR_TOLERANCE &&
                bDiff <= COLOR_TOLERANCE

        if (isRoad) {
            // touchPoint is now stored in original image's pixel coordinates
            touchPoint = Offset(imageX, imageY)
            isAddingMarker = true
        }
    }

    // ----------------------------------------------------------------------
    // 5. UI Layout: Using Canvas
    // ----------------------------------------------------------------------

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
            .clipToBounds()
            .pointerInput(Unit) {
                // Combined gesture detection for the entire interactive area
                detectTransformGestures { _, pan, zoom, _ -> onDrag(pan, zoom) }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = onSingleTap)
            }
    ) {

        // --- CANVAS DRAWING ---
        if (originalBitmap != null) {
            val imageBitmap = originalBitmap!!.asImageBitmap()

            // Draw the roadmap and markers inside a single Canvas composable
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Apply the shared transformation for pan and zoom
                // All drawing operations after this will be relative to the transformed space
                translate(offsetX.value, offsetY.value) {
                    scale(scale.value, Offset.Zero) {

                        // 1. Draw the Background Image (Roadmap)
                        drawImage(
                            image = imageBitmap,
                            topLeft = Offset.Zero, // Draw from the image's top-left corner
                            // The size is the original bitmap size, which is then scaled by the matrix

//                            srcSize= IntSize(imageSize.width, imageSize.height),
//                            size = androidx.compose.ui.geometry.Size(imageSize.width.toFloat(), imageSize.height.toFloat())
                        )

                        // 2. Draw the Markers (relative to the scaled image)
                        markers.forEach { marker ->
                            // Convert normalized position (0-100) back to image pixel coordinates
                            val pixelX = marker.position.x / 100f * imageSize.width
                            val pixelY = marker.position.y / 100f * imageSize.height

                            val markerColor = if (marker.type == "milestone") Color(0xFF00C853) else Color(0xFFFF9800)

                            // Draw a filled circle (simple marker)
                            drawCircle(
                                color = markerColor,
                                radius = markerHalfSizePx * 1.5f, // Use an arbitrary size that will scale
                                center = Offset(pixelX, pixelY)
                            )

                            // To draw text on the Canvas, we must use the native Android Canvas via drawIntoCanvas
                            drawIntoCanvas { canvas ->
                                // The canvas operations are already scaled and translated correctly.
                                canvas.nativeCanvas.drawText(
                                    marker.name,
                                    pixelX,
                                    pixelY - markerHalfSizePx * 2.5f, // Offset text above the circle
                                    textPaint
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Roadmap...")
            }
        }

        // CONTROL BUTTONS (Same as before)
        val isAtInitialScale = scale.value == initialScale
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // "FULL" Button
            if (!isAtInitialScale) {
                Button(onClick = {
                    scope.launch { offsetX.animateTo(0f, animationSpec = tween(500)) }
                    scope.launch { offsetY.animateTo(initialOffsetY.value, animationSpec = tween(500)) }
                    scope.launch { scale.animateTo(initialScale, animationSpec = tween(500)) }
                }) {
                    Text("FULL")
                }
            }

            // "TODAY" Button
            if (isAtInitialScale) {
                Button(onClick = {
                    val todaySegment = segments.find { it.date == LocalDate.now() }

                    if (todaySegment != null && imageSize.height > 0) {
                        scope.launch {
                            // 1. Calculate the Y position of the segment's center in original image pixels.
                            val segmentCenterY = ((todaySegment.startY + todaySegment.endY) / 2f) * imageSize.height

                            // 2. Define the Target Scale (Zoom Level)
                            // We use a specific zoom, e.g., 2.5 times the initial scale.
                            val targetScale = initialScale * 2.5f

                            // 3. Calculate the Target Offset Y.
                            // This calculation centers the segment on the screen after scaling.
                            // TargetOffsetY = (Screen Center Y) - (Segment Center Y * Target Scale)
                            // The target Y should be adjusted by the initial vertical offset (initialOffsetY.value)
                            // which is the top margin created when the image is vertically centered at 1x scale.

                            val centerScreenY = boxSize.height / 2f

                            // Y position of the segment center relative to the top of the overall box,
                            // if the image was not initially offset (at Y=0).
                            val segmentYAfterScale = segmentCenterY * targetScale

                            // The required offset is the difference between the center of the screen
                            // and the scaled position of the segment center, adjusted for the initial offset.
                            val targetOffsetY = centerScreenY - segmentYAfterScale + initialOffsetY.value * targetScale

                            // Ensure the X-offset is centered (or 0 if the image fits horizontally)
                            val targetOffsetX = 0f

                            // Animate the pan and zoom
                            scope.launch { offsetX.animateTo(targetOffsetX, animationSpec = tween(500)) }
                            scope.launch { offsetY.animateTo(targetOffsetY, animationSpec = tween(500)) }
                            scope.launch { scale.animateTo(targetScale, animationSpec = tween(500)) }
                        }
                    }
                }) {
                    Text("TODAY")
                }
            }
        }
    }

    // ADD MARKER POPUP (Same as before)
    if (isAddingMarker) {
        var nameField by remember { mutableStateOf("") }
        var markerType by remember { mutableStateOf("checkpoint") }

//        composa("map") {

            CheckpointPopUpScreen()
//        }
    }
}