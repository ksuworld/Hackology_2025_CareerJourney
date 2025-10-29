package com.uworld.careerjourney.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// --- 1. DATA MODELS AND VIEWMODEL ---

enum class MessageRole { USER, AI }

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val role: MessageRole,
    val isTyping: Boolean = false // Only for AI's current message
)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow(
        listOf(
            Message(text = "Hello! I'm here to help you plan your career journey.", role = MessageRole.AI),
            Message(text = "What is the next step for a student completing Junior Year?", role = MessageRole.USER)
        )
    )
    val messages: StateFlow<List<Message>> = _messages

    private val mockResponses = mapOf(
        "junior" to "The main milestone after Junior Year is the **Senior Launch** phase, which includes finalizing your job search strategy and applying for full-time roles or graduate school. Your summer internship should guide this process!",
        "hello" to "I'm a career assistant designed to guide you through the Pre-College, In-College, and After-College phases.",
        "default" to "That's a great question! For custom milestones, try using the 'Add Custom Milestone' button at the bottom of the checklist."
    )

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add User Message
        _messages.value = _messages.value + Message(text = text.trim(), role = MessageRole.USER)

        // 2. Start AI Typing Simulation
        viewModelScope.launch {
            // Add a placeholder message to display the typing indicator
            val typingMessageId = UUID.randomUUID().toString()
            _messages.value = _messages.value + Message(id = typingMessageId, text = "", role = MessageRole.AI, isTyping = true)

            // Determine response based on user input
            val userLower = text.lowercase()
            val fullResponse = when {
                userLower.contains("junior") -> mockResponses["junior"]!!
                userLower.contains("hello") -> mockResponses["hello"]!!
                else -> mockResponses["default"]!!
            }

            // Simulate typing and update the message
            simulateTyping(typingMessageId, fullResponse)
        }
    }

    private fun simulateTyping(messageId: String, fullResponse: String) {
        viewModelScope.launch {
            var typedText = ""
            for (char in fullResponse) {
                typedText += char
                // Find the placeholder message and update its text
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(text = typedText)
                    } else {
                        msg
                    }
                }
                delay(30) // Typing speed (30ms per character)
            }

            // Final message update: remove the typing state flag
            _messages.value = _messages.value.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(isTyping = false)
                } else {
                    msg
                }
            }
        }
    }
}

// --- 2. UI COMPOSABLES ---

@Composable
fun GeminiChatScreen(viewModel: ChatViewModel = ChatViewModel()) {
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    // Scroll to the latest message whenever the list updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        bottomBar = { ChatInput(onSend = viewModel::sendMessage) }
    ) { paddingValues ->
        // Main Message Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isUser) Alignment.End else Alignment.Start

    // Custom entry animation for AI messages to smooth the typing indicator transition
    val textContent = if (message.isTyping && message.text.isEmpty()) "•" else message.text

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp, // Flat bottom for AI
                bottomEnd = if (isUser) 4.dp else 16.dp    // Flat bottom for user
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(modifier = Modifier.padding(10.dp).widthIn(max = 280.dp)) {

                if (message.isTyping) {
                    // Typing Indicator (Simulated)
                    Text(
                        text = if (message.text.isEmpty()) "•" else "...", // Simple pulsing effect can be added here
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                } else {
                    // Standard Message Text
                    Text(textContent, color = textColor, fontSize = 16.sp)
                }
            }
        }
    }
}


@Composable
fun ChatInput(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    // Custom design mimicking the persistent input field (bottom sheet)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Surface color for the "sheet"
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Input Field
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Ask about your career journey...") },
                modifier = Modifier.weight(1f),
                singleLine = false,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )

            // Send Button
            IconButton(
                onClick = {
                    onSend(text)
                    text = "" // Clear input after sending
                },
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.LightGray)
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Send Message",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// --- 3. PREVIEW ---

@Preview(showBackground = true)
@Composable
fun GeminiChatScreenPreview() {
    // Note: In a real app, the ViewModel would be provided by a ViewModelProvider.
    // Here, we create an instance for the preview.
    MaterialTheme {
        GeminiChatScreen()
    }
}