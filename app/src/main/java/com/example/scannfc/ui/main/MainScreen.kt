package com.example.scannfc.ui.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.scannfc.models.ScanRecord
import com.example.scannfc.ui.theme.CardColor
import com.example.scannfc.ui.theme.DarkBackground
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showActionChoice by remember { mutableStateOf(false) }
    var showWriteInput by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    
    val scanStatus by viewModel.scanStatus.collectAsState()
    val history by viewModel.history.collectAsState()
    val isWritingMode by viewModel.isWritingMode.collectAsState()
    val isDeleteMode by viewModel.isDeleteMode.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(selectedTab) {
        if (selectedTab == 2) viewModel.loadHistory()
    }

    LaunchedEffect(scanStatus) {
        when (scanStatus) {
            is ScanStatus.Success -> {
                val content = (scanStatus as ScanStatus.Success).tagContent.clean()
                if (!isImageUrl(content)) {
                    Toast.makeText(context, "Считано: $content", Toast.LENGTH_SHORT).show()
                }
                delay(15000) // Увеличил время показа результата
                viewModel.resetStatus()
            }
            is ScanStatus.WriteSuccess -> {
                Toast.makeText(context, "Метка успешно записана!", Toast.LENGTH_LONG).show()
                delay(3000)
                viewModel.resetStatus()
            }
            is ScanStatus.DeleteSuccess -> {
                Toast.makeText(context, "Данные с метки удалены", Toast.LENGTH_LONG).show()
                delay(3000)
                viewModel.resetStatus()
            }
            is ScanStatus.Error -> {
                Toast.makeText(context, (scanStatus as ScanStatus.Error).message, Toast.LENGTH_LONG).show()
                delay(3000)
                viewModel.resetStatus()
            }
            else -> {}
        }
    }

    if (showActionChoice) {
        AlertDialog(
            onDismissRequest = { showActionChoice = false },
            containerColor = CardColor,
            title = { Text("Выберите действие", color = Color.White) },
            text = {
                Column {
                    Button(onClick = { showActionChoice = false; showWriteInput = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Записать информацию")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { showActionChoice = false; viewModel.prepareToDelete(); selectedTab = 0 }, modifier = Modifier.fillMaxWidth()) {
                        Text("Очистить метку")
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showWriteInput) {
        AlertDialog(
            onDismissRequest = { showWriteInput = false },
            containerColor = CardColor,
            title = { Text("Что записать?", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Текст или URL") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.prepareToWrite(textInput.trim())
                    showWriteInput = false
                    selectedTab = 0
                }) { Text("Записать") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Scanner NFC", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (isWritingMode) "РЕЖИМ ЗАПИСИ" else if (isDeleteMode) "РЕЖИМ ОЧИСТКИ" else "Готов к работе",
                            fontSize = 12.sp,
                            color = if (isWritingMode || isDeleteMode) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                color = CardColor,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { selectedTab = 0 }) {
                        Icon(Icons.Default.Home, null, tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Gray)
                        Text("Главная", fontSize = 10.sp, color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                    Box(
                        modifier = Modifier.size(60.dp).offset(y = (-20).dp).shadow(8.dp, CircleShape)
                            .background(brush = androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)), shape = CircleShape)
                            .clickable { showActionChoice = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { selectedTab = 2 }) {
                        Icon(Icons.Default.History, null, tint = if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Gray)
                        Text("История", fontSize = 10.sp, color = if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (selectedTab == 0) {
                ScanContent(
                    status = scanStatus, 
                    isBusy = isWritingMode || isDeleteMode,
                    onCancel = { viewModel.resetStatus() }
                )
            } else {
                HistoryContent(history)
            }
        }
    }
}

@Composable
fun ScanContent(status: ScanStatus, isBusy: Boolean, onCancel: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val content = if (status is ScanStatus.Success) status.tagContent.clean() else ""
        val isImg = isImageUrl(content)
        val isLink = content.startsWith("http")

        Box(
            modifier = Modifier.size(if (isImg) 280.dp else 240.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isImg) {
                Box(modifier = Modifier.fillMaxSize().background(CardColor.copy(alpha = 0.3f), CircleShape))
                Box(modifier = Modifier.size(190.dp).background(CardColor.copy(alpha = 0.6f), CircleShape))
            }
            
            when {
                status is ScanStatus.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                status is ScanStatus.Success -> {
                    if (isImg) {
                        SubcomposeAsyncImage(
                            model = content,
                            contentDescription = "Image from NFC",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop,
                            loading = { 
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            error = { Icon(Icons.Default.Nfc, null, modifier = Modifier.size(90.dp), tint = Color.Green) }
                        )
                    } else {
                        Icon(Icons.Default.Nfc, null, modifier = Modifier.size(90.dp), tint = Color.Green)
                    }
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        modifier = Modifier.size(90.dp),
                        tint = when {
                            isBusy -> Color.Yellow
                            status is ScanStatus.Error -> Color.Red
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = when {
                isBusy -> "Ожидание метки..."
                status is ScanStatus.Success -> "Метка считана!"
                status is ScanStatus.Error -> "Ошибка"
                else -> "Поднесите NFC-метку"
            },
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White
        )
        
        Text(
            text = when {
                isBusy -> "Приложите метку к телефону"
                status is ScanStatus.Success -> content
                status is ScanStatus.Error -> (status as ScanStatus.Error).message
                else -> "Информация определится автоматически"
            },
            fontSize = 16.sp, 
            color = if (isLink && status is ScanStatus.Success) MaterialTheme.colorScheme.primary else Color.Gray, 
            textAlign = TextAlign.Center,
            textDecoration = if (isLink && status is ScanStatus.Success) TextDecoration.Underline else null,
            modifier = Modifier
                .padding(top = 12.dp, start = 32.dp, end = 32.dp)
                .clickable(enabled = isLink && status is ScanStatus.Success) {
                    try { uriHandler.openUri(content) } catch (e: Exception) {}
                },
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )

        if (isBusy) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
            ) {
                Text("Отмена")
            }
        }
    }
}

@Composable
fun HistoryContent(history: List<ScanRecord>) {
    val uriHandler = LocalUriHandler.current
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("История пуста", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(history) { record ->
                val content = record.tagContent.clean()
                val isImg = isLikelyImage(content)
                val isLink = content.startsWith("http")
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = content,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLink) MaterialTheme.colorScheme.primary else Color.White,
                                textDecoration = if (isLink) TextDecoration.Underline else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = isLink) {
                                        try { uriHandler.openUri(content) } catch (e: Exception) {}
                                    },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatTimestamp(record.timestamp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        if (isImg) {
                            Spacer(modifier = Modifier.height(12.dp))
                            SubcomposeAsyncImage(
                                model = content,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { try { uriHandler.openUri(content) } catch (e: Exception) {} },
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(Modifier.fillMaxSize().background(Color.DarkGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                },
                                error = {
                                    Box(Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Nfc, null, tint = Color.Gray)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ID: ${record.tagId}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

fun String.clean(): String = this.replace(Regex("[\\p{C}]"), "").trim()

fun isImageUrl(url: String): Boolean {
    return url.clean().startsWith("http", ignoreCase = true)
}

fun isLikelyImage(url: String): Boolean {
    val cleanUrl = url.clean().lowercase()
    if (!cleanUrl.startsWith("http")) return false
    val extensions = listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    val patterns = listOf("firebasestorage", "images", "img", "photo", "avatar", "picsum", "cloudinary", "static")
    return extensions.any { cleanUrl.contains(it) } || patterns.any { cleanUrl.contains(it) }
}

fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("ru"))
    return sdf.format(timestamp.toDate())
}
