package com.example.ui.binder

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.HongKongDistricts
import com.example.ui.pixel.PixelBadge
import com.example.ui.pixel.PixelButton
import com.example.ui.pixel.PixelStepShape
import com.example.ui.pixel.PixelWindow
import com.example.ui.pixel.pixelBevel
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ForgeCardDialog(
    onDismiss: () -> Unit,
    onForge: (
        name: String,
        district: String,
        signatureDish: String,
        flawTrap: String,
        priceAvg: Int,
        ratingStars: Int,
        tastingNotes: String,
        photoUri: String?,
        isClosedMemorial: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("油尖旺") }
    var signatureDish by remember { mutableStateOf("") }
    var flawTrap by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("60") }
    var ratingStars by remember { mutableIntStateOf(4) }
    var tastingNotes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var isClosedMemorial by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
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
            backgroundColor = if (isClosedMemorial) Color(0xFFDEDCD9) else Color(0xFFF7F3E8),
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
                        text = if (isClosedMemorial) "鍛造【追憶墓碑卡】" else "鍛造新食卡 (FORGE)",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClosedMemorial) TombstoneRibbon else RetroRed
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

                Spacer(modifier = Modifier.height(10.dp))

                // Photo Selector Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(PixelStepShape(4.dp))
                        .background(Color(0xFF2C241E))
                        .pixelBevel(
                            borderWidth = 2.dp,
                            outerColor = RetroWindowBorder,
                            highlightColor = RetroWindowHighlight,
                            shadowColor = RetroWindowBevel
                        )
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(photoUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Uploaded photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                        ) {
                            PixelBadge(
                                text = "點擊更換照片",
                                backgroundColor = Color(0xCC000000)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add photo",
                                tint = RetroGold,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "點擊上傳餐廳/招牌菜相片",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Restaurant Name
                RetroInputField(
                    label = "餐廳名稱",
                    value = name,
                    placeholder = "例: 澳洲牛奶公司",
                    onValueChange = { name = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // District Selector Chips
                Text(
                    text = "所屬地區 (香港18區)",
                    fontFamily = SilkscreenFontFamily,
                    fontSize = 11.sp,
                    color = RetroDarkBg
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HongKongDistricts.ALL_DISTRICTS.forEach { district ->
                        val isSelected = district.name == selectedDistrict
                        Box(
                            modifier = Modifier
                                .clip(PixelStepShape(2.dp))
                                .background(if (isSelected) RetroRed else Color(0xFFDDD3C2))
                                .clickable { selectedDistrict = district.name }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = district.name,
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else RetroDarkBg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Signature Dish
                RetroInputField(
                    label = "最出色招牌菜",
                    value = signatureDish,
                    placeholder = "例: 厚蛋多士配熱奶茶",
                    onValueChange = { signatureDish = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Flaw Trap
                RetroInputField(
                    label = "伏位 / 注意事項",
                    value = flawTrap,
                    placeholder = "例: 光速收碟、不能久坐聊天",
                    onValueChange = { flawTrap = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price and Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        RetroInputField(
                            label = "人均消費 (HK$)",
                            value = priceText,
                            placeholder = "60",
                            keyboardType = KeyboardType.Number,
                            onValueChange = { priceText = it }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "美食評分 (1-5星)",
                            fontFamily = SilkscreenFontFamily,
                            fontSize = 11.sp,
                            color = RetroDarkBg
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star $i",
                                    tint = if (i <= ratingStars) RetroGold else Color.LightGray,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { ratingStars = i }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tasting Notes
                RetroInputField(
                    label = "食評筆記",
                    value = tastingNotes,
                    placeholder = "寫低你的味覺記憶...",
                    singleLine = false,
                    maxLines = 3,
                    onValueChange = { tastingNotes = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Memorial Tombstone Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PixelStepShape(2.dp))
                        .background(if (isClosedMemorial) Color(0x33B22222) else Color(0x11000000))
                        .clickable { isClosedMemorial = !isClosedMemorial }
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isClosedMemorial,
                        onCheckedChange = { isClosedMemorial = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = TombstoneRibbon,
                            uncheckedColor = RetroDarkBg
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "[追憶墓碑卡] 已結業店舖",
                            fontFamily = SilkscreenFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClosedMemorial) TombstoneRibbon else RetroDarkBg
                        )
                        Text(
                            text = "已結業的傳奇滋味，卡牌將以黑白灰階與紀念封條展示。",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Forge Action Button
                PixelButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            val price = priceText.toIntOrNull() ?: 50
                            onForge(
                                name.trim(),
                                selectedDistrict,
                                signatureDish.trim().ifBlank { "主廚招牌" },
                                flawTrap.trim().ifBlank { "暫無重大缺點" },
                                price,
                                ratingStars,
                                tastingNotes.trim(),
                                photoUri,
                                isClosedMemorial
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isClosedMemorial) TombstoneRibbon else RetroRed,
                    enabled = name.isNotBlank()
                ) {
                    Text(
                        text = if (isClosedMemorial) "鑄入追憶墓碑卡冊" else "正式鍛造成卡！",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RetroInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = SilkscreenFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = RetroDarkBg
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = FusionPixelFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FusionPixelFontFamily,
                fontSize = 14.sp,
                color = Color.Black
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF9F7F0),
                focusedBorderColor = RetroWindowBorder,
                unfocusedBorderColor = Color(0xFFB8ADA1),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
