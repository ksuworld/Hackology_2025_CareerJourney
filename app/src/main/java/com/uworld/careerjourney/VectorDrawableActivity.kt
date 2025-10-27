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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class VectorDrawableActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                val startDate = LocalDate.now().minusDays(100)
                val endDate = LocalDate.now().plusDays(100)

                InteractiveRoadmapScreenVectorDrawable2(
                    // IMPORTANT: This must be a Vector Drawable (XML) resource ID
                    imageResId = R.drawable.roadmap_vector_blue,
                    startDate = startDate,
                    endDate = endDate
                )
            }
        }
    }
}

// Constants
private const val COLOR_TOLERANCE = 70 // Max allowed difference for R, G, and B components (0-255)
// Base velocity factor (used for standard sensitivity)
private const val BASE_DRAG_VELOCITY = 1.0f
// You can increase this factor to make all drag movements snappier, regardless of zoom.
private const val ACCELERATION_MULTIPLIER = 1.5f
// ----------------------------------------------------------------------
// 1. UTILITIES: Vector to Bitmap & Darkest Color Finder
// ----------------------------------------------------------------------

/**
 * Creates a Bitmap from a VectorDrawable resource ID.
 * The VectorDrawable is drawn onto a Bitmap with the specified pixel dimensions.
 */
fun getBitmapFromVectorDrawable(
    context: Context,
    @DrawableRes drawableId: Int,
    width: Int,
    height: Int
): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

    // Ensure the Bitmap has a high-quality configuration
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}


// ----------------------------------------------------------------------
// 2. MAIN COMPOSABLE
// ----------------------------------------------------------------------

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveRoadmapScreenVectorDrawable(
    @DrawableRes imageResId: Int,
    startDate: LocalDate,
    endDate: LocalDate
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // STATE VARIABLES
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    // The rendered bitmap used for display and color picking
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    var initialScale by remember { mutableFloatStateOf(1f) }
    val initialOffsetY = remember { Animatable(0f) }

    // Dynamic color state for touch detection (initialized to a default blue)
    var roadHexColor by remember { mutableIntStateOf(0xFF0162BA.toInt()) }

    var isAddingMarker by remember { mutableStateOf(false) }
    var touchPoint by remember { mutableStateOf(Offset.Zero) }
    val markers = remember { mutableStateListOf<MarkerData>() }

    // SEGMENTATION DATA (Requirement 6)
    val segments: List<SegmentData> = remember(startDate, endDate) {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toFloat()
        val daysList = (0..totalDays.toInt()).map { startDate.plusDays(it.toLong()) }

        daysList.mapIndexed { index, date ->
            val startY = index / totalDays
            val endY = min(1f, (index + 1) / totalDays)
            SegmentData(date, startY, endY)
        }
    }

    // ----------------------------------------------------------------------
    // 3. DYNAMIC SVG LOADING AND INITIAL ZOOM (Requirements 1 & 5)
    // ----------------------------------------------------------------------

    LaunchedEffect(boxSize) {
        if (boxSize.width > 0) {
            val drawable = ContextCompat.getDrawable(context, imageResId)

            val drawableWidth = drawable?.intrinsicWidth ?: 1 // Use intrinsic for aspect ratio
            val drawableHeight = drawable?.intrinsicHeight ?: 1

            if (drawableWidth > 0 && drawableHeight > 0) {

                // Calculate aspect ratio
                val aspectRatio = drawableHeight.toFloat() / drawableWidth.toFloat()

                // Target Bitmap Size (Fit width, wrap height)
                val targetBitmapWidth = boxSize.width
                val targetBitmapHeight = (targetBitmapWidth.toFloat() * aspectRatio).toInt()

                // Render the Vector to Bitmap at the calculated size
                val renderedBitmap = getBitmapFromVectorDrawable(
                    context = context,
                    drawableId = imageResId,
                    width = targetBitmapWidth,
                    height = targetBitmapHeight
                )

                if (renderedBitmap != null) {
                    originalBitmap = renderedBitmap
                    imageSize = IntSize(renderedBitmap.width, renderedBitmap.height)

                    // Find and set the darkest color dynamically
//                    roadHexColor = findDarkestColor(renderedBitmap)
                    roadHexColor = "#0164BE".toColorInt()
                    val hexString = String.format(Locale.US, "#%08X", roadHexColor)
                    println("Image Analysis: Darkest Road Color set to -> $hexString")

                    // Set Initial Zoom (Scale = 1f because Bitmap width == Box width)
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
    // 4. TOUCH & DRAG LOGIC (Corrected for robustness)
    // ----------------------------------------------------------------------

    val onDrag: (Offset, Float) -> Unit = { dragAmount, zoomChange ->
        val currentScale = scale.value * zoomChange
        val newScale = max(initialScale, currentScale) // Requirement 7: Cannot zoom out past initial

        val scaledWidth = imageSize.width * newScale
        val scaledHeight = imageSize.height * newScale

        // The previous dynamic factor (1.0f / newScale) is REMOVED.
        // The drag movement is now based on a fixed velocity boost, ignoring the zoom level's need for precision.
        val dynamicVelocityFactor = BASE_DRAG_VELOCITY * ACCELERATION_MULTIPLIER

        // Apply the constant factor to the drag amount
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
        val bitmap = originalBitmap ?: return@onSingleTap // Safety check

        val currentScale = scale.value
        val currentOffsetX = offsetX.value
        val currentOffsetY = offsetY.value

        // 1. Inverse Transformation: Map screen touch coordinates to bitmap pixel coordinates.
        val imageX = (tapOffset.x - currentOffsetX) / currentScale
        val imageY = (tapOffset.y - currentOffsetY) / currentScale

        // 2. Coerce the integer coordinates into the valid Bitmap index range (FIX for OOB/Rounding errors)
        val pixelX = imageX.toInt().coerceIn(0, bitmap.width - 1)
        val pixelY = imageY.toInt().coerceIn(0, bitmap.height - 1)

        val pixelColor = bitmap[pixelX, pixelY]
        val hexString = String.format(Locale.US, "#%08X", pixelColor)
        println("Touch area color -> $hexString")

        // 3. Extract components from touch and target
        val alpha = android.graphics.Color.alpha(pixelColor)
        val rTouch = android.graphics.Color.red(pixelColor)
        val gTouch = android.graphics.Color.green(pixelColor)
        val bTouch = android.graphics.Color.blue(pixelColor)

        val rTarget = android.graphics.Color.red(roadHexColor)
        val gTarget = android.graphics.Color.green(roadHexColor)
        val bTarget = android.graphics.Color.blue(roadHexColor)

        // 4. Calculate absolute differences
        val rDiff = abs(rTouch - rTarget)
        val gDiff = abs(gTouch - gTarget)
        val bDiff = abs(bTouch - bTarget)

        // 5. Check if the touch area is close to the road color and is opaque (Requirement 2)
        val isRoad = alpha > 200 &&
                rDiff <= COLOR_TOLERANCE &&
                gDiff <= COLOR_TOLERANCE &&
                bDiff <= COLOR_TOLERANCE

        if (isRoad) {
            touchPoint = Offset(imageX, imageY)
            isAddingMarker = true // Show the popup
        }
    }

    // ----------------------------------------------------------------------
    // 5. UI Layout
    // ----------------------------------------------------------------------

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
            .clipToBounds()
    ) {

        // IMAGE VIEW
        if (originalBitmap != null) {
            Image(
                bitmap = originalBitmap!!.asImageBitmap(),
                contentDescription = "Roadmap Path",
                modifier = Modifier
                    .wrapContentSize()
                    .graphicsLayer {
                        // Apply the current pan and zoom transformations
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = offsetX.value
                        translationY = offsetY.value
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            onDrag(pan, zoom)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = onSingleTap)
                    }
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Roadmap...")
            }
        }

        // MARKERS (Requirement 4) - Using imageSize for 0-100 normalization
        markers.forEach { marker ->
            val screenX = (marker.position.x / 100f * imageSize.width) * scale.value + offsetX.value
            val screenY = (marker.position.y / 100f * imageSize.height) * scale.value + offsetY.value

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = screenX - 16.dp.toPx()
                        translationY = screenY - 32.dp.toPx()
                    }
                    .background(
                        if (marker.type == "milestone") Color(0xFF00C853) else Color(0xFFFF9800),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { /* Handle marker click */ }
            ) {
                Text(
                    text = marker.name,
                    color = Color.White
                )
            }
        }

        // CONTROL BUTTONS (Requirement 8)
        val isAtInitialScale = scale.value == initialScale
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // "FULL" Button (Show if not at initial scale)
            if (!isAtInitialScale) {
                Button(onClick = {
                    scope.launch { offsetX.animateTo(0f, animationSpec = tween(500)) }
                    scope.launch { offsetY.animateTo(initialOffsetY.value, animationSpec = tween(500)) }
                    scope.launch { scale.animateTo(initialScale, animationSpec = tween(500)) }
                }) {
                    Text("FULL")
                }
            }

            // "TODAY" Button (Only show at initial scale based on your requirement)
            if (isAtInitialScale) {
                Button(onClick = {
                    val todaySegment = segments.find { it.date == LocalDate.now() }

                    if (todaySegment != null && imageSize.height > 0) {
                        scope.launch {
                            val segmentCenterY = ((todaySegment.startY + todaySegment.endY) / 2f) * imageSize.height
                            val centerScreenY = boxSize.height / 2f
                            val targetScale = initialScale * 2.5f // Zoom in
                            val targetOffsetY = centerScreenY - segmentCenterY * targetScale

                            scope.launch { offsetX.animateTo(0f, animationSpec = tween(500)) }
                            scope.launch { offsetY.animateTo(targetOffsetY, animationSpec = tween(500)) }
                            scope.launch { scale.animateTo(targetScale, animationSpec = tween(500)) }

                            // Markers visibility logic goes here (Requirement 9)
                        }
                    }
                }) {
                    Text("TODAY")
                }
            }
        }
    }

    // ADD MARKER POPUP (Requirement 3 & 4)
    if (isAddingMarker) {
        var nameField by remember { mutableStateOf("") }
        var markerType by remember { mutableStateOf("checkpoint") }

        AlertDialog(
            onDismissRequest = { isAddingMarker = false },
            title = { Text("Add Checkpoint/Milestone") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        label = { Text("Name") }
                    )
                    Button(onClick = { markerType = "checkpoint" }) { Text("Checkpoint") }
                    Button(onClick = { markerType = "milestone" }) { Text("Milestone") }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newMarker = MarkerData(
                            id = markers.size + 1,
                            // Map touch pixel coordinate back to 0-100 normalized scale
                            position = Offset(
                                touchPoint.x / imageSize.width * 100,
                                touchPoint.y / imageSize.height * 100
                            ),
                            name = nameField.ifEmpty { "New $markerType" },
                            type = markerType,
                            date = LocalDate.now()
                        )
                        markers.add(newMarker)
                        isAddingMarker = false
                    },
                    enabled = nameField.isNotEmpty()
                ) {
                    Text("Add")
                }
            }
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveRoadmapScreenVectorDrawable2(
    @DrawableRes imageResId: Int,
    startDate: LocalDate,
    endDate: LocalDate
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current // Add LocalDensity here

    // STATE VARIABLES
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    // The rendered bitmap used for display and color picking
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    var initialScale by remember { mutableFloatStateOf(1f) }
    val initialOffsetY = remember { Animatable(0f) }

    // Dynamic color state for touch detection (initialized to a default blue)
    var roadHexColor by remember { mutableIntStateOf(0xFF0162BA.toInt()) }

    var isAddingMarker by remember { mutableStateOf(false) }
    var touchPoint by remember { mutableStateOf(Offset.Zero) }
    val markers = remember { mutableStateListOf<MarkerData>() }

    // SEGMENTATION DATA (Requirement 6)
    val segments: List<SegmentData> = remember(startDate, endDate) {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toFloat()
        // Handle case where totalDays is 0 to avoid division by zero
        if (totalDays <= 0) return@remember emptyList()

        val daysList = (0..totalDays.toInt()).map { startDate.plusDays(it.toLong()) }

        daysList.mapIndexed { index, date ->
            val startY = index / totalDays
            val endY = min(1f, (index + 1) / totalDays)
            SegmentData(date, startY, endY)
        }
    }

    // ----------------------------------------------------------------------
    // 3. DYNAMIC SVG LOADING AND INITIAL ZOOM (Requirements 1 & 5)
    // ----------------------------------------------------------------------

    LaunchedEffect(boxSize) {
        if (boxSize.width > 0) {
            val drawable = ContextCompat.getDrawable(context, imageResId)

            val drawableWidth = drawable?.intrinsicWidth ?: 1 // Use intrinsic for aspect ratio
            val drawableHeight = drawable?.intrinsicHeight ?: 1

            if (drawableWidth > 0 && drawableHeight > 0) {

                // Calculate aspect ratio
                val aspectRatio = drawableHeight.toFloat() / drawableWidth.toFloat()

                // Target Bitmap Size (Fit width, wrap height)
                val targetBitmapWidth = boxSize.width
                val targetBitmapHeight = (targetBitmapWidth.toFloat() * aspectRatio).toInt()

                // Render the Vector to Bitmap at the calculated size
                val renderedBitmap = getBitmapFromVectorDrawable(
                    context = context,
                    drawableId = imageResId,
                    width = targetBitmapWidth,
                    height = targetBitmapHeight
                )

                if (renderedBitmap != null) {
                    originalBitmap = renderedBitmap
                    imageSize = IntSize(renderedBitmap.width, renderedBitmap.height)

                    // Find and set the darkest color dynamically
//                    roadHexColor = findDarkestColor(renderedBitmap)
                    roadHexColor = "#0164BE".toColorInt()
                    val hexString = String.format(Locale.US, "#%08X", roadHexColor)
                    println("Image Analysis: Darkest Road Color set to -> $hexString")

                    // Set Initial Zoom (Scale = 1f because Bitmap width == Box width)
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
    // 4. TOUCH & DRAG LOGIC (Corrected for robustness)
    // ----------------------------------------------------------------------

    val onDrag: (Offset, Float) -> Unit = { dragAmount, zoomChange ->
        val currentScale = scale.value * zoomChange
        val newScale = max(initialScale, currentScale) // Requirement 7: Cannot zoom out past initial

        val scaledWidth = imageSize.width * newScale
        val scaledHeight = imageSize.height * newScale

        // The previous dynamic factor (1.0f / newScale) is REMOVED.
        // The drag movement is now based on a fixed velocity boost, ignoring the zoom level's need for precision.
        val dynamicVelocityFactor = BASE_DRAG_VELOCITY * ACCELERATION_MULTIPLIER

        // Apply the constant factor to the drag amount
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
        val bitmap = originalBitmap ?: return@onSingleTap // Safety check

        val currentScale = scale.value
        val currentOffsetX = offsetX.value
        val currentOffsetY = offsetY.value

        // 1. Inverse Transformation: Map screen touch coordinates to bitmap pixel coordinates.
        //    (ScreenPos - PanOffset) / Scale = ImagePixelPos
        val imageX = (tapOffset.x - currentOffsetX) / currentScale
        val imageY = (tapOffset.y - currentOffsetY) / currentScale

        // 2. Coerce the integer coordinates into the valid Bitmap index range (FIX for OOB/Rounding errors)
        val pixelX = imageX.toInt().coerceIn(0, bitmap.width - 1)
        val pixelY = imageY.toInt().coerceIn(0, bitmap.height - 1)

        val pixelColor = bitmap[pixelX, pixelY]
        val hexString = String.format(Locale.US, "#%08X", pixelColor)
        println("Touch area color -> $hexString")

        // 3. Extract components from touch and target
        val alpha = android.graphics.Color.alpha(pixelColor)
        val rTouch = android.graphics.Color.red(pixelColor)
        val gTouch = android.graphics.Color.green(pixelColor)
        val bTouch = android.graphics.Color.blue(pixelColor)

        val rTarget = android.graphics.Color.red(roadHexColor)
        val gTarget = android.graphics.Color.green(roadHexColor)
        val bTarget = android.graphics.Color.blue(roadHexColor)

        // 4. Calculate absolute differences
        val rDiff = abs(rTouch - rTarget)
        val gDiff = abs(gTouch - gTarget)
        val bDiff = abs(bTouch - bTarget)

        // 5. Check if the touch area is close to the road color and is opaque (Requirement 2)
        val isRoad = alpha > 200 &&
                rDiff <= COLOR_TOLERANCE &&
                gDiff <= COLOR_TOLERANCE &&
                bDiff <= COLOR_TOLERANCE

        if (isRoad) {
            touchPoint = Offset(imageX, imageY)
            isAddingMarker = true // Show the popup
        }
    }

    // ----------------------------------------------------------------------
    // 5. UI Layout
    // ----------------------------------------------------------------------

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(onTap = onSingleTap)
            }
    ) {

        // IMAGE VIEW
        if (originalBitmap != null) {
            Image(
                bitmap = originalBitmap!!.asImageBitmap(),
                contentDescription = "Roadmap Path",
                modifier = Modifier
                    .wrapContentSize()
                    .graphicsLayer {
                        // Apply the current pan and zoom transformations
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = offsetX.value
                        translationY = offsetY.value
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ -> onDrag(pan, zoom) }
                    }
//                    .pointerInput(Unit) {
//                        detectTapGestures(onTap = onSingleTap)
//                    }
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Roadmap...")
            }
        }

        // --- START OF MARKER FIX ---

        // Pre-calculate the marker size offset in pixels for centering.
        // We use constant DP values for the visual size of the marker.
        // We must convert these to pixels only once.
        val markerCenterOffsetX = with(density) { 16.dp.toPx() }
        val markerCenterOffsetY = with(density) { 32.dp.toPx() }


        // MARKERS (Requirement 4) - Using imageSize for 0-100 normalization
        markers.forEach { marker ->
            // 1. Calculate the marker's position in the original image's pixel space.
            val originalImageX = marker.position.x / 100f * imageSize.width
            val originalImageY = marker.position.y / 100f * imageSize.height

            // 2. Calculate the screen position: (OriginalPos * Scale) + PanOffset
            val screenX = originalImageX * scale.value + offsetX.value
            val screenY = originalImageY * scale.value + offsetY.value

            // 3. Calculate the translation, adjusting for the marker's size.
            //    The size adjustment must be scaled by the current zoom level (`scale.value`)
            //    to keep the marker centered on the scaled image point.
            val translationX = screenX + (markerCenterOffsetX * scale.value)
            val translationY = screenY + (markerCenterOffsetY * scale.value)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        // Apply the calculated screen position translation
                        this.translationX = translationX
                        this.translationY = translationY

                        // **CRUCIAL FIX**: Counteract the image zoom on the marker itself.
                        // This keeps the marker's visual size constant and readable on the screen.
                        this.scaleX = 1f / scale.value
                        this.scaleY = 1f / scale.value
                    }
                    .background(
                        if (marker.type == "milestone") Color(0xFF00C853) else Color(0xFFFF9800),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { /* Handle marker click */ }
            ) {
                Text(
                    text = marker.name,
                    color = Color.White
                )
            }
        }

        // --- END OF MARKER FIX ---


        // CONTROL BUTTONS (Requirement 8)
        val isAtInitialScale = scale.value == initialScale
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // "FULL" Button (Show if not at initial scale)
            if (!isAtInitialScale) {
                Button(onClick = {
                    scope.launch { offsetX.animateTo(0f, animationSpec = tween(500)) }
                    scope.launch { offsetY.animateTo(initialOffsetY.value, animationSpec = tween(500)) }
                    scope.launch { scale.animateTo(initialScale, animationSpec = tween(500)) }
                }) {
                    Text("FULL")
                }
            }

            // "TODAY" Button (Only show at initial scale based on your requirement)
            if (isAtInitialScale) {
                Button(onClick = {
                    val todaySegment = segments.find { it.date == LocalDate.now() }

                    if (todaySegment != null && imageSize.height > 0) {
                        scope.launch {
                            val segmentCenterY = ((todaySegment.startY + todaySegment.endY) / 2f) * imageSize.height
                            val centerScreenY = boxSize.height / 2f
                            val targetScale = initialScale * 2.5f // Zoom in
                            val targetOffsetY = centerScreenY - segmentCenterY * targetScale

                            scope.launch { offsetX.animateTo(0f, animationSpec = tween(500)) }
                            scope.launch { offsetY.animateTo(targetOffsetY, animationSpec = tween(500)) }
                            scope.launch { scale.animateTo(targetScale, animationSpec = tween(500)) }

                            // Markers visibility logic goes here (Requirement 9)
                        }
                    }
                }) {
                    Text("TODAY")
                }
            }
        }
    }

    // ADD MARKER POPUP (Requirement 3 & 4)
    if (isAddingMarker) {
        var nameField by remember { mutableStateOf("") }
        var markerType by remember { mutableStateOf("checkpoint") }

        AlertDialog(
            onDismissRequest = { isAddingMarker = false },
            title = { Text("Add Checkpoint/Milestone") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        label = { Text("Name") }
                    )
                    Button(onClick = { markerType = "checkpoint" }) { Text("Checkpoint") }
                    Button(onClick = { markerType = "milestone" }) { Text("Milestone") }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newMarker = MarkerData(
                            id = markers.size + 1,
                            // Map touch pixel coordinate back to 0-100 normalized scale
                            position = Offset(
                                touchPoint.x / imageSize.width * 100,
                                touchPoint.y / imageSize.height * 100
                            ),
                            name = nameField.ifEmpty { "New $markerType" },
                            type = markerType,
                            date = LocalDate.now()
                        )
                        markers.add(newMarker)
                        isAddingMarker = false
                    },
                    enabled = nameField.isNotEmpty()
                ) {
                    Text("Add")
                }
            }
        )
    }
}