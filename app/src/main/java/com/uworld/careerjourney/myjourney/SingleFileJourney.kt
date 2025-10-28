//@file:OptIn(ExperimentalMaterial3Api::class)
//
//package com.uworld.careerjourney.myjourney
//
//import android.app.Application
//import android.os.Parcelable
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Chat
//import androidx.compose.material.icons.filled.ColorLens
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TextField
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.airbnb.lottie.compose.LottieAnimation
//import com.airbnb.lottie.compose.LottieCompositionSpec
//import com.airbnb.lottie.compose.LottieConstants
//import com.airbnb.lottie.compose.animateLottieCompositionAsState
//import com.airbnb.lottie.compose.rememberLottieComposition
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import androidx.datastore.preferences.core.*
//import androidx.datastore.preferences.preferencesDataStore
//
//
//// --------------------------------------------------
//// 🌟 DataStore Setup
//// --------------------------------------------------
//private val Application.chatDataStore by preferencesDataStore("chat_store")
//private val CHAT_KEY = stringPreferencesKey("chat_json")
//
//// ---------- VIEWMODEL ---------- //
//class JourneyViewModel(app: Application) : AndroidViewModel(app) {
//    var selectedJourney by mutableStateOf<CareerJourneyType?>(null)
//    var milestones = mutableStateListOf<CareerMilestone>()
//    var selectedCheckPoint by mutableStateOf<CareerMilestone?>(null)
//    var chatMessages = mutableStateListOf<Pair<String, Boolean>>() // (text, isUser)
//    var theme by mutableStateOf("Blue")
//    private val context = app
//
//    init {
//        viewModelScope.launch {
//            loadSampleData()
//            loadChatHistory()
//        }
//    }
//
//    private fun loadSampleData() {
//        milestones.clear()
//        milestones.addAll(
//            listOf(
//                CheckPoint(1, "High School Graduation", "2025-06-15"),
//                CheckPoint(2, "College Admission", "2025-08-01"),
//                CheckPoint(3, "First Internship", "2026-06-01"),
//                CheckPoint(4, "Final Year Project", "2028-01-10")
//            )
//        )
//    }
//
//    fun startJourney(type: JourneyType) {
//        selectedJourney = type
//        loadSampleData()
//    }
//
//    fun updateCheckPoint(milestone: CheckPoint, newTitle: String, newDate: String) {
//        milestone.title = newTitle
//        milestone.date = newDate
//    }
//
//    fun sendMessage(msg: String) {
//        chatMessages.add(msg to true)
//        saveChatHistory()
//    }
//
//    suspend fun simulateAIResponseWithTyping() {
//        chatMessages.add("typing..." to false)
//        delay(1200)
//        chatMessages.removeLastOrNull()
//
//        val responses = listOf(
//            "That’s a wonderful milestone! Keep going.",
//            "Excellent progress — want to discuss how to prepare for it?",
//            "Keep your curiosity alive; each step counts!",
//            "Remember, consistency is the key to mastery."
//        )
//        chatMessages.add(responses.random() to false)
//        saveChatHistory()
//    }
//
//    private suspend fun loadChatHistory() {
//        val prefs = context.chatDataStore.data.first()
//        prefs[CHAT_KEY]?.let { json ->
//            val list = Json.decodeFromString<List<ChatMessage>>(json)
//            chatMessages.clear()
//            chatMessages.addAll(list.map { it.text to it.isUser })
//        }
//    }
//
//    private fun saveChatHistory() {
//        viewModelScope.launch {
//            val list = chatMessages.map { ChatMessage(it.first, it.second) }
//            val json = Json.encodeToString(list)
//            context.chatDataStore.edit { it[CHAT_KEY] = json }
//        }
//    }
//}
//
//// ---------- MAIN APP UI ---------- //
//
//@Composable
//fun CareerJourneyApp(viewModel: JourneyViewModel = viewModel()) {
//    var showThemeDialog by remember { mutableStateOf(false) }
//    var showChatScreen by remember { mutableStateOf(false) }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Career Journey") },
//                actions = {
//                    IconButton(onClick = { showThemeDialog = true }) {
//                        Icon(Icons.Default.ColorLens, contentDescription = "Theme")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .background(Color.White)
//        ) {
//            when {
//                viewModel.selectedJourney == null -> JourneySelectionScreen { viewModel.startJourney(it) }
//                showChatScreen -> ChatScreen(viewModel) { showChatScreen = false }
//                else -> RoadmapScreen(
//                    viewModel,
//                    onCheckPointClick = { viewModel.selectedCheckPoint = it },
//                    onOpenChat = { showChatScreen = true }
//                )
//            }
//        }
//
//        if (showThemeDialog) ThemeDialog(viewModel) { showThemeDialog = false }
//
//        viewModel.selectedCheckPoint?.let {
//            CheckPointPopup(
//                milestone = it,
//                onDismiss = { viewModel.selectedCheckPoint = null }
//            ) { title, date ->
//                viewModel.updateCheckPoint(milestone = it, newTitle = title, newDate = date)
//            }
//        }
//    }
//}
//
//// ---------- SCREENS ---------- //
//
//@Composable
//fun JourneySelectionScreen(onSelect: (JourneyType) -> Unit) {
//    val options = listOf(
//        JourneyType.PreCollege to "Pre-College",
//        JourneyType.InCollege to "In-College",
//        JourneyType.PostCollege to "After-College"
//    )
//
//    Column(
//        Modifier
//            .fillMaxSize()
//            .padding(32.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            "Choose your journey",
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(Modifier.height(24.dp))
//
//        options.forEach { (type, label) ->
//            Button(
//                onClick = { onSelect(type) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//            ) { Text(label) }
//        }
//    }
//}
//
//@Composable
//fun RoadmapScreen(
//    viewModel: JourneyViewModel,
//    onCheckPointClick: (CheckPoint) -> Unit,
//    onOpenChat: () -> Unit
//) {
//    Column(
//        Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            "Career Roadmap",
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(Modifier.height(16.dp))
//
//        LazyColumn {
//            items(viewModel.milestones) { milestone ->
//                Card(
//                    Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                        .clickable { onCheckPointClick(milestone) },
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB))
//                ) {
//                    Column(Modifier.padding(16.dp)) {
//                        Text(milestone.title, fontWeight = FontWeight.Bold)
//                        Text("Target Date: ${milestone.date}", style = MaterialTheme.typography.bodyMedium)
//                    }
//                }
//            }
//        }
//
//        Spacer(Modifier.height(24.dp))
//        Button(onClick = onOpenChat, modifier = Modifier.align(Alignment.CenterHorizontally)) {
//            Icon(Icons.Default.Chat, null)
//            Spacer(Modifier.width(8.dp))
//            Text("Chat with AI Mentor")
//        }
//    }
//}
//
//@Composable
//fun ThemeDialog(viewModel: JourneyViewModel, onDismiss: () -> Unit) {
//    val themes = listOf("Blue", "Purple", "Teal", "Orange")
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Choose Theme") },
//        text = {
//            Column {
//                themes.forEach { theme ->
//                    Row(
//                        Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                viewModel.theme = theme
//                                onDismiss()
//                            }
//                            .padding(8.dp)
//                    ) {
//                        Box(
//                            Modifier
//                                .size(20.dp)
//                                .background(Color.Gray, shape = CircleShape)
//                        )
//                        Spacer(Modifier.width(8.dp))
//                        Text(theme)
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(onClick = onDismiss) { Text("Close") }
//        }
//    )
//}
//
//// ---------- CHAT SCREEN ---------- //
//
//@Composable
//fun ChatScreen(viewModel: JourneyViewModel, onBack: () -> Unit) {
//    val coroutine = rememberCoroutineScope()
//    var message by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
//
//    // Lottie animation for typing dots
//    val typingComposition by rememberLottieComposition(
//        LottieCompositionSpec.Url("https://lottie.host/b4951571-d1a2-4e56-82c0-bb1cc478aa65/nURd70bqG1.json")
//    )
//    val typingProgress by animateLottieCompositionAsState(
//        composition = typingComposition,
//        iterations = LottieConstants.IterateForever
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("AI Mentor") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, null)
//                    }
//                }
//            )
//        },
//        bottomBar = {
//            // Input bar
//            Row(
//                Modifier
//                    .fillMaxWidth()
//                    .background(Color(0xFFF5F5F5))
//                    .padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                BasicTextField(
//                    value = message,
//                    onValueChange = { message = it },
//                    modifier = Modifier
//                        .weight(1f)
//                        .background(Color.White, shape = MaterialTheme.shapes.small)
//                        .padding(12.dp)
//                )
//                Spacer(Modifier.width(8.dp))
//                IconButton(onClick = {
//                    if (message.text.isNotBlank()) {
//                        val msg = message.text
//                        viewModel.sendMessage(msg)
//                        message = TextFieldValue("")
//                        coroutine.launch {
//                            viewModel.simulateAIResponseWithTyping()
//                        }
//                    }
//                }) {
//                    Icon(Icons.Default.Send, null)
//                }
//            }
//        }
//    ) { padding ->
//        // Chat bubble list
//        LazyColumn(
//            Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .padding(8.dp)
//        ) {
//            items(viewModel.chatMessages) { (text, isUser) ->
//                Box(
//                    Modifier
//                        .fillMaxWidth()
//                        .padding(4.dp),
//                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
//                ) {
//                    if (text == "typing...") {
//                        Surface(
//                            color = Color(0xFFEEEEEE),
//                            shape = MaterialTheme.shapes.medium
//                        ) {
//                            LottieAnimation(
//                                composition = typingComposition,
//                                progress = { typingProgress },
//                                modifier = Modifier
//                                    .size(60.dp)
//                                    .padding(4.dp)
//                            )
//                        }
//                    } else {
//                        Surface(
//                            color = if (isUser) Color(0xFF2196F3) else Color(0xFFEEEEEE),
//                            shape = MaterialTheme.shapes.medium
//                        ) {
//                            Text(
//                                text,
//                                Modifier.padding(12.dp),
//                                color = if (isUser) Color.White else Color.Black
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ---------- MILESTONE POPUP WITH DYNAMIC LOTTIE ---------- //
//
//fun getLottieForCheckPoint(title: String): String {
//    val normalized = title.lowercase()
//    return when {
//        "graduation" in normalized || "school" in normalized -> "https://lottie.host/1e29f08f-30d7-47ee-9f80-5b0c4d0f6b10/M0fQ7UZ5gX.json"
//        "college" in normalized || "admission" in normalized -> "https://lottie.host/4a74a5b0-f1b7-42e4-85e9-b165a8903c74/Xh7G5vUjGm.json"
//        "intern" in normalized -> "https://lottie.host/92adcb3b-0575-42b2-8b4b-2e3edb841dbe/kR6VLG07lW.json"
//        "project" in normalized -> "https://lottie.host/ef332e15-0f1e-4d69-83dc-cc205d8a13ef/zxGZKfjYF1.json"
//        else -> "https://lottie.host/37b3777a-39e9-4b6b-9abf-476f42b7d657/GVteLZ4H0D.json"
//    }
//}
//
//@Composable
//fun CheckPointPopup(
//    milestone: CheckPoint,
//    onDismiss: () -> Unit,
//    onSave: (String, String) -> Unit
//) {
//    var title by remember { mutableStateOf(milestone.title) }
//    var date by remember { mutableStateOf(milestone.date) }
//    var saving by remember { mutableStateOf(false) }
//
//    val composition by rememberLottieComposition(LottieCompositionSpec.Url(getLottieForCheckPoint(milestone.title)))
//    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text("Edit CheckPoint")
//                Spacer(Modifier.width(8.dp))
//                LottieAnimation(
//                    composition = composition,
//                    progress = { progress },
//                    modifier = Modifier.size(60.dp)
//                )
//            }
//        },
//        text = {
//            Column {
//                TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
//                Spacer(Modifier.height(8.dp))
//                TextField(value = date, onValueChange = { date = it }, label = { Text("Date (yyyy-mm-dd)") })
//            }
//        },
//        confirmButton = {
//            Button(onClick = {
//                saving = true
//                onSave(title, date)
//                onDismiss()
//            }) {
//                if (saving) {
//                    CircularProgressIndicator(
//                        color = Color.White,
//                        modifier = Modifier
//                            .size(16.dp)
//                            .padding(end = 8.dp),
//                        strokeWidth = 2.dp
//                    )
//                }
//                Text("Save")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) { Text("Cancel") }
//        }
//    )
//}
