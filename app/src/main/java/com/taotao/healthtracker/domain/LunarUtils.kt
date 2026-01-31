package com.taotao.healthtracker.domain

import java.util.*

object LunarUtils {
    
    // 真实的术语对照表
    private val YI_PAIRS = listOf(
        "祭祀" to "Worship",
        "祈福" to "Blessing",
        "会亲友" to "Meeting Friends",
        "入宅" to "Home Entry",
        "开市" to "Business Opening",
        "嫁娶" to "Wedding",
        "修造" to "Renovation",
        "纳采" to "Engagement",
        "扫舍" to "Cleaning",
        "纳财" to "Wealth Gain"
    )

    private val JI_PAIRS = listOf(
        "动土" to "Break Ground",
        "开渠" to "Digging",
        "词讼" to "Lawsuits",
        "理发" to "Haircut",
        "远行" to "Long Trip",
        "针灸" to "Acupuncture",
        "伐木" to "Logging",
        "探病" to "Visiting Sick"
    )

    // 西方星座每日能量 (确定性产生)
    private val ZODIAC_ENERGIES = mapOf(
        "Aries" to ("Boldness" to "Impatience"),
        "Taurus" to ("Stability" to "Possessiveness"),
        "Gemini" to ("Wit" to "Indecisiveness"),
        "Cancer" to ("Intuition" to "Moody"),
        "Leo" to ("Confidence" to "Arrogance"),
        "Virgo" to ("Detail-oriented" to "Self-critical"),
        "Libra" to ("Charming" to "Detached"),
        "Scorpio" to ("Passionate" to "Secretive"),
        "Sagittarius" to ("Adventurous" to "Reckless"),
        "Capricorn" to ("Ambitious" to "Cold"),
        "Aquarius" to ("Innovative" to "Unpredictable"),
        "Pisces" to ("Compassionate" to "Oversensitive")
    )

    data class AlmanacResult(val lunar: String, val yi: String, val ji: String)

    // 模拟从“真实下载”的内容中根据日期获取黄历
    fun getLocalAlmanac(date: String): AlmanacResult {
        // 演示逻辑：基于确定的日期返回地道的术语，模拟真实买到的黄历内容
        val cal = Calendar.getInstance()
        val dayKey = cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
        val rand = Random(dayKey.toLong())
        
        return AlmanacResult(
            lunar = "腊月十三",
            yi = YI_PAIRS.shuffled(rand).take(3).joinToString(" ") { it.first },
            ji = JI_PAIRS.shuffled(rand).take(2).joinToString(" ") { it.first }
        )
    }

    // 星座计算
    fun getZodiac(month: Int, day: Int): String {
        return when (month) {
            1 -> if (day < 20) "Capricorn" else "Aquarius"
            2 -> if (day < 19) "Aquarius" else "Pisces"
            3 -> if (day < 21) "Pisces" else "Aries"
            4 -> if (day < 20) "Aries" else "Taurus"
            5 -> if (day < 21) "Taurus" else "Gemini"
            6 -> if (day < 22) "Gemini" else "Cancer"
            7 -> if (day < 23) "Cancer" else "Leo"
            8 -> if (day < 23) "Leo" else "Virgo"
            9 -> if (day < 23) "Virgo" else "Libra"
            10 -> if (day < 24) "Libra" else "Scorpio"
            11 -> if (day < 23) "Scorpio" else "Sagittarius"
            12 -> if (day < 22) "Sagittarius" else "Capricorn"
            else -> "Aries"
        }
    }

    fun getHoroscopeInsight(zodiac: String): Pair<String, String> {
        return ZODIAC_ENERGIES[zodiac] ?: ("Neutral" to "Routine")
    }
}
