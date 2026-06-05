package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.viewmodel.FitTrackViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: FitTrackViewModel,
    onOnboardingComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    
    // Onboarding form state
    var selectedGender by remember { mutableStateOf("Erkek") }
    var ageValue by remember { mutableStateOf("25") }
    var heightValue by remember { mutableStateOf("175") }
    var weightValue by remember { mutableStateOf("75.0") }
    var goalWeightValue by remember { mutableStateOf("70.0") }
    var selectedFitnessLevel by remember { mutableStateOf("Orta") }
    var selectedGoal by remember { mutableStateOf("Kas Kazanımı") }
    var selectedFrequency by remember { mutableStateOf(3) }
    var selectedEquipment by remember { mutableStateOf("Tüm Salon") }
    var userName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        topBar = {
            if (step in 2..10) {
                TopAppBar(
                    title = {
                        Text(
                            "Adım $step / 10",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { if (step > 1) step-- },
                            modifier = Modifier.testTag("onboarding_prev_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = LightText
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack),
                    windowInsets = WindowInsets.safeDrawing
                )
            }
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() with
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                }
            ) { targetStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    when (targetStep) {
                        1 -> WelcomeStep(
                            name = userName,
                            onNameChange = { userName = it },
                            onNext = { step++ }
                        )
                        2 -> GenderStep(
                            selectedGender = selectedGender,
                            onGenderSelect = { selectedGender = it },
                            onNext = { step++ }
                        )
                        3 -> AgeStep(
                            age = ageValue,
                            onAgeChange = { ageValue = it },
                            onNext = { step++ }
                        )
                        4 -> HeightStep(
                            height = heightValue,
                            onHeightChange = { heightValue = it },
                            onNext = { step++ }
                        )
                        5 -> WeightStep(
                            weight = weightValue,
                            onWeightChange = { weightValue = it },
                            onNext = { step++ }
                        )
                        6 -> GoalWeightStep(
                            goalWeight = goalWeightValue,
                            onGoalWeightChange = { goalWeightValue = it },
                            onNext = { step++ }
                        )
                        7 -> FitnessLevelStep(
                            selectedLevel = selectedFitnessLevel,
                            onLevelSelect = { selectedFitnessLevel = it },
                            onNext = { step++ }
                        )
                        8 -> GoalStep(
                            selectedGoal = selectedGoal,
                            onGoalSelect = { selectedGoal = it },
                            onNext = { step++ }
                        )
                        9 -> FrequencyStep(
                            selectedFrequency = selectedFrequency,
                            onFrequencySelect = { selectedFrequency = it },
                            onNext = { step++ }
                        )
                        10 -> EquipmentStep(
                            selectedEquipment = selectedEquipment,
                            onEquipmentSelect = { selectedEquipment = it },
                            onNext = { step++ }
                        )
                        11 -> LoadingStep(
                            userName = userName.ifEmpty { "Sporcu" },
                            gender = selectedGender,
                            age = ageValue.toIntOrNull() ?: 25,
                            height = heightValue.toDoubleOrNull() ?: 175.0,
                            weight = weightValue.toDoubleOrNull() ?: 75.0,
                            goalWeight = goalWeightValue.toDoubleOrNull() ?: 70.0,
                            fitnessLevel = selectedFitnessLevel,
                            goal = selectedGoal,
                            frequency = selectedFrequency,
                            equipment = selectedEquipment,
                            viewModel = viewModel,
                            onComplete = onOnboardingComplete
                        )
                    }

                    // Bottom progress indicator dots for intermediate steps
                    if (targetStep in 2..10) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            for (i in 2..10) {
                                Box(
                                    modifier = Modifier
                                        .size(if (targetStep == i) 14.dp else 8.dp, 8.dp)
                                        .clip(CircleShape)
                                        .background(if (targetStep == i) OrangePrimary else CardBg)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(OrangePrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "FITTRACK PRO",
                color = OrangePrimary,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Mükemmel Vücuda Giden En Akıllı Yol",
                color = MutedText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Adınız veya Takma Adınız:",
                        color = LightText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = { Text("Örn: Yusuf Demir", color = MutedText) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText,
                            focusedIndicatorColor = OrangePrimary,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .testTag("onboarding_name_input"),
                        singleLine = true
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_welcome_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Başlayalım!", fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
        }
    }
}

@Composable
fun GenderStep(
    selectedGender: String,
    onGenderSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Cinsiyetiniz Nedir?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Bazı kalori ya da güç endeksleri hesaplamalarında önemlidir.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val genders = listOf(
                    Triple("Erkek", "Erkek", Icons.Filled.Male),
                    Triple("Kadın", "Kadın", Icons.Filled.Female),
                    Triple("Belirtmek İstemiyorum", "Diğer / Sakla", Icons.Filled.Transgender)
                )

                genders.forEach { (genderKey, label, icon) ->
                    val isSelected = selectedGender == genderKey
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OrangePrimary.copy(alpha = 0.15f) else CardBg
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OrangePrimary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGenderSelect(genderKey) }
                            .testTag("gender_card_$genderKey")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(18.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isSelected) OrangePrimary else SurfaceDark,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else OrangePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                label,
                                color = if (isSelected) OrangePrimary else LightText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_gender_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun AgeStep(
    age: String,
    onAgeChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Kaç Yaşındasınız?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Metabolizma hızınızı ve antrenman performansınızı optimize edeceğiz.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(CardBg, CircleShape)
                    .border(2.dp, OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = age,
                    onValueChange = { if (it.length <= 3) onAgeChange(it) },
                    textStyle = LocalTextStyle.current.copy(
                        color = LightText,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(100.dp)
                        .testTag("onboarding_age_input")
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Yıl", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Button(
            onClick = onNext,
            enabled = age.isNotEmpty() && (age.toIntOrNull() ?: 0) > 0,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_age_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun HeightStep(
    height: String,
    onHeightChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Boyunuz Kaç?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Vücut kitle indeksinizi hesaplamada kullanılır.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(CardBg, CircleShape)
                    .border(2.dp, OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = height,
                    onValueChange = { if (it.length <= 3) onHeightChange(it) },
                    textStyle = LocalTextStyle.current.copy(
                        color = LightText,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(100.dp)
                        .testTag("onboarding_height_input")
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Santimetre (cm)", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Button(
            onClick = onNext,
            enabled = height.isNotEmpty() && (height.toIntOrNull() ?: 0) > 50,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_height_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun WeightStep(
    weight: String,
    onWeightChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Mevcut Kilonuz Nedir?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Başlangıç kilonuz baz alınarak tüm gelişiminiz raporlanacaktır.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(CardBg, CircleShape)
                    .border(2.dp, OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = LightText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        .testTag("onboarding_weight_input")
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kilogram (kg)", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Button(
            onClick = onNext,
            enabled = weight.isNotEmpty() && (weight.toDoubleOrNull() ?: 0.0) > 20.0,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_weight_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun GoalWeightStep(
    goalWeight: String,
    onGoalWeightChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Hedef Kilonuz Nedir?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sizi bu hedefe en güvenli ve hızlı şekilde ulaştıracağız.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(CardBg, CircleShape)
                    .border(2.dp, OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = goalWeight,
                    onValueChange = onGoalWeightChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = LightText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        .testTag("onboarding_goal_weight_input")
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hedef Kilogram (kg)", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Button(
            onClick = onNext,
            enabled = goalWeight.isNotEmpty() && (goalWeight.toDoubleOrNull() ?: 0.0) > 20.0,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_goal_weight_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun FitnessLevelStep(
    selectedLevel: String,
    onLevelSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Mevcut Kondisyonunuz Nasıl?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sakatlanmaları önlemek için antrenman hacim katsayınızı ayarlar.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val levels = listOf(
                    "Başlangıç" to "Spora yeni başladım veya uzun süre ara verdim.",
                    "Orta" to "Son 6 aydır düzenli ve bilinçli spor yapıyorum.",
                    "İleri" to "Yıllardır demir kaldırıyorum, limitlerimi biliyorum."
                )

                levels.forEach { (level, desc) ->
                    val isSelected = selectedLevel == level
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OrangePrimary.copy(alpha = 0.15f) else CardBg
                        ),
                        border = BorderStroke(1.dp, if (isSelected) OrangePrimary else Color.Transparent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLevelSelect(level) }
                            .testTag("level_card_$level")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                level,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) OrangePrimary else LightText,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                desc,
                                color = MutedText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_level_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun GoalStep(
    selectedGoal: String,
    onGoalSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Birinci Hedefiniz Nedir?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tüm egzersiz önerileri ve beslenme hedefleri bu hedefe kitlenecektir.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val goals = listOf(
                    "Kas Kazanımı" to "Hipertrofi odaklı antrenmanlar ve yüksek protein.",
                    "Yağ Yakımı" to "Ağırlık + kardiyo hibrit programlama ve kalori açığı.",
                    "Güç Artışı" to "Düşük tekrar, yüksek yoğunluklu temel powerlifting.",
                    "Genel Fitness" to "Zindelik, kondisyon ve kardiyovasküler dayanıklılık."
                )

                goals.forEach { (goal, desc) ->
                    val isSelected = selectedGoal == goal
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OrangePrimary.copy(alpha = 0.15f) else CardBg
                        ),
                        border = BorderStroke(1.dp, if (isSelected) OrangePrimary else Color.Transparent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGoalSelect(goal) }
                            .testTag("goal_card_$goal")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                goal,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) OrangePrimary else LightText,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                desc,
                                color = MutedText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_goal_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun FrequencyStep(
    selectedFrequency: Int,
    onFrequencySelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Haftada Kaç Gün Antrenman?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Haftalık plan şablonlarınızı bölmelere ayıracağız.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..7) {
                    val isSelected = selectedFrequency == i
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) OrangePrimary else CardBg)
                            .border(1.dp, if (isSelected) OrangePrimary else SurfaceDark, CircleShape)
                            .clickable { onFrequencySelect(i) }
                            .testTag("frequency_btn_$i"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = i.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else LightText,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Haftada $selectedFrequency Gün", color = OrangePrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_frequency_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun EquipmentStep(
    selectedEquipment: String,
    onEquipmentSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Hangi Ekipmanlara Sahipsiniz?",
                color = LightText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Mevcut donanımınıza göre makine veya dumbell egzersizlerini süzecektir.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val equipmentList = listOf(
                    "Tüm Salon" to "Barlar, dambıllar ve her türlü makineye erişimim var.",
                    "Ev Salonu" to "Güç sehpası, ayarlanabilir dumbeller ve direnç bantları.",
                    "Sadece Dambıl" to "Yalnızca bir çift dambıl ile evde ağırlık kaldırabilirim.",
                    "Sadece Vücut Ağırlığı" to "Herhangi bir ekipmana ihtiyacım yok, mobil çalışıyorum."
                )

                equipmentList.forEach { (equip, desc) ->
                    val isSelected = selectedEquipment == equip
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OrangePrimary.copy(alpha = 0.15f) else CardBg
                        ),
                        border = BorderStroke(1.dp, if (isSelected) OrangePrimary else Color.Transparent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEquipmentSelect(equip) }
                            .testTag("equipment_card_$equip")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                equip,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) OrangePrimary else LightText,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                desc,
                                color = MutedText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_equipment_next"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Haritalandırmayı Tamamla!", fontWeight = FontWeight.Black, fontSize = 15.sp)
        }
    }
}

@Composable
fun LoadingStep(
    userName: String,
    gender: String,
    age: Int,
    height: Double,
    weight: Double,
    goalWeight: Double,
    fitnessLevel: String,
    goal: String,
    frequency: Int,
    equipment: String,
    viewModel: FitTrackViewModel,
    onComplete: () -> Unit
) {
    var progressText by remember { mutableStateOf("Kişiselleştirilmiş hedefler hesaplanıyor...") }
    var percentage by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        delay(800)
        percentage = 0.35f
        progressText = "Antrenman programları optimize ediliyor..."
        delay(1000)
        percentage = 0.7f
        progressText = "Limitler ve makro hedefler belirleniyor..."
        delay(1200)
        percentage = 1.0f
        progressText = "Savaşçı profili oluşturuldu!"
        delay(800)

        // Save profile
        viewModel.updateProfile(
            UserProfile(
                id = 1,
                name = userName,
                height = height,
                weight = weight,
                age = age,
                goal = goal,
                isKg = true,
                isDarkTheme = true,
                notificationsEnabled = true,
                isOnboarded = true
            )
        )
        // Add personalized hydration & steps goals based on onboarding factors
        viewModel.setWaterTarget(if (gender == "Erkek") 3000 else 2400)
        viewModel.setStepTarget(if (fitnessLevel == "İleri") 12000 else 8000)

        onComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = OrangePrimary,
            strokeWidth = 6.dp,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "MÜKEMMELLİK İNŞA EDİLİYOR...",
            color = OrangePrimary,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Yolculuğunuz kurgulanıyor, lütfen bekleyin",
            color = LightText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(CardBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .background(OrangeAccent)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            progressText,
            color = MutedText,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}
