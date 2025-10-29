@file:OptIn(ExperimentalAnimationApi::class)

package com.uworld.careerjourney.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------------------------------------------
// VIEWMODEL
// ---------------------------------------------
class AiChatViewModel : ViewModel() {

    var chatMessages by mutableStateOf(listOf<ChatMessage>())
        private set

    var isAiTyping by mutableStateOf(false)
        private set

    private val simulatedReplies = listOf(
        "Hello there! 👋 I’m your AI career assistant. How can I help today?",
        "That’s a great question! Based on your interest, I suggest focusing on internships that align with your target industry.",
        "Remember — consistency and small daily effort matter more than perfection. 🌱"
    )

    private var replyIndex = 0

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return
        chatMessages = chatMessages + _root_ide_package_.com.uworld.careerjourney.dashboard.ChatMessage(prompt, isUser = true)

        // Start simulated "typing" sequence
        viewModelScope.launch {
            delay(500)
            chatMessages = chatMessages + ChatMessage("", isUser = false, isTyping = true)
            delay(1000)
            simulateStreamingResponse()
        }
    }

    private fun simulateStreamingResponse() {
        val reply = simulatedReplies[replyIndex % simulatedReplies.size]
        replyIndex++
        viewModelScope.launch {
            // Remove typing indicator
            chatMessages = chatMessages.dropLast(1)
            // Simulate streaming text
            var displayed = ""
            for (char in reply) {
                displayed += char
                chatMessages = chatMessages + ChatMessage(displayed, isUser = false)
                delay(20)
                chatMessages = chatMessages.dropLast(1)
            }
            chatMessages = chatMessages + ChatMessage(displayed, isUser = false)
        }
    }

    // Example stream setup (mock)
    fun callStreamApi(prompt: String) {
        viewModelScope.launch {
            // Imagine a streaming JSON response where partial tokens come in
            val mockStream = listOf("Analyzing ", "your ", "career ", "path ", "... ✅")
            var buffer = ""
            chatMessages = chatMessages + ChatMessage("", isUser = false)
            for (token in mockStream) {
                buffer += token
                delay(250)
                chatMessages = chatMessages.dropLast(1) + ChatMessage(buffer, isUser = false)
            }
        }
    }
}

// ---------------------------------------------
// UI COMPOSABLE
// ---------------------------------------------
@Composable
fun AiChatScreen(viewModel: AiChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val chatMessages = viewModel.chatMessages
    var userInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFF0D1B2A),
        bottomBar = {
            ChatInputBar(
                userInput = userInput,
                onInputChange = { userInput = it },
                onSend = {
                    viewModel.sendMessage(userInput)
                    userInput = ""
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0D1B2A))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = false
            ) {
                items(chatMessages) { message ->
                    if (message.isTyping) {
                        TypingDots()
                    } else {
                        ChatBubble(message)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------
// Chat Bubble UI
// ---------------------------------------------
//@Composable
//fun ChatBubble(message: ChatMessage) {
//    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
//    val bubbleColor = if (message.isUser) Color(0xFF1E88E5) else Color(0xFF263238)
//
//    Box(
//        modifier = Modifier.fillMaxWidth(),
//        contentAlignment = alignment
//    ) {
//        Box(
//            modifier = Modifier
//                .background(
//                    bubbleColor,
//                    shape = RoundedCornerShape(16.dp)
//                )
//                .padding(12.dp)
//                .widthIn(max = 280.dp)
//        ) {
//            Text(
//                text = message.text,
//                color = Color.White,
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//    }
//}

// ---------------------------------------------
// Input Bar
// ---------------------------------------------
@Composable
fun ChatInputBar(
    userInput: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color(0xFF1B263B)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userInput,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF6C63FF)
                ),
                shape = RoundedCornerShape(24.dp),
                placeholder = { Text("Type your message...", color = Color.LightGray) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E88E5), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

// ---------------------------------------------
// Typing Dots Animation
// ---------------------------------------------
@Composable
fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf(alpha1, alpha2, alpha3).forEach {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(2.dp)
                    .background(Color.LightGray.copy(alpha = it), CircleShape)
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AiChatBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    viewModel: AiChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = LinearOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = FastOutLinearInEasing)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AI Career Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0072FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Chat messages
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true
                    ) {
                        items(viewModel.chatMessages.reversed()) { msg ->
                            ChatBubble(msg)
                        }

                        if (viewModel.isAiTyping) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask something...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F6FA),
                                unfocusedContainerColor = Color(0xFFF5F6FA),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0072FF)
                            ),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Text("↑", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------- CHAT BUBBLE --------------------

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .background(
                    if (isUser)
                        Brush.linearGradient(listOf(Color(0xFF0072FF), Color(0xFF00C6FF)))
                    else
                        Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isUser) Color.White else Color.Black,
                fontSize = 15.sp
            )
        }
    }
}

// -------------------- TYPING INDICATOR --------------------

@Composable
fun TypingIndicator() {
    val dotCount = 3
    val transition = rememberInfiniteTransition(label = "")
    val dotsAlpha = List(dotCount) { index ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    0.3f at (index * 200)
                    1f at (index * 200 + 400)
                },
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        dotsAlpha.forEach {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = it.value))
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}