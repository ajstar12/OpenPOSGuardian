package com.openpos.guardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.openpos.guardian.domain.model.ThemeMode
import com.openpos.guardian.domain.repository.KioskRepository
import com.openpos.guardian.domain.repository.SettingsRepository
import com.openpos.guardian.ui.MainScreen
import com.openpos.guardian.ui.theme.OpenPOSGuardianTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository


    @Inject
    lateinit var kioskRepository: KioskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val autoStartKioskExtra = intent?.getBooleanExtra(EXTRA_AUTO_START_KIOSK, false) ?: false

        lifecycleScope.launch {
            val settings = settingsRepository.getUserSettings().first()
            if (autoStartKioskExtra || settings.autoLockTaskOnLaunch) {
                kioskRepository.enterLockTaskMode(this@MainActivity)
            }
        }

        setContent {
            val settings by settingsRepository.getUserSettings().collectAsState(
                initial = com.openpos.guardian.domain.model.UserSettings()
            )

            OpenPOSGuardianTheme(themeMode = settings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(currentTheme = settings.themeMode)
                }
            }
        }
    }

    companion object {
        const val EXTRA_AUTO_START_KIOSK = "extra_auto_start_kiosk"
    }
}
