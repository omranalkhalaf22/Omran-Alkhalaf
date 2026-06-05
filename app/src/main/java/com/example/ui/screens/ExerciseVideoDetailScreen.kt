package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.ui.theme.*
import com.example.viewmodel.FitTrackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseVideoDetailView(
    exercise: Exercise,
    viewModel: FitTrackViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ViewModel configurations
    val isBookmarked by viewModel.bookmarkedExerciseIds.collectAsState()
    val isFav = isBookmarked.contains(exercise.id)

    val customPlaylists by viewModel.customPlaylists.collectAsState()
    val downloadsMap by viewModel.downloadedExerciseIds.collectAsState()
    val isDownloaded = downloadsMap.containsKey(exercise.id)
    val downloadedQuality = downloadsMap[exercise.id]

    val beginnerMode by viewModel.beginnerModeEnabled.collectAsState()
    val advancedMode by viewModel.advancedModeEnabled.collectAsState()

    // Local Video Engine states
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) } // 0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x
    var playProgress by remember { mutableStateOf(0.0f) } // 0.0 to 1.0
    var videoActiveTab by remember { mutableStateOf(0) } // 0: Looping Preview, 1: Full Tutorial, 2: Slow-Mo Replay
    var isFullScreen by remember { mutableStateOf(false) }

    // Interactivity: Step Highlight lock (if user wants to lock simulated position to Step 1, 2, 3, or 4)
    var lockedStepIndex by remember { mutableStateOf<Int?>(null) }

    // Simulated downloads state indicators
    var downloadProgress by remember { mutableStateOf<Float?>(null) } // null means idle, 0.0-1.0 means active
    var showQualitySelector by remember { mutableStateOf(false) }
    var showPlaylistSelector by remember { mutableStateOf(false) }

    // Playback Speed selection dropdown
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Synchronize play progress in real-time
    LaunchedEffect(isPlaying, playbackSpeed, videoActiveTab, lockedStepIndex) {
        if (lockedStepIndex != null) {
            playProgress = when (lockedStepIndex) {
                0 -> 0.12f // Start
                1 -> 0.38f // Movement
                2 -> 0.62f // Peak
                else -> 0.88f // Return
            }
            isPlaying = false
        } else if (isPlaying) {
            // Speed factor: 1.0x -> 40ms intervals, 2.0x -> 20ms, 0.5x -> 80ms
            var delayMs = (40 / playbackSpeed).toLong().coerceIn(10L, 200L)
            if (videoActiveTab == 2) {
                delayMs = (delayMs * 2) // Slow down for Slow-Mo tab
            }

            while (isPlaying) {
                delay(delayMs)
                playProgress = (playProgress + 0.015f)
                if (playProgress > 1.0f) {
                    playProgress = 0.0f
                }
            }
        }
    }

    // Connect active step calculation and biomechanics highlighting to playback
    val currentStepIndex = remember(playProgress) {
        when {
            playProgress < 0.25f -> 0 // Starting Position
            playProgress < 0.50f -> 1 // Movement Execution
            playProgress < 0.75f -> 2 // Peak Contraction
            else -> 3                 // Controlled Return
        }
    }

    // Determine muscle highlight intensity based on play progress
    val tensionMultiplier by animateFloatAsState(
        targetValue = if (isPlaying) {
            // Peak tension is around peak contraction (Play progress 0.5 to 0.7)
            if (playProgress in 0.45f..0.7f) 1.0f else 0.4f
        } else {
            if (currentStepIndex == 2) 1.0f else 0.4f
        },
        animationSpec = tween(150),
        label = "muscle_tension"
    )

    // Body Overlay Layout: Dialog full size or BottomSheet
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Premium Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(SurfaceDark, CircleShape)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri Git", tint = LightText)
                    }
                    Column {
                        Text(
                            text = exercise.name,
                            color = LightText,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    exercise.muscleGroup,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                "HD 1080p • Masterclass",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Header Bookmark and Playlist actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleBookmark(exercise.id) },
                        modifier = Modifier.background(SurfaceDark, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Favorilere Ekle",
                            tint = if (isFav) OrangePrimary else MutedText
                        )
                    }

                    IconButton(
                        onClick = { showPlaylistSelector = true },
                        modifier = Modifier.background(SurfaceDark, CircleShape)
                    ) {
                        Icon(Icons.Outlined.PlaylistAdd, contentDescription = "Oynatma Listesine Ekle", tint = LightText)
                    }
                }
            }

            Divider(color = SurfaceDark)

            // MAIN CONTENT SCROLLABLE
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {

                // 1. VIDEO SIMULATOR PLAYER FRAME
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Media Tab choices (Looping Preview, Full Course Tutorial, Slow-Mo Replay)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(10.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val mediaModes = listOf("Önizleme Döngüsü", "Eğitim Kursu", "Ağır Çekim")
                            mediaModes.forEachIndexed { idx, label ->
                                val active = videoActiveTab == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) OrangePrimary else Color.Transparent)
                                        .clickable {
                                            videoActiveTab = idx
                                            if (idx == 2) playbackSpeed = 0.5f else playbackSpeed = 1.0f
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.Black else MutedText,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Immersive Masterclass Video Frame Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.77f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, SurfaceDark, RoundedCornerShape(16.dp))
                                .background(CardBg)
                        ) {
                            // Animated exercise representation on beautiful grid
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Dynamic background mesh
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val gridCount = 8
                                    val width = size.width
                                    val height = size.height
                                    
                                    // Grid lines
                                    for (i in 1 until gridCount) {
                                        val x = (width / gridCount) * i
                                        val y = (height / gridCount) * i
                                        drawLine(Color.White.copy(alpha = 0.04f), Offset(x, 0f), Offset(x, height))
                                        drawLine(Color.White.copy(alpha = 0.04f), Offset(0f, y), Offset(width, y))
                                    }
                                }

                                // Interactive Exercise Animation Loader
                                SimulatedExerciseVisualizer(
                                    exerciseId = exercise.id,
                                    progress = playProgress,
                                    tension = tensionMultiplier
                                )

                                // Active Muscle Overlay Indicator (Glowing diagram embedded)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .size(75.dp)
                                        .background(SurfaceDark.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                        .border(0.5.dp, OrangePrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .padding(4.dp)
                                ) {
                                    MiniGlowAnatomyModel(
                                        muscleGroup = exercise.muscleGroup,
                                        intensity = tensionMultiplier
                                    )
                                }

                                // Interactive "MUTED" or "SLOW-MOTION" Banner Text
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isMuted) {
                                        Box(
                                            modifier = Modifier
                                                .background(AccentError.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text("MUTED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (videoActiveTab == 1) OrangeAccent.copy(alpha = 0.8f) else OrangePrimary.copy(alpha = 0.8f),
                                                RoundedCornerShape(6.dp)
                                            )
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        val label = when(videoActiveTab) {
                                            1 -> "TUTORIAL"
                                            2 -> "SLOW-MO"
                                            else -> "LOOPING"
                                        }
                                        Text(label, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // VIDEO HUD CONTROLS OVERLAY
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                        )
                                    )
                                    .padding(8.dp)
                            ) {
                                // Progress Slider
                                Slider(
                                    value = playProgress,
                                    onValueChange = {
                                        lockedStepIndex = null // Break locked step
                                        playProgress = it
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = OrangePrimary,
                                        activeTrackColor = OrangePrimary,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Play/Pause/Restore
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                lockedStepIndex = null
                                                isPlaying = !isPlaying
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                lockedStepIndex = null
                                                playProgress = 0.0f
                                                isPlaying = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.Replay, contentDescription = "Yeniden Oynat", tint = LightText, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = { isMuted = !isMuted },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                                contentDescription = "Sesi Kapat/Aç",
                                                tint = LightText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Right: Speed and FullScreen toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Speed button
                                        Box {
                                            TextButton(
                                                onClick = { showSpeedMenu = true },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("${playbackSpeed}x", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }

                                            DropdownMenu(
                                                expanded = showSpeedMenu,
                                                onDismissRequest = { showSpeedMenu = false },
                                                modifier = Modifier.background(CardBg)
                                            ) {
                                                val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                                                speeds.forEach { spd ->
                                                    DropdownMenuItem(
                                                        text = { Text("${spd}x", color = LightText, fontSize = 12.sp) },
                                                        onClick = {
                                                            playbackSpeed = spd
                                                            showSpeedMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { isFullScreen = !isFullScreen },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                                contentDescription = "Tam Ekran",
                                                tint = LightText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. TECHNIQUE BREAKDOWN STEPPER ROW
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Teknik Aşama Analizi",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Her aşamayı ayrıntısı ile incelemek için üzerine dokunun ve dondurun.",
                            color = MutedText,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 4 segment custom stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val stages = listOf(
                                "1. Başlangıç",
                                "2. Yürütme",
                                "3. Tepe Kasılma",
                                "4. Kontrollü Dönüş"
                            )
                            stages.forEachIndexed { idx, stageLabel ->
                                val isActive = currentStepIndex == idx
                                val isLocked = lockedStepIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isLocked) OrangeAccent
                                            else if (isActive) OrangePrimary.copy(alpha = 0.2f)
                                            else CardBg
                                        )
                                        .border(
                                            1.dp,
                                            if (isLocked) OrangeAccent
                                            else if (isActive) OrangePrimary
                                            else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            lockedStepIndex = if (lockedStepIndex == idx) null else idx
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            stageLabel.split(" ").first(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = if (isLocked) Color.Black else if (isActive) OrangePrimary else LightText
                                        )
                                        Text(
                                            stageLabel.split(" ").drop(1).joinToString(" "),
                                            fontSize = 8.sp,
                                            color = if (isLocked) Color.Black else MutedText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Display active breakdown tip
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(OrangePrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${currentStepIndex + 1}",
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }

                                val stageTitleAndDesc = when (currentStepIndex) {
                                    0 -> Pair("Başlangıç Pozisyonu Kurulumu", "Ayaklarınızı yere sabitleyin ve omurganızı nötr pozisyona getirerek dengeyi kurun.")
                                    1 -> Pair("Konsantrik Kasılma & İtme/Çekme", "Hedef kas grubunu kasarken nefes verin ve eklemleri zorlamadan hareketi tamamlayın.")
                                    2 -> Pair("Maksimum Tepe Sıkıştırma", "Hareketi zirvede 1 saniye tutun. Bu aşamada hedef kaslar %100 yük altındadır.")
                                    else -> Pair("Eksantrik Gerilimli Kontrollü Bırakış", "Ağırlığı direnç göstererek, vücudun yer çekimi ivmesine yavaşça karşı koymasını sağlayın.")
                                }

                                Column {
                                    Text(stageTitleAndDesc.first, color = LightText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(stageTitleAndDesc.second, color = MutedText, fontSize = 11.sp, lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }

                // 3. OFFLINE VIDEO DOWNLOAD MANAGEMENT
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isDownloaded) Icons.Filled.CloudDone else Icons.Outlined.CloudDownload,
                                        contentDescription = null,
                                        tint = if (isDownloaded) AccentSuccess else OrangePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            if (isDownloaded) "Kurs Videosu İndirildi (Çevrimdışı Aktif)"
                                            else "Çevrimdışı Kurs İndirme",
                                            fontWeight = FontWeight.Bold,
                                            color = LightText,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            if (isDownloaded) "Ayar: $downloadedQuality Kalite • Temizlemek için dokunun."
                                            else "İnternetiniz yokken de antrenmanda videoları kesintisiz izleyin.",
                                            color = MutedText,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Interactive simulator download trigger
                                if (downloadProgress != null) {
                                    CircularProgressIndicator(
                                        progress = { downloadProgress ?: 0f },
                                        color = OrangePrimary,
                                        trackColor = CardBg,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    if (isDownloaded) {
                                        IconButton(
                                            onClick = { viewModel.deleteOfflineVideo(exercise.id) }
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Yedeklemeyi Kaldır", tint = AccentError)
                                        }
                                    } else {
                                        Button(
                                            onClick = { showQualitySelector = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Kalite Seç", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Quality selection list modal-like slider
                        if (showQualitySelector) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(CardBg, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text("Yayın Kalitesini Seçin", color = LightText, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                val options = listOf(
                                    Triple("HD Ultra (1080p)", "28.5 MB", "1080p"),
                                    Triple("SD Medium (720p)", "14.2 MB", "720p"),
                                    Triple("Veri Tasarrufu (480p)", "6.8 MB", "480p")
                                )
                                options.forEach { (label, size, code) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showQualitySelector = false
                                                // Trigger fake loading
                                                scope.launch {
                                                    downloadProgress = 0.0f
                                                    while (downloadProgress!! < 1.0f) {
                                                        delay(300)
                                                        downloadProgress = (downloadProgress ?: 0f) + 0.25f
                                                    }
                                                    viewModel.saveVideoOffline(exercise.id, code)
                                                    downloadProgress = null
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Filled.PhotoSizeSelectLarge, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                                            Text(label, color = LightText, fontSize = 12.sp)
                                        }
                                        Text(size, color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. BEGINNER MODE VS ADVANCED MODE SELECTION
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(10.dp))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (beginnerMode) OrangePrimary else Color.Transparent)
                                    .clickable { viewModel.setBeginnerMode(true) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.School,
                                        contentDescription = null,
                                        tint = if (beginnerMode) Color.Black else MutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "NİCE BAŞLANGIÇ MODU",
                                        fontWeight = FontWeight.Bold,
                                        color = if (beginnerMode) Color.Black else MutedText,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (advancedMode) OrangeAccent else Color.Transparent)
                                    .clickable { viewModel.setAdvancedMode(true) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.FlashOn,
                                        contentDescription = null,
                                        tint = if (advancedMode) Color.Black else MutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "İLERİ SEVİYE PROFESYONEL",
                                        fontWeight = FontWeight.Bold,
                                        color = if (advancedMode) Color.Black else MutedText,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Guidance card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (beginnerMode) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.Stars, contentDescription = null, tint = OrangePrimary)
                                        Text("Acemi Ekstra Rehberliği", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Setup cue
                                    GuidanceDetailRow("Adım Adım Kurulum", "Görsel hizalama rehberine bakarak omuzlarınızı ve ayaklarınızı bar hizanıza getirin.", Icons.Filled.SettingsInputHdmi)
                                    Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

                                    // Setup breathing
                                    val breathExText = when {
                                        exercise.muscleGroup.lowercase().contains("göğüs") || exercise.muscleGroup.lowercase().contains("pres") ->
                                            "Barı göğsünüze indirirken derin nefes alarak göğüs kafesini açın. Yukarı doğru iterken kontrollü nefes verin."
                                        exercise.muscleGroup.lowercase().contains("bacak") ->
                                            "Çömelirken (Squat) karnınızı şişirerek nefesinizi tutun (Valsalva manevrası). Yukarı itişte nefesinizi dışarı üfleyin."
                                        else -> "Ağırlığı çekerken kalbinizi sakinleştirip nefesinizi tamamen boşaltın, geri salarken çiçeği koklar gibi yavaşça içinize havayı çekin."
                                    }
                                    GuidanceDetailRow("Doğru Solunum Formu", breathExText, Icons.Filled.Air)
                                    Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

                                    // Form warning
                                    GuidanceDetailRow("Form İşaret Cümleleri", "Aynada kendinizi kontrol edin, acele etmeyin! Kas zihnini oluşturarak egzersiz başına en az 4 saniye harcayın.", Icons.Filled.Info)

                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = OrangeAccent)
                                        Text("Profesyonel Performans Parametreleri", color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Tempo
                                    val tempoVal = when {
                                        exercise.muscleGroup.lowercase().contains("omuz") -> "3 - 1 - 1 - 0 (3sn Eksantrik, 1sn zirvede tepe sıkıştırma)"
                                        exercise.muscleGroup.lowercase().contains("bacak") -> "4 - 0 - 1 - 1 (Yavaş derinleşme, patlayıcı kalkış)"
                                        else -> "3 - 0 - 1 - 0 (Kontrollü eccentric, kontrollü concentric)"
                                    }
                                    GuidanceDetailRow("Hız & Tempo Önerisi (TUT)", tempoVal, Icons.Filled.Timer)
                                    Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

                                    // RPE / RIR
                                    val rpeRirVal = when (exercise.difficulty.lowercase()) {
                                        "zor" -> "RPE 8.5 - 9.0 • RIR 1 (Tükenmeye son derece yakın, sınırları test eden pres!)"
                                        "orta" -> "RPE 7.5 - 8.0 • RIR 2 (Maksimum hipertrofi eşiği)"
                                        else -> "RPE 6 // RIR 3-4 (Akış kontrolü, ısınma aşaması)"
                                    }
                                    GuidanceDetailRow("RPE & RIR Değerlemesi", rpeRirVal, Icons.Filled.Leaderboard)
                                    Divider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

                                    // Advanced notes
                                    GuidanceDetailRow("Yoğunluk Teknik Notu", "Son setin ardından 15 saniye dinlenip tükenişe gitmek için Drop-Set veya Myo-Reps protokolü uygulayabilirsiniz.", Icons.Filled.LocalActivity)
                                }
                            }
                        }
                    }
                }

                // 5. BODY MUSCLE VISUALIZATION DETAILS (Glow and layout)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Kas Aktivasyon Örtüsü",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(SurfaceDark, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DetailedAnatomyDrawing(
                                        muscleGroup = exercise.muscleGroup,
                                        pulseIntensity = tensionMultiplier
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val (pPrimary, pSecondary) = when (exercise.muscleGroup.lowercase()) {
                                        "göğüs" -> Pair("Pectoralis Major (Büyük Göğüs Kası)", "Anterior Deltoid (Ön Omuz), Triceps Brachii (Arka Kol)")
                                        "sırt" -> Pair("Latissimus Dorsi (Kanat), Rhomboids", "Biceps Brachii, Arka Omuz (Rear Deltoid)")
                                        "omuz" -> Pair("Lateral Deltoid (Yan Omuz), Anterior Deltoid", "Triceps, Upper Trapezius (Üst Trapez)")
                                        "kol" -> Pair("Biceps Brachii (Ön Kol), Triceps Brachii", "Brachialis, Forearm Flexors (Bilek)")
                                        "bacak" -> Pair("Quadriceps Femoris (Ön Bacak), Gluteus Maximus", "Hamstrings (Sürekli Gerim), Calves (Kalf)")
                                        "karın" -> Pair("Rectus Abdominis (Six-Pack)", "Obliques (Yan Karın), Transversus Abdominis")
                                        else -> Pair("Cardiovascular Heart Muscle (Kalp Kondisyonu)", "Legs Endurance, Lungs Performance (Akciğer Katılımı)")
                                    }

                                    // Primary Muscle indicator
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.size(8.dp).background(OrangePrimary, CircleShape))
                                        Text("Birincil Hedef: $pPrimary", color = LightText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }

                                    // Secondary Muscle indicator
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.size(8.dp).background(OrangeAccent, CircleShape))
                                        Text("İkincil Yardımcılar: $pSecondary", color = MutedText, fontSize = 11.sp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                                .background(OrangePrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Görsel simulasyon aktif kasılma anında turuncu parlar.", color = OrangePrimary, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. COMMON MISTAKES SECTION
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "⚠️ Yaygın Yapılan Hatalar",
                            color = AccentError,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val mistakesList = remember(exercise.id) {
                            when {
                                exercise.id.contains("bench_press") -> listOf(
                                    Pair("Beli Aşırı Sehpaya Eğmek", "Beli yay gibi gerip aşırı yükü sakatlık limitlerine çekmek."),
                                    Pair("Barı Göğüsten Sektirmek", "Hız kesmeden barı sertçe fırlatarak göğüs kafesine darbe vurmak.")
                                )
                                exercise.id.contains("squat") -> listOf(
                                    Pair("Dizlerin İçe Bükülmesi (Valgus)", "Ağırlığı kaldırırken diz kapaklarının birbirine yaklaşması."),
                                    Pair("Eksik Derinlik", "Kalçayı paralel seviyenin üzerinde tutarak bacak uyarımını kısmak.")
                                )
                                exercise.id.contains("deadlift") -> listOf(
                                    Pair("Sırtı Yuvarlamak (Kamburlaşma)", "Çekiş esnasında beli korumayıp omurlara devasa disk basısı eklemek."),
                                    Pair("Geriye Aşırı Yaslanmak", "Yukarı kilitlendiğinde diskleri ezerek geriye fazlaca bükülmek.")
                                )
                                else -> listOf(
                                    Pair("Aşırı Hızlı Momentum Kullanımı", "Gövdeyi sallayarak kas gücü hariç tüm ivmeden destek almak."),
                                    Pair("Yarım Hareket Açısı (ROM)", "Eklemleri tam esnetip tam germeden kısa açıda sıkıştırmak.")
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            mistakesList.forEach { (mistakeName, mistakeDesc) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBg),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(AccentError.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Warning, contentDescription = null, tint = AccentError, modifier = Modifier.size(16.dp))
                                        }

                                        Column {
                                            Text(mistakeName, color = LightText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(mistakeDesc, color = MutedText, fontSize = 11.sp, lineHeight = 15.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. COACHING TIPS & SAFETY WARNINGS
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "💡 Koçluk İpuçları & Güvenlik Önlemleri",
                            color = AccentSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Coach notes layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Coaching notes
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = AccentSuccess, modifier = Modifier.size(16.dp))
                                        Text("Teknik Taktikler", color = AccentSuccess, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        exercise.tips,
                                        color = LightText,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            // Safety Warning notes
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Filled.Security, contentDescription = null, tint = AccentError, modifier = Modifier.size(16.dp))
                                        Text("Kritik Uyarı", color = AccentError, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val safeText = when {
                                        exercise.id.contains("squat") -> "Geri çömelirken diz kapaklarınızı aşırı kilitlemeyin. Omurgaya korseleme yapın."
                                        exercise.id.contains("deadlift") -> "Beli bükmeyin. Çekiş bittiğinde aşırı geri bükülmek fıtık riskidir!"
                                        else -> "Eklemlerinizi tepe noktada zınk diye kilitlemekten kaçının, gerilimi kasta tutun!"
                                    }
                                    Text(
                                        safeText,
                                        color = LightText,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. EXERCISE HISTORY & STATISTICS
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Geçmiş Performans & İlgili Veriler",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Last weight completions
                                HistoryStatColumn(
                                    label = "Son Ağırlık",
                                    value = "55.0 kg",
                                    icon = Icons.Filled.FitnessCenter
                                )

                                HistoryStatColumn(
                                    label = "Son Tekrar",
                                    value = "10 Tekrar",
                                    icon = Icons.Filled.FormatListNumbered
                                )

                                val prVal = when(exercise.id) {
                                    "bench_press" -> "60 kg"
                                    "squat" -> "80 kg"
                                    "deadlift" -> "100 kg"
                                    "overhead_press" -> "40 kg"
                                    else -> "Yok"
                                }
                                HistoryStatColumn(
                                    label = "Rekor (PR)",
                                    value = prVal,
                                    icon = Icons.Filled.WorkspacePremium,
                                    color = OrangePrimary
                                )

                                HistoryStatColumn(
                                    label = "Haftalık Sıklık",
                                    value = "2 kez",
                                    icon = Icons.Filled.TrendingUp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- FULL SCREEN VIDEO DIALOG SIMULATOR OVERLAY ---
        if (isFullScreen) {
            AlertDialog(
                onDismissRequest = { isFullScreen = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                confirmButton = {},
                containerColor = Color.Black,
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxSize(),
                text = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SimulatedExerciseVisualizer(
                                exerciseId = exercise.id,
                                progress = playProgress,
                                tension = tensionMultiplier,
                                scale = 1.6f
                            )
                        }

                        // Exit button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(exercise.name + " (Geniş Ekran)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(
                                onClick = { isFullScreen = false },
                                modifier = Modifier.background(SurfaceDark, CircleShape)
                            ) {
                                Icon(Icons.Filled.FullscreenExit, contentDescription = "Tam Ekranı Kapat", tint = Color.White)
                            }
                        }

                        // Bottom scrub line on full screen
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(onClick = { isPlaying = !isPlaying }) {
                                Icon(
                                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Slider(
                                value = playProgress,
                                onValueChange = { playProgress = it },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary)
                            )
                            Text(
                                "%.0f%%".format(playProgress * 100),
                                color = LightText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }

        // --- PLAYLIST SELECTION OVERLAY CONTROLLER ---
        if (showPlaylistSelector) {
            AlertDialog(
                onDismissRequest = { showPlaylistSelector = false },
                title = { Text("Oynatma Listesine Ekle", color = OrangePrimary, fontWeight = FontWeight.Black, fontSize = 16.sp) },
                containerColor = CardBg,
                shape = RoundedCornerShape(16.dp),
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Egzersizi eklemek için aşağıdaki bir listeye dokunun veya yeni bir tane oluşturun.", color = MutedText, fontSize = 11.sp)
                        
                        Divider(color = SurfaceDark)

                        // Playlists List
                        customPlaylists.forEach { (playlistName, content) ->
                            val alreadyIn = content.contains(exercise.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (alreadyIn) OrangePrimary.copy(alpha = 0.15f) else SurfaceDark)
                                    .clickable {
                                        if (alreadyIn) {
                                            viewModel.removeExerciseFromPlaylist(playlistName, exercise.id)
                                        } else {
                                            viewModel.addExerciseToPlaylist(playlistName, exercise.id)
                                        }
                                        showPlaylistSelector = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                                    Text(playlistName, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${content.size} Video", color = MutedText, fontSize = 10.sp)
                            }
                        }

                        // Create quick playlist action
                        var newPlaylistInput by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = newPlaylistInput,
                                onValueChange = { newPlaylistInput = it },
                                placeholder = { Text("Yeni Oynatma Listesi...", color = MutedText, fontSize = 11.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark,
                                    focusedTextColor = LightText,
                                    unfocusedTextColor = LightText
                                ),
                                modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(6.dp)),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (newPlaylistInput.isNotBlank()) {
                                        viewModel.createPlaylist(newPlaylistInput)
                                        newPlaylistInput = ""
                                    }
                                },
                                modifier = Modifier.background(OrangePrimary, CircleShape).size(36.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Ekle", tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPlaylistSelector = false }) {
                        Text("Kapat", color = MutedText)
                    }
                }
            )
        }
    }
}

// Stats generator helper
@Composable
fun HistoryStatColumn(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = LightText
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text(label, color = MutedText, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun GuidanceDetailRow(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Column {
            Text(title, color = LightText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(desc, color = MutedText, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

// SIMULATED VECTOR-BASED EXERCISE RE-ENACTMENT LIFT ENGINE
@Composable
fun SimulatedExerciseVisualizer(
    exerciseId: String,
    progress: Float,
    tension: Float,
    scale: Float = 1.0f
) {
    // We recreate standard exercises on vector canvas smoothly
    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(130.dp * scale)
    ) {
        val cx = size.width / 2
        val cy = size.height / 2
        val liftProgress = sin(progress * Math.PI.toFloat()) // Smooth sinusoid oscillation

        // Stroke brush definitions
        val primaryStroke = Stroke(width = 6f * scale)
        val skeletonStroke = Stroke(width = 4f * scale)

        // Draw depending on specific exercise definitions
        when {
            exerciseId.contains("bench_press") -> {
                // Bench card
                drawLine(Color.White.copy(alpha = 0.2f), Offset(cx - 80f * scale, cy + 30f * scale), Offset(cx + 80f * scale, cy + 30f * scale), strokeWidth = 10f * scale)
                drawLine(Color.White.copy(alpha = 0.15f), Offset(cx - 30f * scale, cy + 30f * scale), Offset(cx - 30f * scale, cy + 60f * scale), strokeWidth = 6f * scale)
                drawLine(Color.White.copy(alpha = 0.15f), Offset(cx + 30f * scale, cy + 30f * scale), Offset(cx + 30f * scale, cy + 60f * scale), strokeWidth = 6f * scale)

                // Human Torso
                drawCircle(Color.LightGray.copy(alpha = 0.8f), center = Offset(cx - 50f * scale, cy + 20f * scale), radius = 10f * scale) // Head
                drawLine(Color.LightGray.copy(alpha = 0.6f), Offset(cx - 40f * scale, cy + 26f * scale), Offset(cx + 40f * scale, cy + 26f * scale), strokeWidth = 8f * scale) // Body

                // Arm push biomechanics
                val handY = cy + 20f * scale - (35f * scale * liftProgress)
                val elbowY = cy + 23f * scale - (12f * scale * liftProgress)

                // Left Arm joint lines
                drawLine(Color.LightGray, Offset(cx - 15f * scale, cy + 26f * scale), Offset(cx - 15f * scale, elbowY), strokeWidth = 4f * scale)
                drawLine(Color.LightGray, Offset(cx - 15f * scale, elbowY), Offset(cx - 15f * scale, handY), strokeWidth = 4f * scale)

                // Right Arm joint lines
                drawLine(Color.LightGray, Offset(cx + 15f * scale, cy + 26f * scale), Offset(cx + 15f * scale, elbowY), strokeWidth = 4f * scale)
                drawLine(Color.LightGray, Offset(cx + 15f * scale, elbowY), Offset(cx + 15f * scale, handY), strokeWidth = 4f * scale)

                // Barbell weight bar
                drawLine(Color.DarkGray, Offset(cx - 90f * scale, handY), Offset(cx + 90f * scale, handY), strokeWidth = 3f * scale)
                // Barbell plates (left/right) with glow responding to tension intensity
                val glowC = if (tension > 0.8f) OrangePrimary else OrangeAccent
                drawRoundRect(glowC, Offset(cx - 90f * scale, handY - 15f * scale), size = androidx.compose.ui.geometry.Size(12f * scale, 30f * scale), cornerRadius = CornerRadius(4f, 4f))
                drawRoundRect(glowC, Offset(cx + 78f * scale, handY - 15f * scale), size = androidx.compose.ui.geometry.Size(12f * scale, 30f * scale), cornerRadius = CornerRadius(4f, 4f))

                // Highlight muscle glow vector (chest circles)
                drawCircle(OrangePrimary.copy(alpha = 0.15f + 0.6f * tension), radius = (14f + 8f * tension) * scale, center = Offset(cx, cy + 23f * scale))
            }
            exerciseId.contains("squat") -> {
                // Floor line
                drawLine(Color.White.copy(alpha = 0.2f), Offset(cx - 80f * scale, cy + 70f * scale), Offset(cx + 80f * scale, cy + 70f * scale), strokeWidth = 4f * scale)

                // Human head
                val torsoYOffset = 45f * scale * liftProgress
                drawCircle(Color.LightGray, center = Offset(cx, cy - 25f * scale + torsoYOffset), radius = 10f * scale)

                // Back and Spine line (bend down as progress increases)
                drawLine(Color.LightGray, Offset(cx, cy - 15f * scale + torsoYOffset), Offset(cx - 14f * scale * liftProgress, cy + 25f * scale + torsoYOffset * 0.4f), strokeWidth = 6f * scale)

                // Hips & Legs bending joints
                val hipY = cy + 25f * scale + torsoYOffset * 0.4f
                val kneeY = cy + 45f * scale + torsoYOffset * 0.1f
                drawLine(Color.LightGray, Offset(cx - 14f * scale * liftProgress, hipY), Offset(cx + 16f * scale, kneeY), strokeWidth = 5f * scale)
                drawLine(Color.LightGray, Offset(cx + 16f * scale, kneeY), Offset(cx, cy + 70f * scale), strokeWidth = 5f * scale)

                // Weighted bar on shoulders
                val barY = cy - 10f * scale + torsoYOffset
                drawLine(Color.DarkGray, Offset(cx - 80f * scale, barY), Offset(cx + 80f * scale, barY), strokeWidth = 4f * scale)
                drawCircle(OrangePrimary, radius = 11f * scale, center = Offset(cx - 80f * scale, barY))
                drawCircle(OrangePrimary, radius = 11f * scale, center = Offset(cx + 80f * scale, barY))

                // Glowing leg muscle groups
                drawCircle(OrangePrimary.copy(alpha = 0.1f + 0.7f * tension), radius = (15f + 10f * tension) * scale, center = Offset(cx + 10f * scale, kneeY - 14f * scale))
            }
            exerciseId.contains("deadlift") -> {
                // Ground
                drawLine(Color.White.copy(alpha = 0.2f), Offset(cx - 80f * scale, cy + 70f * scale), Offset(cx + 80f * scale, cy + 70f * scale), strokeWidth = 4f * scale)

                // Weighted bar height from floor
                val barY = cy + 60f * scale - (45f * scale * liftProgress)
                drawLine(Color.DarkGray, Offset(cx - 85f * scale, barY), Offset(cx + 85f * scale, barY), strokeWidth = 4f * scale)
                drawCircle(OrangePrimary, radius = 12f * scale, center = Offset(cx - 85f * scale, barY))
                drawCircle(OrangePrimary, radius = 12f * scale, center = Offset(cx + 85f * scale, barY))

                // Human skeleton lifter
                val torsoY = cy + 15f * scale - (35f * scale * liftProgress)
                drawCircle(Color.LightGray, center = Offset(cx - 20f * scale, torsoY - 20f * scale), radius = 10f * scale) // head
                drawLine(Color.LightGray, Offset(cx - 20f * scale, torsoY - 10f * scale), Offset(cx - 10f * scale, torsoY + 20f * scale), strokeWidth = 6f * scale) // spine

                // Arms grasping the bar
                drawLine(Color.LightGray, Offset(cx - 20f * scale, torsoY - 5f * scale), Offset(cx + 10f * scale, barY), strokeWidth = 4f * scale)

                // Glowing back muscles
                drawCircle(OrangePrimary.copy(alpha = 0.1f + 0.7f * tension), radius = (12f + 8f * tension) * scale, center = Offset(cx - 15f * scale, torsoY + 5f * scale))
            }
            else -> {
                // Cardio & general dumbbell lift animation
                val liftY = cy + 10f * scale - (40f * scale * liftProgress)
                drawCircle(Color.LightGray, center = Offset(cx, cy - 30f * scale), radius = 12f * scale) // head
                drawLine(Color.LightGray, Offset(cx, cy - 18f * scale), Offset(cx, cy + 30f * scale), strokeWidth = 6f * scale) // torso

                // Left Arm holding dumbbell
                drawLine(Color.LightGray, Offset(cx, cy - 10f * scale), Offset(cx - 30f * scale, liftY), strokeWidth = 5f * scale)
                drawLine(Color.DarkGray, Offset(cx - 40f * scale, liftY), Offset(cx - 20f * scale, liftY), strokeWidth = 4f * scale)
                drawCircle(OrangePrimary, radius = 6f * scale, center = Offset(cx - 40f * scale, liftY))
                drawCircle(OrangePrimary, radius = 6f * scale, center = Offset(cx - 20f * scale, liftY))

                // Right Arm holding dumbbell
                drawLine(Color.LightGray, Offset(cx, cy - 10f * scale), Offset(cx + 30f * scale, liftY), strokeWidth = 5f * scale)
                drawLine(Color.DarkGray, Offset(cx + 20f * scale, liftY), Offset(cx + 40f * scale, liftY), strokeWidth = 4f * scale)
                drawCircle(OrangePrimary, radius = 6f * scale, center = Offset(cx + 20f * scale, liftY))
                drawCircle(OrangePrimary, radius = 6f * scale, center = Offset(cx + 40f * scale, liftY))

                // General pulsing central core muscular glow
                drawCircle(OrangePrimary.copy(alpha = 0.1f + 0.6f * tension), radius = (14f + 12f * tension) * scale, center = Offset(cx, cy))
            }
        }
    }
}

// 2D HUMAN ANATOMY GLOWING CHART IN-CARD MODULE
@Composable
fun DetailedAnatomyDrawing(
    muscleGroup: String,
    pulseIntensity: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2

        // Anatomy skeleton silhouette base
        drawCircle(Color.White.copy(alpha = 0.15f), center = Offset(cx, cy - 30f), radius = 8f) // Head
        drawLine(Color.White.copy(alpha = 0.15f), Offset(cx, cy - 22f), Offset(cx, cy + 18f), strokeWidth = 4f) // Spine
        drawLine(Color.White.copy(alpha = 0.15f), Offset(cx - 18f, cy - 22f), Offset(cx + 18f, cy - 22f), strokeWidth = 3f) // Shoulder
        drawLine(Color.White.copy(alpha = 0.15f), Offset(cx - 10f, cy + 18f), Offset(cx - 10f, cy + 45f), strokeWidth = 4f) // Left Leg
        drawLine(Color.White.copy(alpha = 0.15f), Offset(cx + 10f, cy + 18f), Offset(cx + 10f, cy + 45f), strokeWidth = 4f) // Right Leg

        val primaryGlowColor = OrangePrimary.copy(alpha = 0.25f + 0.7f * pulseIntensity)
        val secondaryGlowColor = OrangeAccent.copy(alpha = 0.15f + 0.4f * pulseIntensity)

        // Light up target areas based on selected category type
        when (muscleGroup.lowercase()) {
            "göğüs" -> {
                // Primary Chest
                drawCircle(primaryGlowColor, center = Offset(cx - 7f, cy - 12f), radius = 10f + 5f * pulseIntensity)
                drawCircle(primaryGlowColor, center = Offset(cx + 7f, cy - 12f), radius = 10f + 5f * pulseIntensity)
                
                // Secondary Arms/Triceps
                drawCircle(secondaryGlowColor, center = Offset(cx - 20f, cy - 2f), radius = 7f)
                drawCircle(secondaryGlowColor, center = Offset(cx + 20f, cy - 2f), radius = 7f)
            }
            "sırt" -> {
                // Back target
                drawCircle(primaryGlowColor, center = Offset(cx, cy - 5f), radius = 14f + 7f * pulseIntensity)
                // Secondary biceps/legs
                drawCircle(secondaryGlowColor, center = Offset(cx - 18f, cy - 12f), radius = 6f)
                drawCircle(secondaryGlowColor, center = Offset(cx + 18f, cy - 12f), radius = 6f)
            }
            "omuz" -> {
                // Shoulder target
                drawCircle(primaryGlowColor, center = Offset(cx - 18f, cy - 20f), radius = 10f + 4f * pulseIntensity)
                drawCircle(primaryGlowColor, center = Offset(cx + 18f, cy - 20f), radius = 10f + 4f * pulseIntensity)
            }
            "kol" -> {
                // Arms target
                drawCircle(primaryGlowColor, center = Offset(cx - 18f, cy - 4f), radius = 9f + 4f * pulseIntensity)
                drawCircle(primaryGlowColor, center = Offset(cx + 18f, cy - 4f), radius = 9f + 4f * pulseIntensity)
            }
            "bacak" -> {
                // Legs target
                drawCircle(primaryGlowColor, center = Offset(cx - 10f, cy + 30f), radius = 12f + 5f * pulseIntensity)
                drawCircle(primaryGlowColor, center = Offset(cx + 10f, cy + 30f), radius = 12f + 5f * pulseIntensity)
            }
            "karın" -> {
                // Rec. Abdominal area
                drawCircle(primaryGlowColor, center = Offset(cx, cy + 2f), radius = 11f + 5f * pulseIntensity)
            }
            else -> {
                // Cardio system total pulsing glow
                drawCircle(primaryGlowColor, center = Offset(cx, cy - 10f), radius = 16f + 8f * pulseIntensity)
            }
        }
    }
}

// 75dp MINI HUD ACTIVE ANATOMY PREVIEW MODULE
@Composable
fun MiniGlowAnatomyModel(
    muscleGroup: String,
    intensity: Float
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2

            // Human blueprint line base
            drawCircle(Color.White.copy(alpha = 0.3f), center = Offset(cx, cy - 18f), radius = 5f)
            drawLine(Color.White.copy(alpha = 0.2f), Offset(cx, cy - 13f), Offset(cx, cy + 12f), strokeWidth = 2f)
            drawLine(Color.White.copy(alpha = 0.2f), Offset(cx - 10f, cy - 13f), Offset(cx + 10f, cy - 13f), strokeWidth = 2f)
            drawLine(Color.White.copy(alpha = 0.2f), Offset(cx - 5f, cy + 12f), Offset(cx - 5f, cy + 28f), strokeWidth = 3f)
            drawLine(Color.White.copy(alpha = 0.2f), Offset(cx + 5f, cy + 12f), Offset(cx + 5f, cy + 28f), strokeWidth = 3f)

            // Primary target highlighter with active glow pulsing color shift
            val activeColor = OrangePrimary.copy(alpha = 0.3f + 0.65f * intensity)
            
            when (muscleGroup.lowercase()) {
                "göğüs" -> {
                    drawCircle(activeColor, radius = 6f + 3f * intensity, center = Offset(cx - 4f, cy - 8f))
                    drawCircle(activeColor, radius = 6f + 3f * intensity, center = Offset(cx + 4f, cy - 8f))
                }
                "sırt" -> {
                    drawCircle(activeColor, radius = 9f + 4f * intensity, center = Offset(cx, cy - 4f))
                }
                "omuz" -> {
                    drawCircle(activeColor, radius = 6f + 3f * intensity, center = Offset(cx - 10f, cy - 13f))
                    drawCircle(activeColor, radius = 6f + 3f * intensity, center = Offset(cx + 10f, cy - 13f))
                }
                "kol" -> {
                    drawCircle(activeColor, radius = 6f + 3f * intensity, center = Offset(cx - 10f, cy - 4f))
                    drawCircle(activeColor, radius = 6f + 3f * intensity, center = Offset(cx + 10f, cy - 4f))
                }
                "bacak" -> {
                    drawCircle(activeColor, radius = 7f + 4f * intensity, center = Offset(cx - 5f, cy + 18f))
                    drawCircle(activeColor, radius = 7f + 4f * intensity, center = Offset(cx + 5f, cy + 18f))
                }
                "karın" -> {
                    drawCircle(activeColor, radius = 7f + 3f * intensity, center = Offset(cx, cy + 1f))
                }
                else -> {
                    // Global core center pulse
                    drawCircle(activeColor, radius = 10f + 5f * intensity, center = Offset(cx, cy - 5f))
                }
            }
        }
    }
}
