package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FoodCardViewModel
import com.example.ui.binder.CardBinderScreen
import com.example.ui.binder.CardDetailDialog
import com.example.ui.binder.ForgeCardDialog
import com.example.ui.duel.DuelBattleModal
import com.example.ui.map.AncientMapScrollScreen
import com.example.ui.pixel.PixelStepShape
import com.example.ui.pixel.pixelBevel
import com.example.ui.profile.FoodMasterIdScreen
import com.example.ui.theme.FoodCardMasterTheme
import com.example.ui.theme.FusionPixelFontFamily
import com.example.ui.theme.GbaCardFrameDark
import com.example.ui.theme.GbaHeaderBg
import com.example.ui.theme.GbaSalmonBg
import com.example.ui.theme.RetroWindowHighlight

class MainActivity : ComponentActivity() {
    private val viewModel: FoodCardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FoodCardMasterTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // BackHandler support
                if (uiState.currentTab != 0) {
                    BackHandler {
                        viewModel.setCurrentTab(0)
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GbaSalmonBg),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        RetroBottomNavigation(
                            currentTab = uiState.currentTab,
                            onTabSelected = { viewModel.setCurrentTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GbaSalmonBg)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        when (uiState.currentTab) {
                            0 -> CardBinderScreen(
                                uiState = uiState,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onDistrictFilterChange = { viewModel.setSelectedDistrict(it) },
                                onMemorialFilterToggle = { viewModel.setShowMemorialOnly(it) },
                                onCardClick = { viewModel.openCardDetail(it) },
                                onDuelToggle = { viewModel.toggleCardForDuel(it) },
                                onUploadPhotos = { cardId, uris -> viewModel.updateCardPhotos(cardId, uris) },
                                onOpenForge = { viewModel.openForgeDialog(true) },
                                onOpenDuel = { viewModel.openDuelModal(true) }
                            )
                            1 -> AncientMapScrollScreen(
                                cards = uiState.allCards
                            )
                            2 -> FoodMasterIdScreen(
                                cards = uiState.allCards,
                                onOpenDuel = { viewModel.openDuelModal(true) }
                            )
                        }

                        // Dialogs
                        if (uiState.isForgeDialogOpen) {
                            ForgeCardDialog(
                                onDismiss = { viewModel.openForgeDialog(false) },
                                onForge = { name, district, sigDish, flaw, price, stars, notes, photo, isClosed ->
                                    viewModel.forgeCard(
                                        name = name,
                                        district = district,
                                        signatureDish = sigDish,
                                        flawTrap = flaw,
                                        priceAvg = price,
                                        ratingStars = stars,
                                        tastingNotes = notes,
                                        photoUri = photo,
                                        isClosedMemorial = isClosed
                                    )
                                }
                            )
                        }

                        uiState.activeCardForDetail?.let { activeCard ->
                            CardDetailDialog(
                                card = activeCard,
                                isInDuel = uiState.duelSelectedCardIds.contains(activeCard.id),
                                onToggleDuel = { viewModel.toggleCardForDuel(activeCard.id) },
                                onToggleMemorial = { viewModel.toggleMemorialStatus(activeCard) },
                                onDelete = { viewModel.deleteCard(activeCard) },
                                onDismiss = { viewModel.openCardDetail(null) },
                                onUpdateCard = { updatedCard -> viewModel.updateCard(updatedCard) },
                                onUploadPhotos = { uris -> viewModel.updateCardPhotos(activeCard.id, uris) },
                                onDirectWhatsAppDuel = {
                                    viewModel.shareDiningDuelViaWhatsApp(this@MainActivity, listOf(activeCard))
                                }
                            )
                        }

                        if (uiState.isDuelModalOpen) {
                            val duelCards = uiState.allCards.filter {
                                uiState.duelSelectedCardIds.contains(it.id)
                            }
                            DuelBattleModal(
                                selectedCards = if (duelCards.isNotEmpty()) duelCards else uiState.allCards.take(2),
                                onDismiss = { viewModel.openDuelModal(false) },
                                onClearDuel = { viewModel.clearDuelCards() },
                                onShareWhatsApp = { context, cards ->
                                    viewModel.shareDiningDuelViaWhatsApp(context, cards)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom navigation occupying exactly 10% of screen height
 * with 3 squarish-rectangles tightly packed touching side-by-side
 */
@Composable
fun RetroBottomNavigation(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    // Occupies 10% of screen height
    val barHeight = (screenHeight * 0.10f).coerceIn(68.dp, 88.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(GbaHeaderBg)
            .pixelBevel(
                borderWidth = 2.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = Color(0x33FFFFFF),
                shadowColor = Color(0x66000000)
            )
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center
    ) {
        // Three buttons horizontally evenly fill the width and current height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroNavItem(
                icon = Icons.Default.Collections,
                label = "卡片冊",
                isSelected = currentTab == 0,
                tag = "tab_binder",
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(0) }
            )
            RetroNavItem(
                icon = Icons.Default.Map,
                label = "熱力地圖",
                isSelected = currentTab == 1,
                tag = "tab_map_scroll",
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(1) }
            )
            RetroNavItem(
                icon = Icons.Default.Badge,
                label = "用戶資料",
                isSelected = currentTab == 2,
                tag = "tab_master_id",
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
private fun RetroNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color(0xFFBA3838) else Color(0xFF8A9BA8)
    val contentColor = if (isSelected) Color.White else GbaCardFrameDark

    Box(
        modifier = modifier
            .testTag(tag)
            .fillMaxHeight()
            .clip(PixelStepShape(2.dp))
            .background(bgColor)
            .pixelBevel(
                borderWidth = 2.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = if (isSelected) RetroWindowHighlight else Color(0x44FFFFFF),
                shadowColor = if (isSelected) Color(0xFF6E1818) else Color(0xFF4A5866)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = label,
                fontFamily = FusionPixelFontFamily,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
