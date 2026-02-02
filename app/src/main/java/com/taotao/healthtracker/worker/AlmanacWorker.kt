package com.taotao.healthtracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nlf.calendar.Solar
import com.taotao.healthtracker.data.AppDatabase
import com.taotao.healthtracker.data.entity.AlmanacData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 黄历数据预生成 Worker
 * 
 * 使用 lunar-java 专业库生成精确的农历数据
 * 覆盖 1900-2100 年，自动处理闰月和立春分界
 */
class AlmanacWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            
            // 动态生成：当前年份 + 下一年
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

    /**
     * 使用专业库生成整年的黄历数据
     */
    private fun generateAlmanacForYear(year: Int): List<AlmanacData> {
        val list = mutableListOf<AlmanacData>()
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        calendar.set(year, Calendar.JANUARY, 1)
        val daysInYear = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 366 else 365

        for (i in 0 until daysInYear) {
            val dateStr = sdf.format(calendar.time)
            
            // 使用专业库获取精确的农历数据
            val solar = Solar.fromDate(calendar.time)
            val lunar = solar.lunar

            // 农历日期字符串
            val lunarStr = "${lunar.monthInChinese}月${lunar.dayInChinese}"

            // 宜忌（库返回完整数据）
            val yiList = lunar.dayYi ?: emptyList<String>()
            val jiList = lunar.dayJi ?: emptyList<String>()
            
            // 取前 4 项展示（简洁版）
            val yiStr = if (yiList.isEmpty()) "诸事不宜" else yiList.take(4).joinToString(" ")
            val jiStr = if (jiList.isEmpty()) "诸事不宜" else jiList.take(4).joinToString(" ")

            list.add(AlmanacData(
                date = dateStr,
                yi = yiStr,
                ji = jiStr,
                lunarDate = lunarStr
            ))

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }
}
