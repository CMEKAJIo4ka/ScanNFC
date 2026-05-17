package com.example.scannfc.ui.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scannfc.ui.theme.CardColor
import com.example.scannfc.ui.theme.DarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val scanStatus by viewModel.scanStatus.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(scanStatus) {
        when (scanStatus) {
            is ScanStatus.Success -> {
                Toast.makeText(context, "Считано: ${(scanStatus as ScanStatus.Success).tagContent}", Toast.LENGTH_SHORT).show()
            }
            is ScanStatus.Error -> {
                Toast.makeText(context, (scanStatus as ScanStatus.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Scanner NFC", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (scanStatus is ScanStatus.Loading) "Сохранение..." else "Готов к сканированию",
                            fontSize = 12.sp, 
                            color = if (scanStatus is ScanStatus.Loading) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(CardColor, CircleShape)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = CardColor,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Icon(
                            Icons.Default.Home, 
                            contentDescription = null, 
                            tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Text("Главная", fontSize = 10.sp, color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Gray)
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .offset(y = (-20).dp)
                            .shadow(8.dp, CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clickable { /* Логика добавления */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 2 }
                    ) {
                        Icon(
                            Icons.Default.History, 
                            contentDescription = null, 
                            tint = if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Text("История", fontSize = 10.sp, color = if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CardColor.copy(alpha = 0.3f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .background(CardColor.copy(alpha = 0.6f), CircleShape)
                    )
                    
                    if (scanStatus is ScanStatus.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            modifier = Modifier.size(90.dp),
                            tint = if (scanStatus is ScanStatus.Success) Color.Green else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = when (scanStatus) {
                        is ScanStatus.Success -> "Метка считана!"
                        is ScanStatus.Error -> "Ошибка сканирования"
                        is ScanStatus.Loading -> "Обработка..."
                        else -> "Поднесите NFC-метку"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Text(
                    text = when (scanStatus) {
                        is ScanStatus.Success -> (scanStatus as ScanStatus.Success).tagContent
                        is ScanStatus.Error -> (scanStatus as ScanStatus.Error).message
                        else -> "Приложение определит информацию\nавтоматически"
                    },
                    fontSize = 18.sp,
                    color = if (scanStatus is ScanStatus.Success) MaterialTheme.colorScheme.primary else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, start = 32.dp, end = 32.dp),
                    lineHeight = 24.sp
                )
            }
        }
    }
}
