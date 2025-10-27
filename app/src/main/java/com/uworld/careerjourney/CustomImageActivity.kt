package com.uworld.careerjourney

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.get
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

// Data classes for application state
data class MarkerData(
    val id: Int,
    val position: Offset, // Position on the 0-100 image scale
    val name: String,
    val type: String, // "checkpoint" or "milestone"
    val date: LocalDate,
    val isVisible: Boolean = true,
    val zIndex: Float = 0f
)

data class SegmentData(
    val date: LocalDate,
    val startY: Float, // Relative Y position (0.0 to 1.0)
    val endY: Float
)

// The color value of the road path in your PNG (blue in the example provided)
//private var ROAD_HEX_COLOR = 0xFF0164BE // Your known road color in ARGB format
private const val COLOR_TOLERANCE = 30 // Max allowed difference for R, G, and B components (0-255)

// ----------------------------------------------------------------------
// 1. Core State and Logic
// ----------------------------------------------------------------------

@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveRoadmapScreen(
    @DrawableRes imageResId: Int,
    startDate: LocalDate,
    endDate: LocalDate
) {
    val context = LocalContext.current

    // Load bitmap once and keep it in memory
    val originalBitmap = remember {
//        val options = BitmapFactory.Options()
//        options.inScaled = false // Prevent automatic scaling based on screen density
//        options.inDither = false // Disable dithering for potentially smoother colors
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888 // Use 32-bit color for highest quality
        BitmapFactory.decodeResource(context.resources, imageResId)
    }

    // 1. Convert the static constant into a mutable state
    var roadHexColor by remember { mutableIntStateOf(0xFF0162BA.toInt()) }

    // STATE VARIABLES
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    var initialScale by remember { mutableFloatStateOf(1f) } // Will be calculated in LaunchedEffect
    val initialOffsetY = remember { Animatable(0f) } // Initial offset for centering

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
    // 2. ASPECT RATIO & INITIAL ZOOM (Requirements 1 & 5)
    // ----------------------------------------------------------------------

    // Calculate the initial scale and bounds once the sizes are known
    LaunchedEffect(boxSize, imageSize) {
        if (boxSize.width > 0) {
//            roadHexColor = findDarkestColor(originalBitmap)

            // Optional logging
            val hexString = String.format(Locale.US, "#%08X", roadHexColor)
            println("Image Analysis: Darkest Road Color set to -> $hexString")
        }

        if (boxSize.width > 0 && imageSize.width > 0) {
            // Requirement 5: Fit image width to box width
            val calculatedInitialScale = boxSize.width.toFloat() / imageSize.width.toFloat()

            // Calculate the height of the scaled image
            val scaledHeight = imageSize.height * calculatedInitialScale
            val verticalCenterOffset = (boxSize.height - scaledHeight) / 2f

            scale.snapTo(calculatedInitialScale)
            initialScale = calculatedInitialScale
            initialOffsetY.snapTo(verticalCenterOffset)
            offsetY.snapTo(verticalCenterOffset)

            // Requirement 1: Reset offsetX to center
            offsetX.snapTo(0f)
        }
    }

    // ----------------------------------------------------------------------
    // 3. TOUCH & DRAG LOGIC (Requirements 1, 2, 7)
    // ----------------------------------------------------------------------

    val scope = rememberCoroutineScope()
    val onDrag: (Offset, Float) -> Unit = { dragAmount, zoomChange ->
        val currentScale = scale.value * zoomChange
        val newScale = max(initialScale, currentScale) // Requirement 7: Cannot zoom out past initial

        // Calculate the maximum allowed translation based on the new scale
        val scaledWidth = imageSize.width * newScale
        val scaledHeight = imageSize.height * newScale

        val maxOffsetX = max(0f, (scaledWidth - boxSize.width) / 2f)
        val maxOffsetY = max(0f, (scaledHeight - boxSize.height) / 2f) + initialOffsetY.value

        // Calculate new offsets
        val newOffsetX = (offsetX.value + dragAmount.x).coerceIn(-maxOffsetX, maxOffsetX)
        val newOffsetY = (offsetY.value + dragAmount.y).coerceIn(-maxOffsetY, maxOffsetY)

        // Apply
        scope.launch {
            scale.snapTo(newScale)
            offsetX.snapTo(newOffsetX)
            offsetY.snapTo(newOffsetY)
        }
    }

    // Touch logic for adding markers (Requirement 2 & 3)
    val onSingleTap: (Offset) -> Unit = { tapOffset ->
        // 1. Transform screen tap coordinates to image coordinates
        val imageX = (tapOffset.x - offsetX.value) / scale.value
        val imageY = (tapOffset.y - offsetY.value) / scale.value

        if (imageX in 0f..imageSize.width.toFloat() && imageY in 0f..imageSize.height.toFloat()) {
            // 2. Get the color from the bitmap at the touch point
            val pixelX = imageX.toInt()/*.coerceIn(0, originalBitmap.width - 1)*/
            val pixelY = imageY.toInt()/*.coerceIn(0, originalBitmap.height - 1)*/
            val pixelColor = originalBitmap.getPixel(pixelX, pixelY)

            // Format the integer color (pixelColor) into an 8-digit hexadecimal string (AARRGGBB)
            val hexColor = String.format("#%08X", pixelColor)
            println("Touched Pixel Color (ARGB): $hexColor")

            // 2. Extract components from the touch color
            val alpha = android.graphics.Color.alpha(pixelColor)
            val rTouch = android.graphics.Color.red(pixelColor)
            val gTouch = android.graphics.Color.green(pixelColor)
            val bTouch = android.graphics.Color.blue(pixelColor)

            // 3. Extract components from the target road color
            val rTarget = android.graphics.Color.red(roadHexColor)
            val gTarget = android.graphics.Color.green(roadHexColor)
            val bTarget = android.graphics.Color.blue(roadHexColor)

            // 4. Calculate absolute differences
            val rDiff = kotlin.math.abs(rTouch - rTarget)
            val gDiff = kotlin.math.abs(gTouch - gTarget)
            val bDiff = kotlin.math.abs(bTouch - bTarget)

            // 5. Check if the touch area is close to the road color and is opaque (Requirement 2)
            val isRoad = alpha > 200 && // Check for near-opaque touch
                    rDiff <= COLOR_TOLERANCE &&
                    gDiff <= COLOR_TOLERANCE &&
                    bDiff <= COLOR_TOLERANCE

            if (isRoad) {
                touchPoint = Offset(imageX, imageY)
                isAddingMarker = true // Show the popup
            }
        }
    }

    // ----------------------------------------------------------------------
    // 4. UI Layout
    // ----------------------------------------------------------------------

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it } // Capture container size
            .clipToBounds()
    ) {
        remember { this }

        // IMAGE VIEW
        Image(
            bitmap = originalBitmap.asImageBitmap(),
            contentDescription = "Roadmap Path",
            modifier = Modifier
                .wrapContentSize()
                .onSizeChanged { imageSize = it } // Capture original image size
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

        // MARKERS (Requirement 4)
        markers.forEach { marker ->
            // Convert 0-ImageSize marker position to transformed screen position
            val screenX = marker.position.x * scale.value + offsetX.value
            val screenY = marker.position.y * scale.value + offsetY.value

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = screenX - 16.dp.toPx() // Adjust for marker size
                        translationY = screenY - 32.dp.toPx() // Adjust for marker size
                        // Note: ZIndex for the marker assets would go here
                    }
                    .background(
                        if (marker.type == "milestone") Color(0xFF00C853) else Color(0xFFFF9800),
                        RoundedCornerShape(4.dp)
                    ) // Placeholder 3D asset
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
                    scope.launch {
                        // Reset to initial scale and center
                        scale.animateTo(initialScale, animationSpec = tween(300))
                        offsetX.animateTo(0f, animationSpec = tween(300))
                        offsetY.animateTo(initialOffsetY.value, animationSpec = tween(300))
                    }
                }) {
                    Text("FULL")
                }
            }

            if (isAtInitialScale) {
                // "TODAY" Button
                Button(onClick = {
                    // Find today's segment
                    val todaySegment = segments.find { it.date == LocalDate.now() }

                    if (todaySegment != null && imageSize.height > 0) {
                        scope.launch {
                            // Calculate today's segment center Y in image pixels
                            val segmentCenterY = ((todaySegment.startY + todaySegment.endY) / 2f) * imageSize.height

                            // Calculate the offset needed to center this point on screen
                            val centerScreenY = boxSize.height / 2f
                            val targetOffsetY = centerScreenY - segmentCenterY * scale.value

                            // Perform a zoom in for the "today" view (e.g., 2.5x initial scale)
                            val targetScale = initialScale * 2.5f

                            // Apply the transformation
                            scale.animateTo(targetScale, animationSpec = tween(500))
                            offsetX.animateTo(0f, animationSpec = tween(500))
                            offsetY.animateTo(targetOffsetY, animationSpec = tween(500))

                            // After zoom, show all markers (simplified for this example)
                            // Requirement 9: Show all markers once zoomed in (full logic is complex, simplified here)
                            // markers.forEach { it.isVisible = true } // Requires state management update
                        }
                    }
                }) {
                    Text("TODAY")
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // 5. ADD MARKER POPUP (Requirement 3 & 4)
    // ----------------------------------------------------------------------

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
                            position = Offset(
                                touchPoint.x / (originalBitmap?.width ?: 0) * 100,
                                touchPoint.y / (originalBitmap?.height ?: 0) * 100
                            ), // Save as 0-100 scale
                            name = nameField.ifEmpty { "New $markerType" },
                            type = markerType,
                            date = LocalDate.now() // You'd calculate the date based on touchPoint.y and segments
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

/**
 * Finds the color with the lowest luminance (darkest color) in a given Bitmap.
 *
 * @param bitmap The Bitmap to analyze.
 * @return The darkest color code as an opaque 8-digit hexadecimal string (#AARRGGBB).
 */
fun findDarkestColor(bitmap: Bitmap): Int {
    if (bitmap.isRecycled) {
        return android.graphics.Color.BLACK // Default black if the bitmap is invalid
    }

    var minLuminance = Float.MAX_VALUE
    var darkestColorInt = Color.Black.toArgb() // Initialize with pure black

    // Iterate through every pixel
    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            val pixel = bitmap[x, y]

            // Ignore transparent pixels, as they don't contribute to "darkness"
            if (android.graphics.Color.alpha(pixel) < 10) {
                continue
            }

            // Calculate Luminance (a measure of perceived brightness)
            // L = 0.2126*R + 0.7152*G + 0.0722*B
            // A lower luminance means a darker color.
            val r = android.graphics.Color.red(pixel).toFloat()
            val g = android.graphics.Color.green(pixel).toFloat()
            val b = android.graphics.Color.blue(pixel).toFloat()

            val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b

            if (luminance < minLuminance) {
                minLuminance = luminance
                darkestColorInt = pixel
            }
        }
    }

    // Format the darkest color as an 8-digit hexadecimal string (AARRGGBB)
//    return String.format(Locale.US, "#%08X", darkestColorInt)
    return darkestColorInt
}

/**
 * Decodes a raster image (PNG/JPG) to a Bitmap optimized for target dimensions.
 * This prevents memory issues and quality loss by calculating the best inSampleSize.
 *
 * @param res Android Resources object.
 * @param resId Drawable resource ID (R.drawable.my_image).
 * @param targetWidth The desired final width in pixels (e.g., boxSize.width).
 * @param targetHeight The desired final height in pixels (e.g., boxSize.height).
 * @return An optimized Bitmap, or null if decoding fails.
 */
fun decodeOptimizedBitmap(
    res: Resources,
    @DrawableRes resId: Int,
    targetWidth: Int,
    targetHeight: Int
): Bitmap? {
    // 1. Read the dimensions of the image without decoding the pixels (inJustDecodeBounds = true)
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(res, resId, options)

    val actualWidth = options.outWidth
    val actualHeight = options.outHeight

    // 2. Calculate the optimal inSampleSize
    var inSampleSize = 1
    if (actualHeight > targetHeight || actualWidth > targetWidth) {
        val halfHeight = actualHeight / 2
        val halfWidth = actualWidth / 2

        // Calculate the largest inSampleSize value that is a power of 2
        // and keeps both height and width larger than or equal to the target dimensions.
        while ((halfHeight / inSampleSize) >= targetHeight && (halfWidth / inSampleSize) >= targetWidth) {
            inSampleSize *= 2
        }
    }

    // Use the calculated inSampleSize for the final decoding
    options.inSampleSize = inSampleSize

    // Optional: Ensure high quality decoding
    options.inPreferredConfig = Bitmap.Config.ARGB_8888

    // 3. Decode the bitmap with the calculated inSampleSize
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}

class CustomImageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                // Start and End dates for vertical segmentation
                val startDate = LocalDate.now().minusDays(100)
                val endDate = LocalDate.now().plusDays(100)

                // Pass the road path PNG you wanted earlier (saved in res/drawable as roadmap_path)
                InteractiveRoadmapScreen(
                    imageResId = R.drawable.roadmap_blue,
                    startDate = startDate,
                    endDate = endDate
                )
            }
        }
    }
}