package com.uworld.careerjourney.old

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.uworld.careerjourney.ui.theme.CareerJourneyTheme

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContent {
//            CareerJourneyTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) {  innerPadding ->
////                    RoadmapScreen1()
////                    RoadMapScreen2()
////                    RoadmapScreen3()
////                    RoadMapScreen4()
////                    RoadMapScreen5()
////                    RoadMapScreen6()
////                    RoadMapScreen7()
////                    RoadMapScreen8()
////                    RoadMapScreen9()
////                    RoadMapScreen10()
//                    RoadMapScreen11()
//                }
//            }
//        }

        setContent {
            CareerJourneyTheme {
                var selectedScreenIndex by remember { mutableIntStateOf(1) } // Default to RoadMapScreen11
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            arrayOfNulls<Int>(1).forEachIndexed { idx, screen ->
                                NavigationBarItem(
                                    selected = selectedScreenIndex == idx,
                                    onClick = { selectedScreenIndex = idx },
                                    icon = { },
                                    label = { Text("" + (idx + 1)) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
//                        CustomRoadmapScreen()
//                        CustomMapBackground()
//                        CustomInteractiveMap()
//                        when (selectedScreenIndex) {
////                            0 -> RoadMapScreen1()
////                            1 -> RoadMapScreen2()
////                            2 -> RoadMapScreen3()
////                            3 -> RoadMapScreen4()
////                            4 -> RoadMapScreen5()
////                            5 -> RoadMapScreen6()
////                            6 -> RoadMapScreen7()
////                            7 -> RoadMapScreen8()
////                            8 -> RoadMapScreen9()
////                            9 -> RoadMapScreen10()
////                            10 -> RoadMapScreen11()
////                            11 -> RoadMapScreen12()
////                            12 -> RoadMapScreen13()
////                            13 -> GeminiMap1()
////                            14 -> GeminiMap2()
////                            15 -> GeminiMap3()
//                            16 -> CustomRoadmapScreen()
//                            else -> RoadMapScreen1()
//                        }
                    }
                }
            }
        }
    }
}