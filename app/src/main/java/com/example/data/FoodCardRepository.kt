package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FoodCardRepository(private val dao: FoodCardDao) {
    val allCards: Flow<List<RestaurantCardEntity>> = dao.getAllCards()
    val profile: Flow<FoodMasterProfileEntity?> = dao.getProfile()

    suspend fun insertCard(card: RestaurantCardEntity): Long = dao.insertCard(card)

    suspend fun updateCard(card: RestaurantCardEntity) = dao.updateCard(card)

    suspend fun deleteCard(card: RestaurantCardEntity) = dao.deleteCard(card)

    suspend fun deleteCardById(id: Long) = dao.deleteCardById(id)

    suspend fun updateProfile(profile: FoodMasterProfileEntity) = dao.insertOrUpdateProfile(profile)

    suspend fun initializeDefaultDataIfEmpty() {
        val existingCards = dao.getAllCards().firstOrNull()
        if (existingCards.isNullOrEmpty()) {
            val sampleCards = listOf(
                RestaurantCardEntity(
                    name = "九記牛腩",
                    district = "中西區",
                    signatureDish = "清湯牛腩粗麵",
                    flawTrap = "店員態度急躁、店面狹窄需搭檯",
                    priceAvg = 85,
                    ratingStars = 4,
                    tastingNotes = "湯底濃郁藥膳香，爽腩肉質酥化，伊麵吸盡精華！",
                    isClosedMemorial = false,
                    battleTastePower = 90,
                    battleFlawDef = 58
                ),
                RestaurantCardEntity(
                    name = "澳洲牛奶公司",
                    district = "油尖旺",
                    signatureDish = "厚蛋多士配熱奶茶",
                    flawTrap = "光速收碟壓迫感極高、催促離座",
                    priceAvg = 55,
                    ratingStars = 5,
                    tastingNotes = "黃金滑蛋牛油香撲鼻，多士香脆，光速用餐傳奇體驗！",
                    isClosedMemorial = false,
                    battleTastePower = 96,
                    battleFlawDef = 50
                ),
                RestaurantCardEntity(
                    name = "珍寶海鮮舫",
                    district = "南區",
                    signatureDish = "避風塘金蒜炒蟹",
                    flawTrap = "傳奇畫舫不幸沉沒歷史記憶，已無法再訪",
                    priceAvg = 380,
                    ratingStars = 4,
                    tastingNotes = "香港水上皇宮時代象徵，炒蟹香辣鑊氣十足，只能留存回憶。",
                    isClosedMemorial = true, // 追憶墓碑卡
                    battleTastePower = 88,
                    battleFlawDef = 70
                ),
                RestaurantCardEntity(
                    name = "添好運點心專門店",
                    district = "深水埗",
                    signatureDish = "酥皮焗叉燒包",
                    flawTrap = "排隊時間長，環境較為熱鬧嘈吵",
                    priceAvg = 65,
                    ratingStars = 5,
                    tastingNotes = "酥皮香甜薄脆，叉燒餡熱辣爆汁，平民米芝蓮神話！",
                    isClosedMemorial = false,
                    battleTastePower = 93,
                    battleFlawDef = 80
                ),
                RestaurantCardEntity(
                    name = "大安茶氷廳",
                    district = "油尖旺",
                    signatureDish = "焦糖燉蛋菠蘿包",
                    flawTrap = "客滿時限時60分鐘，熱門時段需候位",
                    priceAvg = 62,
                    ratingStars = 4,
                    tastingNotes = "老冰室創新之作，現烤脆皮與法式布甸完美碰撞！",
                    isClosedMemorial = false,
                    battleTastePower = 87,
                    battleFlawDef = 76
                ),
                RestaurantCardEntity(
                    name = "皇后飯店",
                    district = "灣仔",
                    signatureDish = "正宗俄羅斯羅宋湯",
                    flawTrap = "舊舖結業多次遷移，價位偏中高",
                    priceAvg = 135,
                    ratingStars = 4,
                    tastingNotes = "牛腩牛骨熬煮蔬菜甜酸回甘，配熱餐包回味老香港情懷。",
                    isClosedMemorial = false,
                    battleTastePower = 82,
                    battleFlawDef = 82
                ),
                RestaurantCardEntity(
                    name = "陳根記",
                    district = "沙田",
                    signatureDish = "Wasabi 手撕雞",
                    flawTrap = "搬入商場後排檔氣氛較弱，芥末嗆喉",
                    priceAvg = 110,
                    ratingStars = 4,
                    tastingNotes = "海蜇手撕雞冰涼彈牙，芥末衝鼻過癮，禾輋村經典回憶！",
                    isClosedMemorial = false,
                    battleTastePower = 89,
                    battleFlawDef = 74
                ),
                RestaurantCardEntity(
                    name = "群記牛肉圓豬手",
                    district = "大埔",
                    signatureDish = "爽脆南乳豬手配牛爽腩",
                    flawTrap = "營業時間短，下午3點經常沽清收檔",
                    priceAvg = 52,
                    ratingStars = 5,
                    tastingNotes = "豬手去骨爽口毫無腥膩，牛爽腩肉汁豐盈，大埔街坊第一名！",
                    isClosedMemorial = false,
                    battleTastePower = 95,
                    battleFlawDef = 66
                ),
                RestaurantCardEntity(
                    name = "勝香園",
                    district = "中西區",
                    signatureDish = "蕃茄鮮牛肉公仔麵",
                    flawTrap = "露天大排檔夏天極熱、雨天受阻",
                    priceAvg = 48,
                    ratingStars = 4,
                    tastingNotes = "鮮熬濃郁蕃茄蓉鋪滿公仔麵，牛油檸檬蜜糖脆脆必食！",
                    isClosedMemorial = false,
                    battleTastePower = 86,
                    battleFlawDef = 62
                )
            )
            sampleCards.forEach { dao.insertCard(it) }
        }

        val existingProfile = dao.getProfile().firstOrNull()
        if (existingProfile == null) {
            dao.insertOrUpdateProfile(
                FoodMasterProfileEntity(
                    id = 1,
                    masterName = "赤子食客",
                    title = "街坊美食大師",
                    avatarType = "hero_chef",
                    totalDuelsWon = 12
                )
            )
        }
    }
}
