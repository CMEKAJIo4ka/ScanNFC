package com.example.scannfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scannfc.ui.auth.LoginScreen
import com.example.scannfc.ui.auth.RegisterScreen
import com.example.scannfc.ui.main.MainScreen
import com.example.scannfc.ui.main.MainViewModel
import com.example.scannfc.ui.theme.ScanNFCTheme
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var sharedMainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC не поддерживается", Toast.LENGTH_LONG).show()
        }

        setContent {
            ScanNFCTheme {
                val mViewModel: MainViewModel = viewModel()
                sharedMainViewModel = mViewModel
                
                AppNavigation(mViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    private fun setupForegroundDispatch() {
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                try {
                    addDataType("*/*")
                } catch (e: Exception) {
                    Log.e("NFC", "Filter error", e)
                }
            },
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || 
            NfcAdapter.ACTION_NDEF_DISCOVERED == action || 
            NfcAdapter.ACTION_TECH_DISCOVERED == action) {
            
            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            
            tag?.let {
                val tagId = it.id.joinToString(":") { byte -> "%02X".format(byte) }
                val tagContent = readNdefContent(it) ?: "Пустая метка"
                
                Log.d("NFC_SCAN", "Считана метка: $tagId, Контент: $tagContent")
                sharedMainViewModel?.onTagScanned(tagId, tagContent)
            }
        }
    }

    private fun readNdefContent(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()
            
            ndefMessage?.records?.firstOrNull()?.let { record ->
                val payload = record.payload
                if (payload.isNotEmpty()) {
                    val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                    val languageCodeLength = payload[0].toInt() and 63
                    String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charset.forName(textEncoding))
                } else null
            }
        } catch (e: Exception) {
            Log.e("NFC", "Read error", e)
            null
        }
    }
}

@Composable
fun AppNavigation(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { 
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onRegisterSuccess = { navController.navigate("login") }
            )
        }
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onLogout = { 
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}
