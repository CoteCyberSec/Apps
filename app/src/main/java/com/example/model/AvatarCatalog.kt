package com.example.model

data class CoupleAvatar(
    val id: String,
    val name: BilingualText,
    val bgTopHex: Long,
    val bgBottomHex: Long,
    val ringHex: Long
)

object AvatarCatalog {
    val presets: List<CoupleAvatar> = listOf(
        CoupleAvatar(
            id = "sunny_cat",
            name = BilingualText("Sunny Cat", "Солнечный кот"),
            bgTopHex = 0xFFFFE5B4L,
            bgBottomHex = 0xFFF7B267L,
            ringHex = 0xFFC66B56L
        ),
        CoupleAvatar(
            id = "moon_bunny",
            name = BilingualText("Moon Bunny", "Лунный зайка"),
            bgTopHex = 0xFFFDE2E4L,
            bgBottomHex = 0xFFF2A7B3L,
            ringHex = 0xFF8C3B47L
        ),
        CoupleAvatar(
            id = "cozy_bear",
            name = BilingualText("Cozy Bear", "Уютный мишка"),
            bgTopHex = 0xFFF3D5B5L,
            bgBottomHex = 0xFFD4A373L,
            ringHex = 0xFF7F4F24L
        ),
        CoupleAvatar(
            id = "dreamy_fox",
            name = BilingualText("Dreamy Fox", "Мечтательный лис"),
            bgTopHex = 0xFFFFD6BAL,
            bgBottomHex = 0xFFE07A5FL,
            ringHex = 0xFF9E3B2BL
        ),
        CoupleAvatar(
            id = "starry_owl",
            name = BilingualText("Starry Owl", "Звёздная сова"),
            bgTopHex = 0xFF3D5A80L,
            bgBottomHex = 0xFF293241L,
            ringHex = 0xFFC89B6EL
        ),
        CoupleAvatar(
            id = "matcha_frog",
            name = BilingualText("Matcha Frog", "Матча-лягушонок"),
            bgTopHex = 0xFFD8E2DCL,
            bgBottomHex = 0xFF84A98CL,
            ringHex = 0xFF4A6B53L
        ),
        CoupleAvatar(
            id = "espresso_shiba",
            name = BilingualText("Coffee Shiba", "Кофейный сиба"),
            bgTopHex = 0xFFFFF1E6L,
            bgBottomHex = 0xFFCB997EL,
            ringHex = 0xFF6B4226L
        ),
        CoupleAvatar(
            id = "berry_panda",
            name = BilingualText("Berry Panda", "Ягодная панда"),
            bgTopHex = 0xFFF3D8E6L,
            bgBottomHex = 0xFFC89EB8L,
            ringHex = 0xFF6D3B5EL
        ),
        CoupleAvatar(
            id = "honey_bee",
            name = BilingualText("Honey Bee", "Медовая пчёлка"),
            bgTopHex = 0xFFFFF3B0L,
            bgBottomHex = 0xFFE9C46AL,
            ringHex = 0xFF9C6644L
        ),
        CoupleAvatar(
            id = "cloud_koala",
            name = BilingualText("Cloud Koala", "Облачный коала"),
            bgTopHex = 0xFFE0F2F1L,
            bgBottomHex = 0xFF90B4CEL,
            ringHex = 0xFF3D5A80L
        )
    )

    fun findById(id: String?): CoupleAvatar? {
        if (id.isNullOrBlank()) return null
        return presets.firstOrNull { it.id == id }
    }

    fun isValidPresetId(id: String): Boolean {
        return id.isEmpty() || presets.any { it.id == id }
    }

    fun slotForUser(couple: CoupleSpace, userId: String): Int {
        return if (couple.creatorUid.isBlank() || couple.creatorUid == userId) 1 else 2
    }

    fun resolveForPartner(
        couple: CoupleSpace,
        authorUid: String,
        speakerRole: String
    ): String {
        val isPartnerOne = if (couple.creatorUid.isBlank()) {
            speakerRole != "partner_local"
        } else {
            (authorUid == couple.creatorUid && speakerRole != "partner_local")
        }
        return if (isPartnerOne) couple.partnerOneAvatarId else couple.partnerTwoAvatarId
    }
}
