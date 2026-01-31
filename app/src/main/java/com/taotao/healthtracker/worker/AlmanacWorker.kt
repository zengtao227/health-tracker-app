package com.taotao.healthtracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.taotao.healthtracker.data.AppDatabase
import com.taotao.healthtracker.data.entity.AlmanacData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.taotao.healthtracker.domain.RealLunarCalendar

class AlmanacWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // 十二建除 (The 12 Day Officers) - 真实黄历的核心循环
    private val OFFICERS = listOf(
        "建" to ("出行 访友" to "动土 开仓"),
        "除" to ("治病 扫舍" to "嫁娶 出货"),
        "满" to ("祈福 祭祀" to "动土 栽种"),
        "平" to ("修饰 涂泥" to "移徙 入宅"),
        "定" to ("交易 立券" to "词讼 出行"),
        "执" to ("捕捉 畋猎" to "开市 纳财"),
        "破" to ("求医 治病" to "嫁娶 移徙"),
        "危" to ("安床 纳财" to "登山 乘船"),
        "成" to ("开市 交易" to "词讼 争斗"),
        "收" to ("纳财 捕捉" to "安葬 下葬"),
        "开" to ("祭祀 祈福" to "安葬 动土"),
        "闭" to ("安床 修造" to "出行 针灸")
    )

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            
            // Dynamic Perpetual Generation
            // Detect current year and generate data for Current Year + Next Year
            // This ensures the app works forever (e.g. in 2030, 2040)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val yearsToGenerate = listOf(currentYear, currentYear + 1)

            yearsToGenerate.forEach { year ->
                val yearData = generateAlmanacForYear(year)
                yearData.forEach { db.healthDao().insertAlmanac(it) }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun generateAlmanacForYear(year: Int): List<AlmanacData> {
        val list = mutableListOf<AlmanacData>()
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Start from Jan 1 of the target year
        calendar.set(year, Calendar.JANUARY, 1)
        
        // Jian-Chu Cycle Offset Calculation
        // In a real sophisticated engine, this offset varies by solar terms.
        // For this perpetual algorithm, we use a consistent hash of the year to determine the start officer.
        // This ensures Determinism: 2026 will always start with the same officer, regardless of when it's generated.
        var officerIndex = (year * 13) % 12 

        val daysInYear = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 366 else 365

        for (i in 0 until daysInYear) {
                val dateStr = sdf.format(calendar.time)
            
            // Authentic Lunar Conversion
            // Replaced naive math with RealLunarCalendar logic anchored to real 2026 data
            val lunarStr = RealLunarCalendar.solarToLunar(calendar.time)

            val officer = OFFICERS[officerIndex % 12]
            
            list.add(AlmanacData(
                date = dateStr,
                yi = officer.second.first,
                ji = officer.second.second,
                lunarDate = lunarStr
            ))

            // Move to next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            officerIndex++
        }
        return list
    }

    private fun getLunarString(month: Int, day: Int): String {
        val monthStr = when(month) {
            1 -> "正月"; 11 -> "冬月"; 12 -> "腊月"; else -> "${month}月"
        }
        val dayStr = when {
            day == 1 -> "初一"; day == 15 -> "十五"
            day <= 10 -> "初${dayMap(day)}"
            day < 20 -> "十${dayMap(day % 10)}"
            day == 20 -> "二十"
            day < 30 -> "廿${dayMap(day % 10)}"
            else -> "三十"
        }
        return "$monthStr$dayStr"
    }

    private fun dayMap(d: Int): String = when(d) {
        1->"一"; 2->"二"; 3->"三"; 4->"四"; 5->"五"; 6->"六"; 7->"七"; 8->"八"; 9->"九"; 0->""; else->""
    }
}
