package jp.co.studio.kaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import jp.co.studio.kaka.ui.navigation.LunaRoot
import jp.co.studio.kaka.ui.theme.LunaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LunaTheme {
                LunaRoot()
            }
        }
    }
}
