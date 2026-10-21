package com.example.ui.duel

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RestaurantCardEntity
import com.example.ui.pixel.PixelBadge
import com.example.ui.pixel.PixelButton
import com.example.ui.pixel.PixelStepShape
import com.example.ui.pixel.PixelWindow
import com.example.ui.pixel.pixelBevel
import com.example.ui.theme.RetroBlue
import com.example.ui.theme.RetroDarkBg
import com.example.ui.theme.RetroGold
import com.example.ui.theme.RetroGreen
import com.example.ui.theme.RetroRed
import com.example.ui.theme.RetroWindowBevel
import com.example.ui.theme.RetroWindowBorder
import com.example.ui.theme.RetroWindowHighlight
import com.example.ui.theme.FusionPixelFontFamily
import com.example.ui.theme.SilkscreenFontFamily
import com.example.ui.theme.TombstoneAsh
import com.example.ui.theme.TombstoneRibbon

@Composable
fun DuelBattleModal(
    selectedCards: List<RestaurantCardEntity>,
    onDismiss: () -> Unit,
    onClearDuel: () -> Unit,
    onShareWhatsApp: (Context, List<RestaurantCardEntity>) -> Unit
) {
    val context = LocalContext.current

    // Calculate duel winner based on Taste Power + Flaw Def - Price Penalty
    val winnerCard = remember(selectedCards) {
        selectedCards.maxByOrNull { card ->
            val priceScore = (100 - (card.priceAvg / 5)).coerceIn(10, 80)
            card.battleTastePower * 1.5 + card.battleFlawDef + priceScore
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PixelWindow(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            backgroundColor = Color(0xFFF7F3E8),
            step = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "食界大決鬥 (FOOD DUEL)",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroRed
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = RetroDarkBg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "選擇困難解藥！16-bit 數據對決與一鍵 WhatsApp 號召好友開餐。",
                    fontFamily = FusionPixelFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF554433)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Winner Spotlight Banner
                if (winnerCard != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PixelStepShape(4.dp))
                            .background(Color(0xFF2C241E))
                            .pixelBevel(
                                borderWidth = 2.5.dp,
                                outerColor = RetroWindowBorder,
                                highlightColor = RetroGold,
                                shadowColor = RetroWindowBevel
                            )
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = RetroGold,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "[今日決鬥推薦冠軍]：",
                                    fontFamily = SilkscreenFontFamily,
                                    fontSize = 10.sp,
                                    color = RetroGold
                                )
                                Text(
                                    text = "${winnerCard.name}（${winnerCard.district}）",
                                    fontFamily = SilkscreenFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "必食：${winnerCard.signatureDish} | 綜合戰力：${winnerCard.battleTastePower}",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFFDDD2C4)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Competitor Cards Comparison List
                Text(
                    text = "候選開餐戰隊 (${selectedCards.size} 間)：",
                    fontFamily = SilkscreenFontFamily,
                    fontSize = 11.sp,
                    color = RetroDarkBg
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedCards.forEachIndexed { index, card ->
                        val isWinner = card.id == winnerCard?.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PixelStepShape(2.dp))
                                .background(if (isWinner) Color(0xFFFFF7DB) else Color(0xFFECE7D8))
                                .border(
                                    width = if (isWinner) 2.dp else 1.dp,
                                    color = if (isWinner) RetroGold else Color(0xFFB5A996),
                                    shape = PixelStepShape(2.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#${index + 1} ${card.name}",
                                            fontFamily = SilkscreenFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (card.isClosedMemorial) TombstoneAsh else RetroDarkBg
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        PixelBadge(
                                            text = card.district,
                                            backgroundColor = RetroBlue
                                        )
                                    }

                                    if (isWinner) {
                                        PixelBadge(
                                            text = "WINNER",
                                            backgroundColor = RetroRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "招牌：${card.signatureDish}",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF332920)
                                )

                                Text(
                                    text = "伏位：${card.flawTrap}",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 10.sp,
                                    color = RetroRed
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "ATK: ${card.battleTastePower} | DEF: ${card.battleFlawDef}",
                                        fontFamily = SilkscreenFontFamily,
                                        fontSize = 9.sp,
                                        color = Color(0xFF6B5845)
                                    )
                                    Text(
                                        text = "人均 HK$${card.priceAvg}",
                                        fontFamily = SilkscreenFontFamily,
                                        fontSize = 9.sp,
                                        color = RetroGold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // WhatsApp Sharing Button
                PixelButton(
                    onClick = { onShareWhatsApp(context, selectedCards) },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = RetroGreen
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "一鍵 WhatsApp 分享決鬥與開餐！",
                            fontFamily = SilkscreenFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clear Selection
                PixelButton(
                    onClick = {
                        onClearDuel()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF5A4C40)
                ) {
                    Text(
                        text = "重選候選戰隊",
                        fontFamily = FusionPixelFontFamily,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
