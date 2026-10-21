package com.example.ui.pixel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 16-Bit Hand-Crafted GBA / MOTHER 3 Style Hong Kong Food Sprites
 * Drawn with crisp, pure hard pixel cells (no blur, 16-color palette, chunky pixels).
 */
@Composable
fun PixelFoodArtwork(
    dishName: String,
    isMemorial: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridDim = 24 // 24x24 pixel art grid
            val pxSize = size.width / gridDim

            fun px(x: Int, y: Int, color: Color) {
                drawRect(
                    color = color,
                    topLeft = Offset(x * pxSize, y * pxSize),
                    size = Size(pxSize, pxSize)
                )
            }

            fun rect(x: Int, y: Int, w: Int, h: Int, color: Color) {
                for (dy in 0 until h) {
                    for (dx in 0 until w) {
                        px(x + dx, y + dy, color)
                    }
                }
            }

            if (isMemorial) {
                // 16-Bit Granite Memorial Tombstone (追憶墓碑石)
                val stoneDark = Color(0xFF2B2A29)
                val stoneMid = Color(0xFF555351)
                val stoneLight = Color(0xFF8C8A87)
                val goldTrim = Color(0xFFD4AC0D)
                val ribbonRed = Color(0xFFA93226)

                // Pediment Base
                rect(3, 19, 18, 3, stoneDark)
                rect(4, 18, 16, 2, stoneMid)
                rect(5, 17, 14, 1, stoneLight)

                // Tombstone Arch
                rect(6, 4, 12, 13, stoneMid)
                rect(7, 3, 10, 1, stoneMid)
                rect(8, 2, 8, 1, stoneMid)

                // Stone Outlines
                for (x in 8..15) px(x, 2, stoneDark)
                for (x in 7..8) px(x, 3, stoneDark)
                for (x in 15..16) px(x, 3, stoneDark)
                for (y in 4..17) {
                    px(5, y, stoneDark)
                    px(18, y, stoneDark)
                }

                // Inset Highlight & Shadow
                for (y in 4..16) px(6, y, stoneLight)
                for (y in 4..16) px(17, y, Color(0xFF3A3937))

                // Golden Cross engraving
                rect(11, 6, 2, 7, goldTrim)
                rect(9, 8, 6, 2, goldTrim)

                // Memorial Rose Ribbon
                rect(10, 14, 4, 2, ribbonRed)
                px(9, 15, ribbonRed)
                px(14, 15, ribbonRed)
                return@Canvas
            }

            // Food Type Selection based on signature dish name
            when {
                dishName.contains("叉燒") || dishName.contains("蛋") -> {
                    // 16-Bit 滑蛋叉燒飯 / 厚蛋多士 (Charsiu Egg Rice)
                    val bowlRim = Color(0xFF1E1B18)
                    val bowlWhite = Color(0xFFF7F9F9)
                    val bowlShadow = Color(0xFFBDC3C7)
                    val riceWhite = Color(0xFFFDEDEC)
                    val eggYellow = Color(0xFFF4D03F)
                    val eggOrange = Color(0xFFF39C12)
                    val charsiuRed = Color(0xFFB03A2E)
                    val charsiuChar = Color(0xFF641E16)
                    val vegGreen = Color(0xFF27AE60)

                    // Plate / Bowl Oval
                    rect(3, 14, 18, 6, bowlRim)
                    rect(4, 13, 16, 6, bowlWhite)
                    rect(5, 17, 14, 2, bowlShadow)

                    // Rice Base
                    rect(5, 10, 14, 4, riceWhite)

                    // Golden Scrambled Egg Swirl
                    rect(6, 9, 7, 5, eggYellow)
                    rect(7, 10, 5, 3, eggOrange)
                    px(8, 11, Color(0xFFF9E79F)) // highlight

                    // Sliced BBQ Charsiu
                    rect(13, 8, 6, 6, charsiuRed)
                    for (i in 0..4 step 2) {
                        px(13 + i, 8, charsiuChar)
                        px(14 + i, 11, charsiuChar)
                        px(13 + i, 13, charsiuChar)
                    }

                    // Green Scallions / Veg
                    px(11, 7, vegGreen)
                    px(12, 7, vegGreen)
                    px(12, 8, Color(0xFF52BE80))
                    px(10, 9, vegGreen)
                }

                dishName.contains("牛腩") || dishName.contains("麵") || dishName.contains("豬手") -> {
                    // 16-Bit 清湯牛腩粗麵 (Beef Brisket Noodles)
                    val bowlBlue = Color(0xFF2980B9)
                    val bowlDark = Color(0xFF1A5276)
                    val brothBrown = Color(0xFF7E5109)
                    val noodleYellow = Color(0xFFF9E79F)
                    val beefDark = Color(0xFF512E14)
                    val beefFat = Color(0xFF935116)
                    val scallion = Color(0xFF2ECC71)

                    // Bowl
                    rect(4, 10, 16, 9, bowlDark)
                    rect(5, 11, 14, 7, bowlBlue)
                    rect(6, 17, 12, 2, Color(0xFF1B4F72))

                    // Broth
                    rect(5, 7, 14, 4, brothBrown)

                    // Wavy Noodles
                    for (x in 6..17 step 2) {
                        px(x, 7, noodleYellow)
                        px(x + 1, 8, noodleYellow)
                        px(x, 9, noodleYellow)
                    }

                    // Stewed Beef Brisket Chunks
                    rect(7, 6, 4, 3, beefDark)
                    px(8, 7, beefFat)
                    rect(13, 6, 4, 3, beefDark)
                    px(14, 7, beefFat)

                    // Green Chopped Onion
                    px(11, 6, scallion)
                    px(12, 7, scallion)
                    px(10, 8, scallion)
                }

                dishName.contains("蟹") || dishName.contains("海鮮") -> {
                    // 16-Bit 避風塘炒蟹 (Typhoon Shelter Crab)
                    val plateDark = Color(0xFF1C2833)
                    val crabRed = Color(0xFFC0392B)
                    val crabOrange = Color(0xFFE67E22)
                    val garlicGold = Color(0xFFF1C40F)
                    val chiliRed = Color(0xFF900C3F)

                    // Plate
                    rect(3, 13, 18, 6, plateDark)
                    rect(4, 12, 16, 6, Color(0xFF34495E))

                    // Crab Body Shell
                    rect(8, 7, 8, 5, crabRed)
                    rect(9, 8, 6, 3, crabOrange)

                    // Big Crab Claws
                    rect(4, 6, 4, 3, crabRed)
                    rect(16, 6, 4, 3, crabRed)
                    px(4, 5, crabOrange)
                    px(19, 5, crabOrange)

                    // Crispy Golden Garlic Bed
                    for (x in 5..18 step 2) {
                        px(x, 12, garlicGold)
                        px(x + 1, 13, garlicGold)
                        px(x, 14, garlicGold)
                    }
                    px(8, 11, chiliRed)
                    px(14, 12, chiliRed)
                    px(11, 13, chiliRed)
                }

                dishName.contains("包") || dishName.contains("點心") -> {
                    // 16-Bit 酥皮叉燒包 / 港式點心 (Dim Sum)
                    val steamWood = Color(0xFFB9770E)
                    val steamDark = Color(0xFF784212)
                    val bunCrust = Color(0xFFEDBB99)
                    val bunTop = Color(0xFFF5CBA7)
                    val crackGold = Color(0xFFD35400)

                    // Steamer Base
                    rect(3, 13, 18, 6, steamDark)
                    rect(4, 12, 16, 6, steamWood)

                    // 3 Steamed Buns
                    // Bun 1 (Left)
                    rect(5, 7, 5, 5, bunCrust)
                    rect(6, 6, 3, 2, bunTop)
                    px(7, 7, crackGold)

                    // Bun 2 (Center High)
                    rect(10, 5, 5, 5, bunCrust)
                    rect(11, 4, 3, 2, bunTop)
                    px(12, 5, crackGold)

                    // Bun 3 (Right)
                    rect(14, 7, 5, 5, bunCrust)
                    rect(15, 6, 3, 2, bunTop)
                    px(16, 7, crackGold)
                }

                else -> {
                    // 16-Bit 菠蘿油配熱奶茶 (Pineapple Bun & Milk Tea)
                    val bunGold = Color(0xFFF39C12)
                    val bunHighlight = Color(0xFFF9E79F)
                    val bunCrust = Color(0xFFB9770E)
                    val butterYellow = Color(0xFFFFF9C4)
                    val cupRed = Color(0xFFC0392B)
                    val teaBrown = Color(0xFFD35400)

                    // Pineapple Bun (Left)
                    rect(4, 8, 8, 7, bunGold)
                    // Checkered crust lines
                    for (i in 0..6 step 2) {
                        px(4 + i, 8, bunCrust)
                        px(4 + i, 11, bunCrust)
                    }
                    px(6, 9, bunHighlight)
                    // Butter slab melting in the middle
                    rect(5, 12, 6, 2, butterYellow)

                    // Retro Tea Cup (Right)
                    rect(14, 9, 6, 7, cupRed)
                    rect(15, 8, 4, 2, teaBrown)
                    // Cup Handle
                    px(20, 10, cupRed)
                    px(20, 12, cupRed)
                }
            }
        }
    }
}
