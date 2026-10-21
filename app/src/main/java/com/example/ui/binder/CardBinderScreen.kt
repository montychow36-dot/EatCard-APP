package com.example.ui.binder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HongKongDistricts
import com.example.data.RestaurantCardEntity
import com.example.ui.FoodCardUiState
import com.example.ui.pixel.PixelButton
import com.example.ui.pixel.PixelStepShape
import com.example.ui.pixel.checkerboardDithering
import com.example.ui.pixel.pixelBevel
import com.example.ui.theme.FusionPixelFontFamily
import com.example.ui.theme.GbaCardFrameDark
import com.example.ui.theme.GbaHeaderBg
import com.example.ui.theme.GbaSalmonBg
import com.example.ui.theme.GbaSidebarBtnBg
import com.example.ui.theme.RetroGold
import com.example.ui.theme.RetroRed
import com.example.ui.theme.RetroWindowHighlight

@Composable
fun CardBinderScreen(
    uiState: FoodCardUiState,
    onSearchChange: (String) -> Unit,
    onDistrictFilterChange: (String?) -> Unit,
    onMemorialFilterToggle: (Boolean) -> Unit,
    onCardClick: (RestaurantCardEntity) -> Unit,
    onDuelToggle: (Long) -> Unit,
    onUploadPhotos: (Long, List<String>) -> Unit = { _, _ -> },
    onOpenForge: () -> Unit,
    onOpenDuel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRegionFilter by remember { mutableStateOf<String?>("ALL") } // ALL, Island, Kowloon, NT
    var isDistrictDropdownOpen by remember { mutableStateOf(false) }
    var pageNumber by remember { mutableIntStateOf(1) }

    // Filter by Region & District
    val regionFilteredCards = remember(uiState.filteredCards, selectedRegionFilter) {
        if (selectedRegionFilter == null || selectedRegionFilter == "ALL") {
            uiState.filteredCards
        } else {
            val targetRegion = when (selectedRegionFilter) {
                "Island" -> HongKongDistricts.REGION_HK_ISLAND
                "Kowloon" -> HongKongDistricts.REGION_KOWLOON
                "NT" -> HongKongDistricts.REGION_NT
                else -> ""
            }
            uiState.filteredCards.filter { card ->
                val districtInfo = HongKongDistricts.ALL_DISTRICTS.firstOrNull {
                    it.name == card.district || card.district.startsWith(it.name.replace("區", ""))
                }
                districtInfo?.region == targetRegion
            }
        }
    }

    // Cards enlarged by 100%: 2 columns layout with 4 cards per page (2x2)
    val pageSize = 4
    val totalPages = maxOf(1, (regionFilteredCards.size + pageSize - 1) / pageSize)
    val currentPageCards = regionFilteredCards.drop((pageNumber - 1) * pageSize).take(pageSize)
    val emptySlotsCount = (pageSize - currentPageCards.size).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .checkerboardDithering(tileDp = 2.5.dp, colorA = GbaSalmonBg, colorB = Color(0xFFDF9E84))
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
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+ ",
                        fontFamily = FusionPixelFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                    Text(
                        text = "GBA POCKET DECK",
                        fontFamily = FusionPixelFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (uiState.duelSelectedCardIds.isNotEmpty()) {
                    PixelButton(
                        onClick = onOpenDuel,
                        backgroundColor = RetroRed,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "決鬥 (${uiState.duelSelectedCardIds.size})",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Main Layout: Left Sidebar + Right 2x2 Card Pocket Deck (Cards enlarged 100%)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            // Left Sidebar: [港], [九], [新], [全] (Zero emojis!)
            Column(
                modifier = Modifier
                    .width(58.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SidebarFilterButton(
                    label = "港島",
                    iconText = "港",
                    isSelected = selectedRegionFilter == "Island",
                    onClick = {
                        selectedRegionFilter = if (selectedRegionFilter == "Island") "ALL" else "Island"
                        pageNumber = 1
                    }
                )

                SidebarFilterButton(
                    label = "九龍",
                    iconText = "九",
                    isSelected = selectedRegionFilter == "Kowloon",
                    onClick = {
                        selectedRegionFilter = if (selectedRegionFilter == "Kowloon") "ALL" else "Kowloon"
                        pageNumber = 1
                    }
                )

                SidebarFilterButton(
                    label = "新界",
                    iconText = "新",
                    isSelected = selectedRegionFilter == "NT",
                    onClick = {
                        selectedRegionFilter = if (selectedRegionFilter == "NT") "ALL" else "NT"
                        pageNumber = 1
                    }
                )

                SidebarFilterButton(
                    label = "全部",
                    iconText = "全",
                    isHot = true,
                    isSelected = selectedRegionFilter == "ALL",
                    onClick = {
                        selectedRegionFilter = "ALL"
                        onDistrictFilterChange(null)
                        pageNumber = 1
                    }
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Pocket Deck Container
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Search Bar + District Dropdown Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(PixelStepShape(2.dp))
                            .background(Color(0xFFFAF7EE))
                            .border(1.5.dp, GbaCardFrameDark, PixelStepShape(2.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = {
                                    onSearchChange(it)
                                    pageNumber = 1
                                },
                                placeholder = {
                                    Text(
                                        text = "搜尋餐廳/招牌菜...",
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
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // District Selector Dropdown Box
                    Box {
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(PixelStepShape(2.dp))
                                .background(Color(0xFFFAF7EE))
                                .border(1.5.dp, GbaCardFrameDark, PixelStepShape(2.dp))
                            .clickable { isDistrictDropdownOpen = true }
                            .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.selectedDistrict ?: "全港18區"} ▼",
                                fontFamily = FusionPixelFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GbaCardFrameDark
                            )
                        }

                        DropdownMenu(
                            expanded = isDistrictDropdownOpen,
                            onDismissRequest = { isDistrictDropdownOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("全部地區 (ALL)", fontFamily = FusionPixelFontFamily, fontSize = 14.sp) },
                                onClick = {
                                    onDistrictFilterChange(null)
                                    isDistrictDropdownOpen = false
                                }
                            )
                            HongKongDistricts.ALL_DISTRICTS.forEach { district ->
                                DropdownMenuItem(
                                    text = { Text(district.name, fontFamily = FusionPixelFontFamily, fontSize = 14.sp) },
                                    onClick = {
                                        onDistrictFilterChange(district.name)
                                        isDistrictDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2 Columns Grid: Each card is enlarged 100% (2x size), matching full card proportions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(PixelStepShape(3.dp))
                        .background(Color(0xFFE0A78F))
                        .border(1.5.dp, GbaCardFrameDark, PixelStepShape(3.dp))
                        .padding(4.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        // Render user's cards on current page
                        items(currentPageCards, key = { it.id }) { card ->
                            FoodCardItem(
                                card = card,
                                isSelectedForDuel = uiState.duelSelectedCardIds.contains(card.id),
                                onCardClick = { onCardClick(card) },
                                onDuelToggle = { onDuelToggle(card.id) },
                                onUploadPhotos = { uris -> onUploadPhotos(card.id, uris) }
                            )
                        }

                        // Fill remaining slots up to pageSize with EmptyFoodCardSlot
                        items(emptySlotsCount) {
                            EmptyFoodCardSlot(
                                onAddClick = onOpenForge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Pagination & Forge Card Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pagination controls: [+] ← 1/X →
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(PixelStepShape(2.dp))
                                .background(Color(0xFF5A7E73))
                                .border(1.5.dp, GbaCardFrameDark, PixelStepShape(2.dp))
                                .clickable { onOpenForge() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "◀",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GbaCardFrameDark,
                            modifier = Modifier.clickable {
                                if (pageNumber > 1) pageNumber--
                            }
                        )

                        Text(
                            text = " $pageNumber/$totalPages ",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GbaCardFrameDark
                        )

                        Text(
                            text = "▶",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GbaCardFrameDark,
                            modifier = Modifier.clickable {
                                if (pageNumber < totalPages) pageNumber++
                            }
                        )
                    }

                    // Forge Card Button
                    Box(
                        modifier = Modifier
                            .clip(PixelStepShape(3.dp))
                            .background(Color(0xFFBA3838))
                            .pixelBevel(
                                borderWidth = 2.dp,
                                outerColor = GbaCardFrameDark,
                                highlightColor = Color(0xFFF18C8C),
                                shadowColor = Color(0xFF6E1818)
                            )
                            .clickable { onOpenForge() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "鑄造卡片",
                            fontFamily = FusionPixelFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarFilterButton(
    label: String,
    iconText: String,
    isSelected: Boolean,
    isHot: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) RetroRed else GbaSidebarBtnBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(PixelStepShape(2.dp))
            .background(bgColor)
            .pixelBevel(
                borderWidth = 1.5.dp,
                outerColor = GbaCardFrameDark,
                highlightColor = if (isSelected) RetroWindowHighlight else Color(0x33FFFFFF),
                shadowColor = if (isSelected) Color(0xFF701A1A) else Color(0xFF2E4841)
            )
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(PixelStepShape(1.dp))
                    .background(if (isSelected) Color(0xFF6E1818) else Color(0xFF263C36)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    fontFamily = FusionPixelFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = FusionPixelFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isHot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(PixelStepShape(1.dp))
                    .background(RetroRed)
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "HOT!",
                    fontFamily = FusionPixelFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Yellow
                )
            }
        }
    }
}
