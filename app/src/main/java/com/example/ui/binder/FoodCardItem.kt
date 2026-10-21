package com.example.ui.binder

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.RestaurantCardEntity
import com.example.ui.pixel.PixelBadge
import com.example.ui.pixel.PixelStepShape
import com.example.ui.pixel.pixelBevel
import com.example.ui.theme.FusionPixelFontFamily
import com.example.ui.theme.GbaCardFrameDark
import com.example.ui.theme.GbaCardTerracotta
import com.example.ui.theme.GbaHeaderBg
import com.example.ui.theme.RetroBlue
import com.example.ui.theme.RetroGold
import com.example.ui.theme.RetroRed
import com.example.ui.theme.RetroWindowBevel
import com.example.ui.theme.RetroWindowHighlight

/**
 * Standard Trading Card miniature scaled version matching enlarged card design:
 * - Basic colors and styles are identical to the enlarged card:
 *   1. Top Header: GbaHeaderBg bar with bold restaurant name
 *   2. Center: Landscape photo window with 16-bit rendering. Tap photo to upload multiple photos (1st shown)
 *   3. Bottom: Evaluation panel with Price, Rating, Signature dish, District badge
 * - Zero emojis!
 */
@Composable
fun FoodCardItem(
    card: RestaurantCardEntity,
    isSelectedForDuel: Boolean,
    onCardClick: () -> Unit,
    onDuelToggle: () -> Unit,
    onUploadPhotos: ((List<String>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isMemorial = card.isClosedMemorial
    val matrix = ColorMatrix().apply { if (isMemorial) setToSaturation(0f) }

    val cardBg = if (isMemorial) Color(0xFFC7C5C2) else GbaCardTerracotta
    val headerBg = if (isMemorial) Color(0xFF3E3D3B) else GbaHeaderBg
    val innerFrameBg = if (isMemorial) Color(0xFF333333) else Color(0xFF283544)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty() && onUploadPhotos != null) {
            onUploadPhotos(uris.map { it.toString() })
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.72f) // Standard trading card proportion (~1:1.38)
            .clip(PixelStepShape(4.dp))
            .background(cardBg)
            .pixelBevel(
                borderWidth = 2.5.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = RetroWindowHighlight,
                shadowColor = RetroWindowBevel
            )
            .clickable(onClick = onCardClick)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Header: Matching enlarged card header bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PixelStepShape(2.dp))
                    .background(headerBg)
                    .pixelBevel(
                        borderWidth = 1.5.dp,
                        outerColor = GbaCardFrameDark,
                        highlightColor = Color(0x33FFFFFF),
                        shadowColor = Color(0x66000000)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.name,
                    fontFamily = FusionPixelFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 2. Center Photo Window: 1.25f landscape ratio
            // Tap photo directly to upload multiple photos (first is displayed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(PixelStepShape(3.dp))
                    .background(innerFrameBg)
                    .pixelBevel(
                        borderWidth = 2.dp,
                        outerColor = GbaCardFrameDark,
                        highlightColor = Color(0x44FFFFFF),
                        shadowColor = Color(0xFF141C24)
                    )
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val displayPhoto = card.firstPhotoUri
                if (!displayPhoto.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(displayPhoto))
                            .crossfade(false)
                            .build(),
                        contentDescription = card.name,
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.None, // 16-bit Nearest-Neighbor!
                        colorFilter = if (isMemorial) ColorFilter.colorMatrix(matrix) else null,
                        modifier = Modifier.fillMaxSize()
                    )

                    // If multiple photos, show count badge
                    if (card.photoUriList.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(3.dp)
                                .clip(PixelStepShape(2.dp))
                                .background(Color(0xCC000000))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${card.photoUriList.size}張",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Camera Placeholder - tap to upload
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "按此上傳相片",
                            tint = Color(0xFFC8B49B),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "按此上傳相片",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD6C4AD)
                        )
                    }
                }

                // Duel Select Badge in top right corner (Zero emojis!)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .clip(PixelStepShape(2.dp))
                        .background(if (isSelectedForDuel) RetroRed else Color(0xCC000000))
                        .border(1.dp, Color.White, PixelStepShape(2.dp))
                        .clickable(onClick = onDuelToggle)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelectedForDuel) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "已選",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            text = "決鬥",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // District tag badge on top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(3.dp)
                        .clip(PixelStepShape(2.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = card.district,
                        fontFamily = FusionPixelFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 3. Bottom Evaluations: Matching enlarged card info box (Price, Rating, Dish)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PixelStepShape(2.dp))
                    .background(Color(0xFFF7F3E6))
                    .border(1.5.dp, GbaCardFrameDark, PixelStepShape(2.dp))
                    .padding(horizontal = 5.dp, vertical = 3.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Price
                        Text(
                            text = "HK$${card.priceAvg}",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetroRed
                        )

                        // Rating Stars (Star icon + score, zero emojis)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "評分",
                                tint = RetroGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${card.ratingStars}.0",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GbaCardFrameDark
                            )
                        }
                    }

                    // Signature Dish Title
                    if (card.signatureDish.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = card.signatureDish,
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GbaCardFrameDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty binder card slot to ensure grid layout fills nicely!
 */
@Composable
fun EmptyFoodCardSlot(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.72f) // Same standard card aspect ratio
            .clip(PixelStepShape(4.dp))
            .background(Color(0x22FFFFFF))
            .border(1.5.dp, Color(0x66000000), PixelStepShape(4.dp))
            .clickable(onClick = onAddClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "+",
                fontFamily = FusionPixelFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0x884A3B2C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "鑄造新卡",
                fontFamily = FusionPixelFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0x884A3B2C)
            )
        }
    }
}
