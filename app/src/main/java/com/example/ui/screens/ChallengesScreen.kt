package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.FitTrackViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChallengesScreen(viewModel: FitTrackViewModel) {
    var activeCategoryTab by remember { mutableStateOf(0) } // 0: Günlük, 1: Haftalık, 2: Aylık & Efsanevi

    // Simulated in-memory reactive state for active challenge list progress!
    // This makes the screen highly interactive: clicking Join or Complete updates viewModel and gives XP!
    val challengesStore = remember {
        mutableStateListOf(
            ChallengeItem("c1", "Su Savaşçısı", "Günlük 3 Litre (3000ml) su hedefine ulaş.", "Günlük", 0.6f, "3000 ml", "1800/3000 ml", 50, Icons.Filled.WaterDrop, false),
            ChallengeItem("c2", "Sabah Koşucusu", "8.000 adım hedefini bugün tamamla.", "Günlük", 0.81f, "8000 Adım", "6542/8000 Adım", 60, Icons.Filled.DirectionsRun, false),
            ChallengeItem("c3", "Karın Yakıcı", "3 set plank yap (her biri en az 60 saniye).", "Günlük", 0.0f, "3 Set", "0/3 Set Completed", 40, Icons.Filled.Timer, false),
            
            ChallengeItem("c4", "Yüksek Yoğunluk", "Hafta boyunca 4 adet antrenman seansı bitir.", "Haftalık", 0.5f, "4 Seans", "2/4 Seans", 250, Icons.Filled.FitnessCenter, false),
            ChallengeItem("c5", "Tonaj Aşımı", "Haftalık toplam kaldırılan döküm dambıl ağırlığını 20.000 kg'a çıkar.", "Haftalık", 0.35f, "20 Ton", "7.000 / 20.000 kg", 350, Icons.Filled.TrendingUp, false),
            ChallengeItem("c6", "Kardiyo Canavarı", "Hafta içinde toplam 90 dakika HIIT veya koşu yap.", "Haftalık", 0.0f, "90 dk", "Henüz Başlanmadı", 200, Icons.Filled.Speed, false),

            ChallengeItem("c7", "Demir Disiplin", "Aralıksız 15 gün boyunca antrenman serini sürdür.", "Aylık", 0.46f, "15 Gün Seri", "7/15 Gün", 600, Icons.Filled.WorkspacePremium, false),
            ChallengeItem("c8", "Efsanevi Savaşçı", "Sırt veya göğüs odaklı hareketlerde kendi vücut ağırlığının 1.5 katını kaldır.", "Legendary", 0.1f, "1.5x Vücut Ağırlığı", "Limitlerde Zorlanıyor", 1200, Icons.Filled.Whatshot, false),
            ChallengeItem("c9", "Transformasyon Başlangıcı", "Profiline 3 adet farklı günlerde çekilmiş gelişim fotoğrafı ekle.", "Aylık", 0.33f, "3 Fotoğraf", "1/3 Fotoğraf", 450, Icons.Filled.PhotoLibrary, false)
        )
    }

    // Filter list based on selected category index
    val filteredChallenges = remember(activeCategoryTab, challengesStore) {
        when(activeCategoryTab) {
            0 -> challengesStore.filter { it.category == "Günlük" }
            1 -> challengesStore.filter { it.category == "Haftalık" }
            else -> challengesStore.filter { it.category == "Aylık" || it.category == "Legendary" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Challenges title header & XP boost card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Meydan Okuma Arenası",
                color = LightText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
            Text(
                "Limitleri zorla, klan puanı kazan ve rütbeni yükselt!",
                color = MutedText,
                fontSize = 12.sp
            )
        }

        // Tab category switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf("Günlük", "Haftalık", "Destansı & Aylık")
            tabs.forEachIndexed { index, label ->
                val isSelected = activeCategoryTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) OrangePrimary else Color.Transparent)
                        .clickable { activeCategoryTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else MutedText,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display challenges
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(filteredChallenges) { challenge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("challenge_card_${challenge.id}"),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (challenge.category == "Legendary") OrangeAccent.copy(alpha = 0.2f)
                                            else OrangePrimary.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        challenge.icon,
                                        contentDescription = null,
                                        tint = if (challenge.category == "Legendary") OrangeAccent else OrangePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        challenge.title,
                                        fontWeight = FontWeight.Bold,
                                        color = LightText,
                                        fontSize = 14.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (challenge.category == "Legendary") OrangeAccent.copy(alpha = 0.15f)
                                                    else SurfaceDark,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                challenge.category,
                                                color = if (challenge.category == "Legendary") OrangeAccent else OrangePrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            challenge.targetDesc,
                                            color = MutedText,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            // XP reward pill
                            Box(
                                modifier = Modifier
                                    .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "+${challenge.rewardXp} XP",
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            challenge.description,
                            color = MutedText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress section
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (challenge.isJoined) "Senin İlerlemen: ${challenge.progressText}"
                                    else "İlerleme Durumu: Kilitli",
                                    color = if (challenge.isJoined) LightText else MutedText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${(challenge.progressVal * 100).toInt()}%",
                                    color = OrangePrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(SurfaceDark, CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (challenge.isJoined) challenge.progressVal else 0.01f)
                                        .fillMaxHeight()
                                        .background(OrangePrimary, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons bottom drawer modifier!
                        if (!challenge.isJoined) {
                            Button(
                                onClick = {
                                    // Join Challenge!
                                    val idx = challengesStore.indexOf(challenge)
                                    if (idx != -1) {
                                        challengesStore[idx] = challenge.copy(isJoined = true)
                                        viewModel.addXp(20) // reward for committing to a challenge!
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = LightText),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Meydan Okumaya Katıl", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Complete Challenge immediately (simulation)
                                        val idx = challengesStore.indexOf(challenge)
                                        if (idx != -1) {
                                            challengesStore.removeAt(idx) // disappear or show complete!
                                            viewModel.addXp(challenge.rewardXp) // give full challenge XP reward!
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Gelişimi Gönder", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        // Opt out
                                        val idx = challengesStore.indexOf(challenge)
                                        if (idx != -1) {
                                            challengesStore[idx] = challenge.copy(isJoined = false)
                                        }
                                    },
                                    modifier = Modifier.height(38.dp),
                                    border = BorderStroke(1.dp, MutedText.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Ayrıl", color = MutedText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Entity helper
data class ChallengeItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val progressVal: Float,
    val targetDesc: String,
    val progressText: String,
    val rewardXp: Int,
    val icon: ImageVector,
    val isJoined: Boolean
)
