package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_cards")
data class RestaurantCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val district: String,              // 18區之一 (例: "油尖旺", "中西區")
    val signatureDish: String,         // 最出色招牌菜
    val flawTrap: String,              // 伏位 / 雷點
    val priceAvg: Int,                 // 人均消費 HK$
    val ratingStars: Int,              // 1 至 5 星
    val tastingNotes: String,          // 食評隨筆
    val photoUri: String? = null,      // 用戶上傳圖片路徑
    val isClosedMemorial: Boolean = false, // 追憶墓碑卡 (已結業店舖，灰階模式)
    val battleTastePower: Int = 80,    // 戰鬥美味值 (ATK)
    val battleFlawDef: Int = 75,       // 抗伏防禦值 (DEF)
    val createdAt: Long = System.currentTimeMillis()
) {
    val photoUriList: List<String>
        get() = photoUri?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    val firstPhotoUri: String?
        get() = photoUriList.firstOrNull()
}

@Entity(tableName = "food_master_profile")
data class FoodMasterProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val masterName: String = "傳奇食家",
    val title: String = "街坊覓食大師",
    val avatarType: String = "hero_chef", // hero_chef, noodle_sage, roast_knight
    val totalDuelsWon: Int = 0
)
