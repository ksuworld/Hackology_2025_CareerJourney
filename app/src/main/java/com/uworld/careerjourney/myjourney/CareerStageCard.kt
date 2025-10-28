import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max

// --- 1. DATA MODELS & COLORS (Reused for Context) ---
// Colors for section banners
val PreCollegeColor = Color(0xFF800080)
val InCollegeColor = Color(0xFF008000)
val AfterCollegeColor = Color(0xFFFFA500)

// Data structure for the inner stage card (simplified for this container demo)
data class ImageCareerStage(
    val title: String,
    val progressPercent: Int,
    val isCompleted: Boolean,
    val isCurrentFocus: Boolean,
    val buttonText: String,
    val accentColor: Color
)

// Data structure for the outer container section
data class JourneyStageContainerData(
    val name: String,
    val color: Color,
    val stages: List<ImageCareerStage>
)

// --- 2. JOURNEY SECTION CONTAINER ---

/**
 * Renders the Journey Section Header (Banner) and the Stages within it.
 * This function acts as the outer container for a single major journey (e.g., IN-COLLEGE JOURNEY).
 */
@Composable
fun JourneyStageContainer(section: JourneyStageContainerData) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. Section Header (The Banner)
        JourneySectionHeader(section = section)

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Stages Content (The Wrapper for the inner cards)
        // We use a custom layout (or a wrapper simulating FlowRow) to place items horizontally
        // until the row is full, then wrap to the next line, matching the image's appearance.
        HorizontalFlowLayout(
            modifier = Modifier.fillMaxWidth(),
            horizontalSpacing = 16.dp, // Spacing between cards
            verticalSpacing = 16.dp  // Spacing between rows of cards
        ) {
            section.stages.forEach { stage ->
                // Call the inner card Composable (from the previous step)
                // Note: We're calling a dummy version here, but in a full app, you'd call
                // the full ImageCareerStageCard Composable.
                DummyImageCareerStageCard(stage = stage)
            }
        }
    }
}

// Composable for the Journey Section Header (The Banner)
@Composable
fun JourneySectionHeader(section: JourneyStageContainerData) {
    Text(
        text = section.name,
        color = Color.White,
        fontWeight = FontWeight.Black,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(section.color.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    )
}

// Dummy Composable for the Inner Card (Used here to avoid complexity, but represents the Card UI)
@Composable
fun DummyImageCareerStageCard(stage: ImageCareerStage) {
    // This width must match the calculated size in HorizontalFlowLayout
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp)
            .border(2.dp, if (stage.isCurrentFocus) stage.accentColor else Color.Transparent, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (stage.isCompleted) Color(0xFFE0E0E0) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
            Text(stage.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Custom Layout Composable to mimic FlowRow behavior for two cards per row
@Composable
fun HorizontalFlowLayout(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    content: @Composable () -> Unit
) {
    // This custom layout arranges children until it runs out of space, then wraps.
    // Given the fixed width of 160.dp for the cards, this simulates the two-card-per-row grid.
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hSpacing = horizontalSpacing.roundToPx()
        val vSpacing = verticalSpacing.roundToPx()
        val rowWidth = constraints.maxWidth

        var xPosition = 0
        var yPosition = 0
        var rowHeight = 0

        val placeables = measurables.map { measurable ->
            // Assume the width of the card is fixed to 160.dp as per the design
            val placeable = measurable.measure(Constraints.fixedWidth(160.dp.roundToPx()))

            if (xPosition + placeable.width > rowWidth) {
                // Start new row
                xPosition = 0
                yPosition += rowHeight + vSpacing
                rowHeight = 0
            }

            val position = Offset(xPosition.toFloat(), yPosition.toFloat())
            xPosition += placeable.width + hSpacing
            rowHeight = max(rowHeight, placeable.height)

            placeable to position
        }

        val layoutHeight = yPosition + rowHeight

        layout(rowWidth, layoutHeight) {
            placeables.forEach { (placeable, position) ->
                placeable.place(position.x.toInt(), position.y.toInt())
            }
        }
    }
}


// --- 3. PREVIEW ---

@Preview(showBackground = true, name = "Journey Section Container")
@Composable
fun JourneySectionContainerPreview() {
    // Sample data simulating the IN-COLLEGE JOURNEY section
    val sampleStages = listOf(
        ImageCareerStage("Freshman Year", 100, true, false, "Review", InCollegeColor),
        ImageCareerStage("Sophomore Year", 75, false, true, "Continue", InCollegeColor),
        ImageCareerStage("Junior Year", 0, false, false, "Start", InCollegeColor),
        ImageCareerStage("Senior Launch", 0, false, false, "Start", InCollegeColor),
    )

    val sampleSection = JourneyStageContainerData(
        name = "IN-COLLEGE JOURNEY",
        color = InCollegeColor,
        stages = sampleStages
    )

    // The main screen will use a LazyColumn to stack these sections
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Assuming the app background is white
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Simulate the Pre-College Section (as the first item)
            JourneyStageContainer(
                section = JourneyStageContainerData(
                    name = "PRE-COLLEGE JOURNEY",
                    color = PreCollegeColor,
                    stages = listOf(ImageCareerStage("HS Prep", 100, true, false, "Review", PreCollegeColor), ImageCareerStage("College App", 100, true, false, "Review", PreCollegeColor))
                )
            )
        }
        item {
            // The IN-COLLEGE Section (The focus of the container)
            JourneyStageContainer(section = sampleSection)
        }
        item {
            // Simulate the After-College Section (as the last item)
            JourneyStageContainer(
                section = JourneyStageContainerData(
                    name = "AFTER-COLLEGE JOURNEY",
                    color = AfterCollegeColor,
                    stages = listOf(ImageCareerStage("Transition", 0, false, false, "Explore", AfterCollegeColor), ImageCareerStage("Growth", 0, false, false, "Explore", AfterCollegeColor))
                )
            )
        }
    }
}