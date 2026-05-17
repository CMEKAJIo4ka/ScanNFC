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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scannfc.models.ScanRecord
import com.example.scannfc.ui.theme.CardColor
import com.example.scannfc.ui.theme.DarkBackground
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showWriteDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    
    val scanStatus by viewModel.scanStatus.collectAsState()
    val history by viewModel.history.collectAsState()
    val isWritingMode by viewModel.isWritingMode.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(selectedTab) {
        if (selectedTab == 2) viewModel.loadHistory()
    }

    LaunchedEffect(scanStatus) {
        when (scanStatus) {
            is ScanStatus.Success -> {
                Toast.makeText(context, "Считано: ${(scanStatus as ScanStatus.Success).tagContent}", Toast.LENGTH_SHORT).show()
            }
            is ScanStatus.WriteSuccess -> {
                Toast.makeText(context, "Метка успешно записана!", Toast.LENGTH_LONG).show()
                viewModel.resetStatus()
            }
            is ScanStatus.Error -> {
                Toast.makeText(context, (scanStatus as ScanStatus.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    if (showWriteDialog) {
        AlertDialog(
            onDismissRequest = { showWriteDialog = false },
            containerColor = CardColor,
            title = { Text("Запись на метку", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Информация (кабинет, предмет)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.prepareToWrite(textInput)
                    showWriteDialog = false
                    selectedTab = 0
                }) { Text("Начать запись") }
            },
            dismissButton = {
                TextButton(onClick = { showWriteDialog = false }) { Text("Отмена") }
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
                            text = if (isWritingMode) "РЕЖИМ ЗАПИСИ" else "Готов к работе",
                            fontSize = 12.sp, 
                            color = if (isWritingMode) MaterialTheme.colorScheme.primary else Color.Gray
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
                        Icon(Icons.Default.Home, contentDescription = null, tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Gray)
                        Text("Главная", fontSize = 10.sp, color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Gray)
                    }

                    Box(
                        modifier = Modifier.size(60.dp).offset(y = (-20).dp).shadow(8.dp, CircleShape)
                            .background(brush = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)), shape = CircleShape)
                            .clickable { showWriteDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { selectedTab = 2 }) {
                        Icon(Icons.Default.History, contentDescription = null, tint = if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Gray)
                        Text("История", fontSize = 10.sp, color = if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (selectedTab == 0) {
                ScanContent(scanStatus, isWritingMode)
            } else {
                HistoryContent(history)
            }
        }
    }
}

@Composable
fun ScanContent(status: ScanStatus, isWriting: Boolean) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.fillMaxSize().background(CardColor.copy(alpha = 0.3f), CircleShape))
            Box(modifier = Modifier.size(170.dp).background(CardColor.copy(alpha = 0.6f), CircleShape))
            
            if (status is ScanStatus.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = when {
                        isWriting -> Color.Yellow
                        status is ScanStatus.Success -> Color.Green
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = when {
                isWriting -> "Ожидание метки..."
                status is ScanStatus.Success -> "Метка считана!"
                status is ScanStatus.Error -> "Ошибка"
                else -> "Поднесите NFC-метку"
            },
            fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color.White
        )
        
        Text(
            text = when {
                isWriting -> "Приложите метку для записи данных"
                status is ScanStatus.Success -> (status as ScanStatus.Success).tagContent
                status is ScanStatus.Error -> (status as ScanStatus.Error).message
                else -> "Информация определится автоматически"
            },
            fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, start = 32.dp, end = 32.dp)
        )
    }
}

@Composable
fun HistoryContent(history: List<ScanRecord>) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("История пуста", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { record ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardColor), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = record.tagContent, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                            Text(text = formatTimestamp(record.timestamp), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(text = "Tag: ${record.tagId}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("ru"))
    return sdf.format(timestamp.toDate())
}
