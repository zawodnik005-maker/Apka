import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Inicjalizacja DataStore
val Context.dataStore by preferencesDataStore(name = "settings")
val LANG_KEY = stringPreferencesKey("app_language")

enum class AppLanguage(val label: String, val title: String) {
    ENGLISH("English", "AI Arena Mobile"),
    DEUTSCHE("Deutsche", "AI Arena Mobil"),
    POLSKI("Polski", "AI Arena Mobilna")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            
            // Odczyt zapisanego języka (domyślnie ENGLISH)
            val savedLangName by context.dataStore.data
                .map { it[LANG_KEY] ?: AppLanguage.ENGLISH.name }
                .collectAsState(initial = AppLanguage.ENGLISH.name)

            val currentLang = AppLanguage.valueOf(savedLangName)

            MaterialTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    SmallTopAppBar(
                        title = { Text(currentLang.title) },
                        actions = {
                            LanguageSelector { newLang ->
                                // Zapisywanie nowego wyboru
                                scope.launch {
                                    context.dataStore.edit { it[LANG_KEY] = newLang.name }
                                }
                            }
                        }
                    )
                    LMArenaWebView("https://lmarena.ai")
                }
            }
        }
    }
}

@Composable
fun LanguageSelector(onLangSelected: (AppLanguage) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Text("🌐")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLanguage.values().forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.label) },
                    onClick = {
                        onLangSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LMArenaWebView(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    }, modifier = Modifier.fillMaxSize())
}
