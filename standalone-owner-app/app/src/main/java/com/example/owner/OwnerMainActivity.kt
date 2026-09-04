package com.example.owner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.MainActivity
import com.example.owner.ui.OwnerAppShell
import com.example.owner.ui.components.OwnerThemeColors
import com.example.ui.theme.AcademiaTrackTheme
import com.example.ui.viewmodel.SchoolViewModel

/**
 * Dedicated Entry Point Activity for the Platform Owner's App Module.
 * Implements a separate executive UI shell with isolated task affinity ("com.example.owner")
 * for managing global application settings, multi-tenant school account provisioning,
 * and remote access control.
 */
class OwnerMainActivity : ComponentActivity() {

    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AcademiaTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = OwnerThemeColors.BackgroundDark
                ) {
                    OwnerAppShell(
                        viewModel = viewModel,
                        onSwitchToSchoolApp = {
                            val intent = Intent(this@OwnerMainActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
