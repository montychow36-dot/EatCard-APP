package com.example.data

data class DistrictInfo(
    val name: String,
    val region: String, // "香港島", "九龍", "新界與離島"
    val code: String,
    val landmarkDesc: String
)

object HongKongDistricts {
    val REGION_HK_ISLAND = "香港島"
    val REGION_KOWLOON = "九龍"
    val REGION_NT = "新界與離島"

    val ALL_DISTRICTS = listOf(
        // 香港島
        DistrictInfo("中西區", REGION_HK_ISLAND, "CW", "老牌茶室、中上環特色洋館與大排檔"),
        DistrictInfo("灣仔", REGION_HK_ISLAND, "WC", "叮叮車沿線傳統冰室與新派酒館"),
        DistrictInfo("東區", REGION_HK_ISLAND, "E", "北角雞蛋仔、筲箕灣東大街老食坊"),
        DistrictInfo("南區", REGION_HK_ISLAND, "S", "香港仔避風塘海鮮與赤柱異國情調"),

        // 九龍
        DistrictInfo("油尖旺", REGION_KOWLOON, "YTM", "旺角油麻地茶餐廳聚集之王"),
        DistrictInfo("深水埗", REGION_KOWLOON, "SSP", "平民美食聖地、豆腐花與米芝蓮小吃"),
        DistrictInfo("九龍城", REGION_KOWLOON, "KC", "小泰國風情、老牌潮州菜與火鍋"),
        DistrictInfo("黃大仙", REGION_KOWLOON, "WTS", "冬菇亭熟食檔與慈雲山招牌煲仔飯"),
        DistrictInfo("觀塘", REGION_KOWLOON, "KT", "工廈隱世西餐、裕民坊重塑小檔"),

        // 新界與離島
        DistrictInfo("荃灣", REGION_KOWLOON, "TW", "路德圍小吃一條街、傳統麵店"),
        DistrictInfo("葵青", REGION_NT, "KC2", "葵涌廣場掃街熱門勝地"),
        DistrictInfo("沙田", REGION_NT, "ST", "大排檔雞粥、乳鴿與瀝源古早味"),
        DistrictInfo("大埔", REGION_NT, "TP", "大埔墟街市熟食中心與地道牛肉麵"),
        DistrictInfo("北區", REGION_NT, "N", "粉嶺聯和墟豬手肉餅與鄉村農家菜"),
        DistrictInfo("元朗", REGION_NT, "YL", "老婆餅發源地、圍村盆菜與B仔涼粉"),
        DistrictInfo("屯門", REGION_NT, "TM", "三聖海鮮、紅橋宵夜與日式小店"),
        DistrictInfo("西貢", REGION_NT, "SK", "香港後花園、海旁海鮮酒家與露天Café"),
        DistrictInfo("離島", REGION_NT, "ISL", "長洲巨型芒果糯米糍、大澳蝦醬美食")
    )
}
