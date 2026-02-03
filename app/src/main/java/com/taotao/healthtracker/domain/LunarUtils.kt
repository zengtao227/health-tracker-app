@file:Suppress("SpellCheckingInspection", "unused")

package com.taotao.healthtracker.domain

import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.util.Date

/**
 * 黄历工具类 - 使用 lunar-java 专业库
 * 
 * 技术规范：
 * - 使用 cn.6tail:lunar 库 (MIT License, 1900-2100 年覆盖)
 * - 精确处理闰月、大小月、立春分界
 * - 完整支持建除十二神、宜忌、冲煞、吉神凶煞
 */
object LunarUtils {

    data class AlmanacResult(
        val lunar: String,      // 干支年+生肖+农历月日+建除神
        val yi: String,         // 宜
        val ji: String,         // 忌
        val tianShen: String = "",   // 值神 (如: 司命)
        val tianShenType: String = "", // 黄道/黑道
        val chongSha: String = "",  // 冲煞
        val jiShen: String = "",    // 吉神
        val xiongSha: String = "",  // 凶煞
        val jiShi: String = ""      // 吉时参考
    )

    /**
     * 获取完整黄历信息
     * @param date 日期对象，默认为当前时间
     * @return AlmanacResult 包含农历、干支、宜忌等完整信息
     */
    fun getLocalAlmanac(date: Date = Date()): AlmanacResult {
        // 使用专业库转换
        val solar = Solar.fromDate(date)
        val lunar = solar.lunar

        // 1. 基础日期：干支年 + 生肖 + 农历月日
        val yearGanZhi = lunar.yearInGanZhi      // 如 "乙巳"
        val animal = lunar.yearShengXiao          // 如 "蛇"
        val monthChinese = lunar.monthInChinese   // 如 "正月"
        val dayChinese = lunar.dayInChinese       // 如 "初一"

        // 2. 建除十二神与值神
        val zhiXing = lunar.zhiXing               // 如 "建"
        val tianShen = lunar.dayTianShen          // 值神 (如 "司命")
        val tianShenType = lunar.dayTianShenType  // 黄道/黑道

        // 3. 宜忌 (库返回 List<String>)
        val yiList = lunar.dayYi ?: emptyList()
        val jiList = lunar.dayJi ?: emptyList()

        // 防碎词处理：词内部用 \u2060 绑定，词之间用空格分隔
        val WJ = "\u2060"
        val formatPhrases: (List<String>, Int) -> String = { list, count ->
            if (list.isEmpty()) "诸事不宜"
            else list.take(count).joinToString(" ") { word -> word.chunked(1).joinToString(WJ) }
        }

        val yiFormatted = formatPhrases(yiList, 15)
        val jiFormatted = formatPhrases(jiList, 10)

        // 4. 冲煞信息
        val chong = lunar.dayChongDesc ?: lunar.dayChong ?: ""
        val sha = lunar.daySha ?: ""
        val chongSha = if (chong.isNotEmpty()) "冲${chong}煞${sha}" else ""

        // 5. 吉神凶煞
        val jiShenList = lunar.dayJiShen ?: emptyList()
        val xiongShaList = lunar.dayXiongSha ?: emptyList()
        val jiShen = jiShenList.take(6).joinToString("、").ifEmpty { "无" }
        val xiongSha = xiongShaList.take(6).joinToString("、").ifEmpty { "无" }

        // 6. 吉时参考 (取几个关键时辰)
        val jiShiBuilder = StringBuilder()
        val hourTimes = lunar.times
        // 取前几个或全部
        hourTimes.take(12).forEach { lt ->
            val hourRange = when(lt.zhi) {
                "子" -> "23:00-00:59"; "丑" -> "01:00-02:59"; "寅" -> "03:00-04:59"
                "卯" -> "05:00-06:59"; "辰" -> "07:00-08:59"; "巳" -> "09:00-10:59"
                "午" -> "11:00-12:59"; "未" -> "13:00-14:59"; "申" -> "15:00-16:59"
                "酉" -> "17:00-18:59"; "戌" -> "19:00-20:59"; "亥" -> "21:00-22:59"
                else -> ""
            }
            if (lt.yi.isNotEmpty() && lt.yi[0] != "无") {
                jiShiBuilder.append("${lt.zhi}时($hourRange) 宜: ${lt.yi.take(3).joinToString(" ")}\n")
            }
        }
        val jiShi = jiShiBuilder.toString().trim()

        // 组装农历显示字符串
        val lunarStr = "${yearGanZhi}${animal}年 · ${monthChinese}月${dayChinese} [${zhiXing}日]"

        return AlmanacResult(
            lunar = lunarStr,
            yi = yiFormatted,
            ji = jiFormatted,
            tianShen = tianShen,
            tianShenType = tianShenType,
            chongSha = chongSha,
            jiShen = jiShen,
            xiongSha = xiongSha,
            jiShi = jiShi
        )
    }

    /**
     * 获取西式星座
     * @param month 月份 (1-12)
     * @param day 日期 (1-31)
     * @return 星座英文名
     */
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

    /**
     * 获取星座能量洞察
     */
    fun getHoroscopeInsight(zodiac: String): Pair<String, String> {
        val energies = mapOf(
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
        return energies[zodiac] ?: ("Neutral" to "Routine")
    }
}
