package com.example.scannfc.ui.main

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scannfc.ui.theme.CardColor
import com.example.scannfc.ui.theme.DarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Scanner NFC", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Готов к сканированию", fontSize = 12.sp, color = Color.Gray)
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
            // Кастомный Bottom Bar с закруглением
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
                    // Кнопка Главная
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

                    // Центральная кнопка Добавить (+)
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

                    // Кнопка История
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
                // Декоративный эффект вокруг иконки NFC
                Box(
                    modifier = Modifier
                        .size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Внешний приглушенный круг
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CardColor.copy(alpha = 0.3f), CircleShape)
                    )
                    // Внутренний круг поярче
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .background(CardColor.copy(alpha = 0.6f), CircleShape)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        modifier = Modifier.size(90.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Поднесите NFC-метку",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Text(
                    text = "Приложение определит информацию\nавтоматически",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, start = 32.dp, end = 32.dp),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
