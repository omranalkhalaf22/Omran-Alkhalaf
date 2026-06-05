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
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.FitTrackViewModel
import com.example.viewmodel.SocialPost
import com.example.viewmodel.LeaderboardUser

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialScreen(viewModel: FitTrackViewModel) {
    val posts by viewModel.socialPosts.collectAsState()
    val leaderboardUsers by viewModel.leaderboardUsers.collectAsState()

    var activeSocialTab by remember { mutableStateOf(0) } // 0: Feed, 1: Groups, 2: Leaderboards

    // Mock comment input state controls
    var activePostForComments by remember { mutableStateOf<SocialPost?>(null) }
    var userCommentInput by remember { mutableStateOf("") }
    
    // In-memory list of comments per post ID (simulate API response / database)
    val commentsStore = remember {
        mutableStateMapOf<Int, List<Pair<String, String>>>().apply {
            put(1, listOf(
                Pair("Selin Kaya", "İnanılmaz bir antrenman! Tebrikler Yusuf! 🔥"),
                Pair("Caner Öztürk", "Harika hacim çalışması dostum.")
            ))
        }
    }

    // Active Group state
    var selectedGroupIdx by remember { mutableStateOf(0) }
    val groupsList = listOf(
        GroupCommunity("Demir Savaşçıları (Powerlifting)", "powerlifting", "En ağır squat, deadlift ve bench basanların kutsal ocağı.", 1420, Icons.Filled.Shield),
        GroupCommunity("Estetik Estetiği (Bodybuilding)", "bodybuilding", "Hipertrofi ve simetri sevdalılarının kas mabet noktası.", 2250, Icons.Filled.FitnessCenter),
        GroupCommunity("Dayanıklılık Elitleri (Running)", "running", "Yarım ve tam maratoncular, kalp kondisyon canavarları.", 980, Icons.Filled.DirectionsRun)
    )

    // Chat room messaging simulator per Group
    var groupChatInput by remember { mutableStateOf("") }
    val groupChatsStore = remember {
        mutableStateMapOf<String, List<Pair<String, String>>>().apply {
            put("powerlifting", listOf(
                Pair("Caner Öztürk", "Bugün squat PR günüm, dua edin beyler."),
                Pair("Hilal Şahin", "Diz bandajlarını unutma sakın!"),
                Pair("Umut Güler", "180kg hayırlı olsun şimdiden.")
            ))
            put("bodybuilding", listOf(
                Pair("Selin Kaya", "Arka kol için süper set önerisi olan?"),
                Pair("Yusuf Demir", "French Press + Diamond Pushup efsane pompalar.")
            ))
            put("running", listOf(
                Pair("Hilal Şahin", "Sabah 5.30'da Belgrad Ormanı'nda 10K koşuyorum. Gelen var mı?"),
                Pair("Ömer Kara", "Hava yağmurlu görünüyor dikkat et.")
            ))
        }
    }

    // Leaderboard Metrics Filter
    var selectedMetricFilter by remember { mutableStateOf(0) } // 0: Streak, 1: Workouts, 2: Volume, 3: Steps

    // Leaderboard Category Filter
    var selectedCategoryFilter by remember { mutableStateOf(0) } // 0: Global, 1: Country, 2: Gym Club

    LaunchedEffect(Unit) {
        viewModel.updateLeaderboard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Social tab switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf("Akış", "Topluluklar", "Sıralama")
            tabs.forEachIndexed { index, label ->
                val isSelected = activeSocialTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) OrangePrimary else Color.Transparent)
                        .clickable { activeSocialTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else MutedText,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (activeSocialTab == 0) {
            // FEED TAB
            Box(modifier = Modifier.fillMaxSize()) {
                if (posts.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = null,
                            tint = MutedText.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Henüz Sosyal Akış Gönderisi Yok",
                            color = LightText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Antrenman yaptıktan sonra 'Seansı Bitir' dediğinizde buraya otomatik gönderi düşer!",
                            color = MutedText,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(posts) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("feed_card_${post.id}"),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Header Layout
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
                                                    .background(OrangePrimary.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    post.author.first().toString(),
                                                    fontWeight = FontWeight.Black,
                                                    color = OrangePrimary,
                                                    fontSize = 15.sp
                                                )
                                            }
                                            Column {
                                                Text(
                                                    post.author,
                                                    color = LightText,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    post.timestampText,
                                                    color = MutedText,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        IconButton(onClick = {}) {
                                            Icon(Icons.Filled.MoreVert, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Content text
                                    Text(
                                        text = post.activitySummary,
                                        color = LightText,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Divider(color = SurfaceDark)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Interaction panel row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Like
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.toggleLikePost(post.id) }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                    contentDescription = "Beğen",
                                                    tint = if (post.isLiked) OrangePrimary else MutedText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    "${post.likes} Beğeni",
                                                    color = if (post.isLiked) OrangePrimary else MutedText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Trigger Comment Dialog modal view
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { activePostForComments = post }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Comment,
                                                    contentDescription = "Yorum Yap",
                                                    tint = MutedText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                val commentCount = commentsStore[post.id]?.size ?: post.commentsCount
                                                Text(
                                                    "$commentCount Yorum",
                                                    color = MutedText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        IconButton(onClick = {}) {
                                            Icon(Icons.Outlined.Share, contentDescription = "Paylaş", tint = MutedText, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Comment Dialog Overlay bottom sheet style!
                activePostForComments?.let { currentPost ->
                    val cList = commentsStore[currentPost.id] ?: emptyList()

                    AlertDialog(
                        onDismissRequest = { activePostForComments = null },
                        title = {
                            Text("Yorumlar", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                            ) {
                                // List current comments
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (cList.isEmpty()) {
                                        item {
                                            Text(
                                                "Bu gönderiye henüz yorum yapılmadı. İlk yorum yapan ol!",
                                                color = MutedText,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                    } else {
                                        items(cList) { (author, text) ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(SurfaceDark, RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(author, color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(text, color = LightText, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Write comment field
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = userCommentInput,
                                        onValueChange = { userCommentInput = it },
                                        placeholder = { Text("Yorum ekle...", color = MutedText, fontSize = 12.sp) },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = SurfaceDark,
                                            unfocusedContainerColor = SurfaceDark,
                                            focusedTextColor = LightText,
                                            unfocusedTextColor = LightText,
                                            focusedIndicatorColor = OrangePrimary,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .height(48.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (userCommentInput.isNotBlank()) {
                                                val existing = commentsStore[currentPost.id] ?: emptyList()
                                                commentsStore[currentPost.id] = existing + Pair("Yusuf Demir (Sen)", userCommentInput)
                                                userCommentInput = ""
                                                viewModel.addXp(10) // Comment reward!
                                            }
                                        },
                                        modifier = Modifier.background(OrangePrimary, CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.Filled.Send, contentDescription = "Gönder", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { activePostForComments = null }) {
                                Text("Kapat", color = OrangePrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = CardBg,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        } else if (activeSocialTab == 1) {
            // COMMUNITIES & GROUPS TAB
            val selectedGroup = groupsList[selectedGroupIdx]
            val actualChannelMessages = groupChatsStore[selectedGroup.channelId] ?: emptyList()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Guild communities selector
                item {
                    Text("Bölgelere Göre Klanlar", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupsList.forEachIndexed { idx, group ->
                            val isSel = selectedGroupIdx == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) OrangePrimary else CardBg)
                                    .clickable { selectedGroupIdx = idx }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        group.icon,
                                        contentDescription = null,
                                        tint = if (isSel) Color.Black else OrangePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        group.name.split(" ").first(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.Black else LightText,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected community intro card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(selectedGroup.icon, contentDescription = null, tint = OrangePrimary)
                                    Text(selectedGroup.name, fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OrangeAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${selectedGroup.membersCount} Üye", color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(selectedGroup.description, color = MutedText, fontSize = 11.sp, lineHeight = 15.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Visual Clan challenge goals progress
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDark, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Aktif Klan Mücadelesi: 10M KG Kaldır", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("63%", color = OrangePrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(CardBg, CircleShape)) {
                                    Box(modifier = Modifier.fillMaxWidth(0.63f).fillMaxHeight().background(OrangePrimary, CircleShape))
                                }
                            }
                        }
                    }
                }

                // Interactive clan chat room
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBg, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "Klan Odası Hızlı Mesajlaşma",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Message outputs scrolling list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 220.dp)
                                .background(SurfaceDark, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (actualChannelMessages.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Kanala ilk mesajı gönder!", color = MutedText, fontSize = 11.sp)
                                }
                            } else {
                                actualChannelMessages.forEach { (sender, text) ->
                                    val isMe = sender.contains("(Sen)")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .widthIn(max = 200.dp)
                                                .background(
                                                    if (isMe) OrangePrimary.copy(alpha = 0.2f) else CardBg,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                sender,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMe) OrangePrimary else OrangeAccent,
                                                fontSize = 9.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text, color = LightText, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Message input controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = groupChatInput,
                                onValueChange = { groupChatInput = it },
                                placeholder = { Text("Klana mesaj gönder...", color = MutedText, fontSize = 11.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark,
                                    focusedTextColor = LightText,
                                    unfocusedTextColor = LightText,
                                    focusedIndicatorColor = OrangePrimary,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .height(44.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (groupChatInput.isNotBlank()) {
                                        val existing = groupChatsStore[selectedGroup.channelId] ?: emptyList()
                                        groupChatsStore[selectedGroup.channelId] = existing + Pair("Yusuf (Sen)", groupChatInput)
                                        groupChatInput = ""
                                        viewModel.addXp(12) // chat support rewarded
                                    }
                                },
                                modifier = Modifier.background(OrangePrimary, CircleShape).size(36.dp)
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = "Gönder", tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // LEADERBOARD TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Category subselector (Global, Country, Gym)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        val rankingCats = listOf("Küresel Sıralama", "Türkiye", "Gym Kulübü")
                        rankingCats.forEachIndexed { index, title ->
                            val isSelected = selectedCategoryFilter == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) OrangeAccent.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedCategoryFilter = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OrangeAccent else MutedText
                                )
                            }
                        }
                    }
                }

                // Metric filters (Streak, Workout counter, Volume load, Step counter)
                item {
                    val filters = listOf("Seri (Gün)", "Seans", "Hacim (kg)", "Adım")
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEachIndexed { index, label ->
                            val isSel = selectedMetricFilter == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) OrangePrimary.copy(alpha = 0.15f) else CardBg)
                                    .border(1.dp, if (isSel) OrangePrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedMetricFilter = index }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) OrangePrimary else LightText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Display Top 3 podium visually
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBg, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val podiumUsers = leaderboardUsers.take(3)
                        
                        // Rank 2 (Left)
                        if (podiumUsers.size >= 2) {
                            PodiumCol(podiumUsers[1], hDp = 70.dp, titleColor = Color(0xFFC0C0C0), rankName = "2.")
                        }

                        // Rank 1 (Center)
                        if (podiumUsers.isNotEmpty()) {
                            PodiumCol(podiumUsers[0], hDp = 95.dp, titleColor = OrangePrimary, rankName = "👑 1.")
                        }

                        // Rank 3 (Right)
                        if (podiumUsers.size >= 3) {
                            PodiumCol(podiumUsers[2], hDp = 55.dp, titleColor = Color(0xFFCD7F32), rankName = "3.")
                        }
                    }
                }

                // Table Rows list of all ranked users
                items(leaderboardUsers) { user ->
                    val isMe = user.isCurrentUser
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isMe) OrangePrimary.copy(alpha = 0.1f) else CardBg,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isMe) OrangePrimary else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Rank indicator
                            Text(
                                user.rank.toString(),
                                fontWeight = FontWeight.Black,
                                color = if (isMe) OrangePrimary else MutedText,
                                fontSize = 12.sp,
                                modifier = Modifier.width(20.dp)
                            )

                            // Avatar icon
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isMe) OrangePrimary.copy(alpha = 0.2f) else SurfaceDark,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                            }

                            // Username details
                            Text(
                                user.name,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                                color = if (isMe) OrangePrimary else LightText,
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val scoreValueText = when(selectedMetricFilter) {
                                0 -> "${user.score / 200} Gün" // Scaled mock metric multipliers
                                1 -> "${user.score / 150} Seans"
                                2 -> "${user.score * 10} kg"
                                else -> "${user.score * 4} Adım"
                            }
                            Text(
                                scoreValueText,
                                color = if (isMe) OrangePrimary else LightText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MutedText.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumCol(
    user: LeaderboardUser,
    hDp: androidx.compose.ui.unit.Dp,
    titleColor: Color,
    rankName: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Name labels
        Text(user.name.split(" ").first(), color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        
        // Podium block
        Box(
            modifier = Modifier
                .width(55.dp)
                .height(hDp)
                .background(
                    Brush.verticalGradient(colors = listOf(titleColor.copy(alpha = 0.35f), SurfaceDark)),
                    RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(rankName, color = titleColor, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(user.score.toString(), color = LightText, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                Text("XP", color = MutedText, fontSize = 8.sp)
            }
        }
    }
}

// Data holder
data class GroupCommunity(
    val name: String,
    val channelId: String,
    val description: String,
    val membersCount: Int,
    val icon: ImageVector
)
