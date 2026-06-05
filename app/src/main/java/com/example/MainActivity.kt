package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.FitTrackTheme
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.MutedText
import com.example.ui.theme.OrangePrimary
import com.example.viewmodel.FitTrackViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: FitTrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge drawing
        enableEdgeToEdge()

        setContent {
            FitTrackTheme {
                // Main top layout
                var currentTab by remember { mutableStateOf(0) }
                
                // Reusable dialogue sheet state controls
                var showAddWeightDialog by remember { mutableStateOf(false) }
                var showAddPhotoDialog by remember { mutableStateOf(false) }

                val activeWorkoutSession by viewModel.activeProgram.collectAsState()
                val prCelebrationExercise by viewModel.recentPrBroken.collectAsState()
                val levelUpState by viewModel.recentlyLeveledUp.collectAsState()
                val userProfile by viewModel.userProfile.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize().background(DarkBg),
                    bottomBar = {
                        // Only show tab-bar if NO active workout session is running and user is onboarded
                        if (activeWorkoutSession == null && userProfile.isOnboarded) {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("app_bottom_bar")
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                containerColor = DeepBlack,
                                tonalElevation = 4.dp
                            ) {
                                val tabs = listOf(
                                    Triple(0, "Panel", Icons.Filled.Home),
                                    Triple(1, "Çalış", Icons.Filled.FitnessCenter),
                                    Triple(2, "Gelişim", Icons.Filled.TrendingUp),
                                    Triple(3, "Sosyal", Icons.Filled.People),
                                    Triple(4, "Analiz", Icons.Filled.BarChart),
                                    Triple(5, "Meydan", Icons.Filled.EmojiEvents),
                                    Triple(6, "Profil", Icons.Filled.Person)
                                )

                                tabs.forEach { (index, title, iconVector) ->
                                    val isSelected = currentTab == index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentTab = index },
                                        icon = {
                                            Icon(
                                                iconVector,
                                                contentDescription = title,
                                                tint = if (isSelected) OrangePrimary else MutedText,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                title,
                                                fontSize = 9.sp,
                                                color = if (isSelected) OrangePrimary else MutedText,
                                                maxLines = 1
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = OrangePrimary.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // CONDITIONAL ONBOARDING OR WORKOUT SESSION OVERLAY
                        if (!userProfile.isOnboarded) {
                            OnboardingScreen(
                                viewModel = viewModel,
                                onOnboardingComplete = {}
                            )
                        } else if (activeWorkoutSession != null) {
                            WorkoutSessionView(viewModel = viewModel)
                        } else {
                            // STANDARD NAVIGATION SCREENS
                            when (currentTab) {
                                0 -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToTab = { currentTab = it },
                                    onStartWorkout = { prog -> viewModel.startWorkoutSession(prog) },
                                    onOpenAddWeightDialog = { showAddWeightDialog = true },
                                    onOpenAddPhotoDialog = { showAddPhotoDialog = true }
                                )
                                1 -> WorkoutsScreen(
                                    viewModel = viewModel,
                                    onStartWorkout = { prog -> viewModel.startWorkoutSession(prog) }
                                )
                                2 -> ProgressScreen(
                                    viewModel = viewModel,
                                    onOpenAddWeightDialog = { showAddWeightDialog = true },
                                    onOpenAddPhotoDialog = { showAddPhotoDialog = true }
                                )
                                3 -> SocialScreen(viewModel = viewModel)
                                4 -> StatsScreen(viewModel = viewModel)
                                5 -> ChallengesScreen(viewModel = viewModel)
                                6 -> ProfileScreen(viewModel = viewModel)
                            }
                        }

                        // --- PR RECORD CELEBRATION MODAL OVERLAY ---
                        prCelebrationExercise?.let { exerciseName ->
                            PrCelebrationOverlay(
                                exerciseName = exerciseName,
                                onDismiss = { viewModel.clearPrAlert() }
                            )
                        }

                        // --- LEVEL UP CELEBRATION OVERLAY ---
                        levelUpState?.let { level ->
                            LevelUpOverlay(
                                level = level,
                                onDismiss = { viewModel.clearLevelUpAlert() }
                            )
                        }

                        // --- GLOBAL DIALOG SHEET OVERLAYS ---
                        if (showAddWeightDialog) {
                            AddWeightDialog(
                                onDismiss = { showAddWeightDialog = false },
                                onSave = { weight, notes ->
                                    viewModel.addWeightLog(weight, notes)
                                    showAddWeightDialog = false
                                }
                            )
                        }

                        if (showAddPhotoDialog) {
                            AddPhotoDialog(
                                onDismiss = { showAddPhotoDialog = false },
                                onSave = { pos, notes, avt ->
                                    viewModel.addProgressPhoto(pos, notes, avt)
                                    showAddPhotoDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
