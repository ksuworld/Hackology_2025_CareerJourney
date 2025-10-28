package com.uworld.careerjourney.sample_journey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uworld.careerjourney.dashboard.CareerCompassApp
import com.uworld.careerjourney.dashboard.CareerTimelineScreen
import com.uworld.careerjourney.dashboard.DashboardScreen

class OnBoardingActivity : ComponentActivity() {

    val showTabLayout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UWorldRoadmapTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    var tabIndex by remember { mutableIntStateOf(0) }

                    NavHost(navController = navController, startDestination = "select") {
                        composable("select") {
                            Column(
                                modifier = Modifier.background(MaterialTheme.colorScheme.background)
                            ) {
                                if (showTabLayout) {
                                    TabRow(selectedTabIndex = tabIndex) {
                                        val tabs = listOf("1", "2", "3")
                                        tabs.forEachIndexed { index, title ->
                                            Tab(
                                                selected = index == 0,
                                                onClick = { tabIndex = index },
                                                text = { Text(title) }
                                            )
                                        }
                                    }
                                }

                                when (tabIndex) {
                                    0 -> {
                                        CareerCompassApp()
//                                        DashboardScreen(onNavigateToMilestone = { journey ->
//                                            navController.navigate("roadmap/$journey")
//                                        })
                                    }

                                    1 -> CareerTimelineScreen(onJourneySelected = { journey ->
                                        navController.navigate("roadmap/$journey")
                                    })

                                    else -> JourneySelectionScreen(onJourneySelected = { journey ->
                                        navController.navigate("roadmap/$journey")
                                    })
                                }
                            }
                        }

                        composable("roadmap/{journey}") { backStackEntry ->
                            val journey = backStackEntry.arguments?.getString("journey") ?: "Pre-College"
                            RoadmapScreen(journey = journey, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMain() {
    UWorldRoadmapTheme {
        // Not navigating in preview
    }
}
