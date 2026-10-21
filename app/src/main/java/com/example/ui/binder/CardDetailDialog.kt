package com.example.ui.binder

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.HongKongDistricts
import com.example.data.RestaurantCardEntity
import com.example.ui.pixel.PixelBadge
import com.example.ui.pixel.PixelButton
import com.example.ui.pixel.PixelRatingStars
import com.example.ui.pixel.PixelStepShape
import com.example.ui.pixel.pixelBevel
import com.example.ui.theme.FusionPixelFontFamily
import com.example.ui.theme.GbaCardFrameDark
import com.example.ui.theme.GbaCardTerracotta
import com.example.ui.theme.GbaHeaderBg
import com.example.ui.theme.RetroBlue
import com.example.ui.theme.RetroDarkBg
import com.example.ui.theme.RetroGold
import com.example.ui.theme.RetroGreen
import com.example.ui.theme.RetroRed
import com.example.ui.theme.RetroWindowBevel
import com.example.ui.theme.RetroWindowHighlight
import com.example.ui.theme.SilkscreenFontFamily
import com.example.ui.theme.TombstoneRibbon

@Composable
fun CardDetailDialog(
    card: RestaurantCardEntity,
    isInDuel: Boolean,
    onToggleDuel: () -> Unit,
    onToggleMemorial: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onUpdateCard: (RestaurantCardEntity) -> Unit = {},
    onUploadPhotos: (List<String>) -> Unit = {},
    onDirectWhatsAppDuel: () -> Unit = onToggleDuel
) {
    var isEditing by remember { mutableStateOf(false) }

    // Editable form state
    var editName by remember(card) { mutableStateOf(card.name) }
    var editDistrict by remember(card) { mutableStateOf(card.district) }
    var editSignatureDish by remember(card) { mutableStateOf(card.signatureDish) }
    var editFlawTrap by remember(card) { mutableStateOf(card.flawTrap) }
    var editPriceAvg by remember(card) { mutableStateOf(card.priceAvg.toString()) }
    var editRatingStars by remember(card) { mutableIntStateOf(card.ratingStars) }
    var editTastingNotes by remember(card) { mutableStateOf(card.tastingNotes) }
    var editPhotoUris by remember(card) { mutableStateOf(card.photoUriList) }
    var isDistrictDropdownOpen by remember { mutableStateOf(false) }

    // Currently displayed photo index for multi-photo carousel
    var selectedPhotoIndex by remember(card) { mutableIntStateOf(0) }

    val isMemorial = card.isClosedMemorial
    val matrix = ColorMatrix().apply { if (isMemorial) setToSaturation(0f) }

    // Multiple photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val stringUris = uris.map { it.toString() }
            if (isEditing) {
                editPhotoUris = stringUris
            } else {
                onUploadPhotos(stringUris)
            }
            selectedPhotoIndex = 0
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(PixelStepShape(4.dp))
                .background(if (isMemorial) Color(0xFFC4C2BF) else GbaCardTerracotta)
                .pixelBevel(
                    borderWidth = 2.5.dp,
                    outerColor = GbaCardFrameDark,
                    highlightColor = RetroWindowHighlight,
                    shadowColor = RetroWindowBevel
                )
                .padding(vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GbaHeaderBg)
                        .pixelBevel(
                            borderWidth = 2.dp,
                            outerColor = GbaCardFrameDark,
                            highlightColor = Color(0x33FFFFFF),
                            shadowColor = Color(0x66000000)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditing) "修改食卡資料" else "食卡檔案: ${card.name}",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isEditing) {
                                PixelButton(
                                    onClick = { isEditing = true },
                                    backgroundColor = RetroBlue,
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "修改",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "修改資料",
                                            fontFamily = FusionPixelFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "關閉",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    if (isEditing) {
                        // =================== EDIT MODE ===================
                        Text(
                            text = "[編輯模式] 請修改以下資料：",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GbaCardFrameDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Restaurant Name
                        CardEditInputField(
                            label = "餐廳名稱",
                            value = editName,
                            onValueChange = { editName = it }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // District selector
                        Text(
                            text = "香港地區",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GbaCardFrameDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .clip(PixelStepShape(2.dp))
                                    .background(Color(0xFFFAF7EE))
                                    .border(1.5.dp, GbaCardFrameDark, PixelStepShape(2.dp))
                                    .clickable { isDistrictDropdownOpen = true }
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "$editDistrict ▼",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            DropdownMenu(
                                expanded = isDistrictDropdownOpen,
                                onDismissRequest = { isDistrictDropdownOpen = false }
                            ) {
                                HongKongDistricts.ALL_DISTRICTS.forEach { district ->
                                    DropdownMenuItem(
                                        text = { Text(district.name, fontFamily = FusionPixelFontFamily, fontSize = 14.sp) },
                                        onClick = {
                                            editDistrict = district.name
                                            isDistrictDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Signature Dish
                        CardEditInputField(
                            label = "招牌菜式 (必食推薦)",
                            value = editSignatureDish,
                            onValueChange = { editSignatureDish = it }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Flaw Trap
                        CardEditInputField(
                            label = "伏位 / 注意事項",
                            value = editFlawTrap,
                            onValueChange = { editFlawTrap = it }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Price Avg & Rating
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                CardEditInputField(
                                    label = "人均消費 (HK$)",
                                    value = editPriceAvg,
                                    keyboardType = KeyboardType.Number,
                                    onValueChange = { editPriceAvg = it }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "美味評分 (1-5星)",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GbaCardFrameDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .clip(PixelStepShape(2.dp))
                                        .background(Color(0xFFFAF7EE))
                                        .border(1.5.dp, GbaCardFrameDark, PixelStepShape(2.dp))
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    (1..5).forEach { star ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "$star 星",
                                            tint = if (star <= editRatingStars) RetroGold else Color(0xFFC0B8A8),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { editRatingStars = star }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Tasting Notes
                        CardEditInputField(
                            label = "食評筆記",
                            value = editTastingNotes,
                            singleLine = false,
                            onValueChange = { editTastingNotes = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Photo Upload Button inside Edit Mode
                        PixelButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            backgroundColor = Color(0xFF5B4636),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "上傳相片",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (editPhotoUris.isNotEmpty()) "更換相片 (已選 ${editPhotoUris.size} 張)" else "按此上傳相片 (可選多張)",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Save & Cancel Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PixelButton(
                                onClick = {
                                    val price = editPriceAvg.toIntOrNull() ?: card.priceAvg
                                    val atk = (editRatingStars * 18 + (editSignatureDish.length.coerceAtMost(10) * 1.2)).toInt().coerceIn(60, 99)
                                    val def = (100 - (editFlawTrap.length.coerceAtMost(15) * 2)).coerceIn(40, 95)
                                    val updated = card.copy(
                                        name = editName.ifBlank { card.name },
                                        district = editDistrict,
                                        signatureDish = editSignatureDish,
                                        flawTrap = editFlawTrap,
                                        priceAvg = price,
                                        ratingStars = editRatingStars,
                                        tastingNotes = editTastingNotes,
                                        photoUri = if (editPhotoUris.isNotEmpty()) editPhotoUris.joinToString(",") else card.photoUri,
                                        battleTastePower = atk,
                                        battleFlawDef = def
                                    )
                                    onUpdateCard(updated)
                                    isEditing = false
                                },
                                backgroundColor = RetroGreen,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "儲存修改",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            PixelButton(
                                onClick = { isEditing = false },
                                backgroundColor = Color(0xFF666666),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "取消",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                    } else {
                        // =================== VIEW MODE ===================
                        // Restaurant Name & Stars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = card.name,
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GbaCardFrameDark
                            )

                            PixelRatingStars(rating = card.ratingStars)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large Center Pixel Dish Artwork Frame (Clickable to upload photo!)
                        // If multiple photos, displays 1st photo, or allows tapping thumbnails
                        val photoList = card.photoUriList
                        val currentDisplayUri = if (photoList.isNotEmpty()) {
                            photoList.getOrElse(selectedPhotoIndex) { photoList.first() }
                        } else null

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.25f)
                                .clip(PixelStepShape(4.dp))
                                .background(Color(0xFF283544))
                                .pixelBevel(
                                    borderWidth = 3.dp,
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
                            if (!currentDisplayUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(Uri.parse(currentDisplayUri))
                                        .crossfade(false)
                                        .build(),
                                    contentDescription = card.name,
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.None, // Forced Nearest-Neighbor!
                                    colorFilter = if (isMemorial) ColorFilter.colorMatrix(matrix) else null,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Multi-photo indicator tag
                                if (photoList.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .clip(PixelStepShape(2.dp))
                                            .background(Color(0xCC000000))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${selectedPhotoIndex + 1}/${photoList.size} (按相片更換)",
                                            fontFamily = FusionPixelFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "按此上傳相片",
                                        tint = Color(0xFFC8B49B),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "按下照片即可上傳相片 (可多張)",
                                        fontFamily = FusionPixelFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD6C4AD)
                                    )
                                }
                            }
                        }

                        // If multiple photos, show thumbnail gallery strip
                        if (photoList.size > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                photoList.forEachIndexed { index, uriStr ->
                                    val isSelected = index == selectedPhotoIndex
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(PixelStepShape(2.dp))
                                            .background(Color.Black)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) RetroGold else Color.Gray,
                                                shape = PixelStepShape(2.dp)
                                            )
                                            .clickable { selectedPhotoIndex = index }
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(Uri.parse(uriStr))
                                                .crossfade(false)
                                                .build(),
                                            contentDescription = "Thumbnail $index",
                                            contentScale = ContentScale.Crop,
                                            filterQuality = FilterQuality.None,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Info Card Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PixelStepShape(3.dp))
                                .background(Color(0xFFF7F3E6))
                                .border(1.5.dp, GbaCardFrameDark, PixelStepShape(3.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                // Price & Stars Tag (Zero emojis!)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "[人均: HK$${card.priceAvg} | 評分: ${card.ratingStars}.0]",
                                        fontFamily = FusionPixelFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RetroRed
                                    )

                                    PixelBadge(
                                        text = card.district,
                                        backgroundColor = RetroBlue
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Dish Title
                                Text(
                                    text = card.signatureDish,
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GbaCardFrameDark
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Badges Row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    PixelBadge(text = "招牌必食", backgroundColor = RetroGold)
                                    PixelBadge(text = "超人氣", backgroundColor = RetroRed)
                                    PixelBadge(text = "ATK:${card.battleTastePower}", backgroundColor = Color(0xFF333333))
                                    PixelBadge(text = "DEF:${card.battleFlawDef}", backgroundColor = Color(0xFF555555))
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Flaw / Trap line (Zero emojis)
                                Text(
                                    text = "[伏位注意]: ${card.flawTrap}",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RetroRed
                                )

                                if (card.tastingNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "[食評隨筆]: ${card.tastingNotes}",
                                        fontFamily = FusionPixelFontFamily,
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // WhatsApp Duel Banner Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PixelStepShape(3.dp))
                                .background(Color(0xFF2E7D32))
                                .pixelBevel(
                                    borderWidth = 2.dp,
                                    outerColor = GbaCardFrameDark,
                                    highlightColor = Color(0xFF81C784),
                                    shadowColor = Color(0xFF1B5E20)
                                )
                                .clickable { onDirectWhatsAppDuel() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "WhatsApp 決鬥 - 幫你決定去邊度食！",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action buttons: Duel Toggle, Memorial Toggle, Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PixelButton(
                                onClick = onToggleDuel,
                                modifier = Modifier.weight(1f),
                                backgroundColor = if (isInDuel) RetroRed else RetroGreen
                            ) {
                                Text(
                                    text = if (isInDuel) "退出決鬥" else "加入決鬥",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            PixelButton(
                                onClick = onToggleMemorial,
                                modifier = Modifier.weight(1f),
                                backgroundColor = if (isMemorial) Color(0xFF555555) else TombstoneRibbon
                            ) {
                                Text(
                                    text = if (isMemorial) "恢復營業" else "設為墓碑",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            PixelButton(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFF423832)
                            ) {
                                Text(
                                    text = "銷毀食卡",
                                    fontFamily = FusionPixelFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
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
private fun CardEditInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = FusionPixelFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = GbaCardFrameDark
        )
        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 3,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FusionPixelFontFamily,
                fontSize = 14.sp,
                color = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(PixelStepShape(2.dp))
                .background(Color(0xFFFAF7EE)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GbaCardFrameDark,
                unfocusedBorderColor = GbaCardFrameDark,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}
