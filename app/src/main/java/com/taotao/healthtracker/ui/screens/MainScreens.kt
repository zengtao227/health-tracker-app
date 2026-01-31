package com.taotao.healthtracker.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import java.io.File
import androidx.core.content.FileProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.taotao.healthtracker.data.entity.HealthRecord
import com.taotao.healthtracker.data.entity.UserProfile
import com.taotao.healthtracker.domain.DateUtils
import com.taotao.healthtracker.domain.LunarUtils
import com.taotao.healthtracker.viewmodel.HealthViewModel
import com.taotao.healthtracker.ui.L10n
import java.text.SimpleDateFormat
import java.util.*

val ColorBpRed = Color(0xFFFF4848)
val ColorBpBlue = Color(0xFF2196F3)
val ColorHrOrange = Color(0xFFFF9800)
val ColorWeightPurple = Color(0xFF9C27B0)
val ColorGreen = Color(0xFF43A047)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(viewModel: HealthViewModel, onSaveSuccess: () -> Unit) {
    val profiles by viewModel.userProfiles.collectAsState()
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    
    val appLang = activeProfile?.language ?: "zh"
    val insLang = activeProfile?.insightLanguage ?: "zh"
    val almanac by viewModel.currentAlmanac.collectAsState()
    
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val westernDateStr = remember(appLang) { 
        if(appLang == "zh") SimpleDateFormat("M月d日").format(Date()) 
        else SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(Date()) 
    }

    var isExpanded by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var monthInput by remember { mutableStateOf("") }
    var dayInput by remember { mutableStateOf("") }

    var sbpText by remember { mutableStateOf("") }
    var dbpText by remember { mutableStateOf("") }
    var hrText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }

    LaunchedEffect(activeProfile) {
        activeProfile?.let {
            nameInput = it.name
            heightInput = it.height.toString()
            yearInput = it.birthYear.toString()
            monthInput = it.birthMonth.toString()
            dayInput = it.birthDay.toString()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 2.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(4.dp))
        
        // --- 1. Enhanced Profile Header ---
        Card(
            modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.08f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${L10n.get("active_user", appLang)}: ${activeProfile?.name ?: "P1"}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${L10n.get("ht", appLang)}: ${activeProfile?.height ?: 0f}cm | ${L10n.get("born", appLang)}: ${activeProfile?.birthYear}-${activeProfile?.birthMonth}-${activeProfile?.birthDay}", 
                             style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    // Dual Language Switcher (Compact & Logic Corrected)
                    Column(horizontalAlignment = Alignment.End) {
                        TextButton(onClick = { activeProfile?.let { viewModel.saveProfile(it.copy(language = if(appLang == "zh") "en" else "zh")) } }, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp), modifier = Modifier.height(24.dp)) {
                            Text(if(appLang == "zh") "English" else "中文", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        TextButton(onClick = { activeProfile?.let { viewModel.saveProfile(it.copy(insightLanguage = if(insLang == "zh") "en" else "zh")) } }, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp), modifier = Modifier.height(24.dp)) {
                            Text(if(insLang == "zh") "Zodiac" else "黄历", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (isExpanded) {
                    Divider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text(L10n.get("user_name", appLang)) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = heightInput, onValueChange = { heightInput = it }, label = { Text("Ht (cm)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = yearInput, onValueChange = { yearInput = it }, label = { Text("YYYY") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = monthInput, onValueChange = { monthInput = it }, label = { Text("MM") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = dayInput, onValueChange = { dayInput = it }, label = { Text("DD") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    
                    Button(onClick = {
                        activeProfile?.let { 
                            viewModel.saveProfile(it.copy(
                                name = nameInput, 
                                height = heightInput.toFloatOrNull() ?: it.height, 
                                birthYear = yearInput.toIntOrNull() ?: it.birthYear,
                                birthMonth = monthInput.toIntOrNull() ?: it.birthMonth,
                                birthDay = dayInput.toIntOrNull() ?: it.birthDay
                            )) 
                        }
                        isExpanded = false
                    }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text(L10n.get("save", appLang)) }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- 2. Central Date Display ---
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(westernDateStr, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            if (appLang == "zh") {
                Text("乙巳年 [蛇年] · ${almanac.lunar}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                val cal = java.util.Calendar.getInstance()
                val dow = java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH).format(cal.time)
                val z = com.taotao.healthtracker.domain.LunarUtils.getZodiac(cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
                Text("$dow · $z Season", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(Modifier.height(12.dp))
            Text(L10n.get("daily_log", appLang).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        }

        Spacer(Modifier.height(12.dp))

        // --- 3. Body Inputs ---
        Column(modifier = Modifier.width(310.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = sbpText, onValueChange = { sbpText = it }, label = { Text(L10n.get("sbp", appLang)) }, textStyle = MaterialTheme.typography.displaySmall, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = dbpText, onValueChange = { dbpText = it }, label = { Text(L10n.get("dbp", appLang)) }, textStyle = MaterialTheme.typography.displaySmall, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = hrText, onValueChange = { hrText = it }, label = { Text(L10n.get("hr", appLang)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text(L10n.get("weight", appLang)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                val s = sbpText.toIntOrNull(); val d = dbpText.toIntOrNull(); val h = hrText.toIntOrNull(); val w = weightText.toFloatOrNull() ?: 0f
                if (s != null && d != null && h != null) {
                    viewModel.saveRecord(HealthRecord(userId = activeProfile?.id ?: 1, date = DateUtils.getCurrentDate(), sbp = s, dbp = d, hr = h, weight = w))
                    sbpText = ""; dbpText = ""; hrText = ""; weightText = ""
                    onSaveSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth(0.9f).height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) { Text(L10n.get("save_analysis", appLang), fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(32.dp))

        // --- 4. The Culture decoupled Insight Card ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f))
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (insLang == "zh") {
                    // --- MODE: AUTHENTIC ALMANAC (Real or Local) ---
                    Text("今日黄历 · ${almanac.lunar}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("宜", color = ColorGreen, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            Text(almanac.yi, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.onSurface.copy(0.1f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("忌", color = ColorBpRed, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            Text(almanac.ji, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                } else {
                    // --- MODE: WESTERN ZODIAC (星座) ---
                    val zodiac = LunarUtils.getZodiac(activeProfile?.birthMonth ?: 1, activeProfile?.birthDay ?: 1)
                    val horo = LunarUtils.getHoroscopeInsight(zodiac)
                    Text("$zodiac Daily Horoscope", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STRENGTH", color = ColorBpBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(horo.first, fontSize = 14.sp)
                        }
                        Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.onSurface.copy(0.1f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BEWARE", color = ColorHrOrange, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(horo.second, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// StatsScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: HealthViewModel) {
    val records by viewModel.allRecords.collectAsState()
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    val lang = activeProfile?.language ?: "zh"
    var filterRange by remember { mutableStateOf("All") }
    
    val filtered = remember(records, filterRange) {
        when(filterRange) {
            "7D" -> records.filter { DateUtils.getDaysDiff(it.date) <= 7 }
            "30D" -> records.filter { DateUtils.getDaysDiff(it.date) <= 30 }
            else -> records
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${L10n.get("nav_trends", lang)}: ${activeProfile?.name ?: "P1"}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f)) // Push filters to the right
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf("7D", "30D", "1Y", "All").forEach { r ->
                    FilterChip(selected = filterRange == r, onClick = { filterRange = r }, label = { Text(r, fontSize = 10.sp) }, modifier = Modifier.padding(start = 2.dp)) // Tighter padding
                }
                Spacer(Modifier.width(8.dp))
                // Compact Language Switcher
                TextButton(onClick = { activeProfile?.let { viewModel.saveProfile(it.copy(language = if(lang == "zh") "en" else "zh")) } }, contentPadding = PaddingValues(0.dp)) {
                    Text(if(lang == "zh") "En" else "中", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (filtered.isNotEmpty()) {
            val last = filtered.first()
            val heightM = (activeProfile?.height ?: 175f) / 100f
            val bmi = if (heightM > 0) (last.weight ?: 0f) / (heightM * heightM) else 0f
            val sbp = last.sbp ?: 0
            val dbp = last.dbp ?: 0
            
            // Interactive States
            var selectedBp by remember { mutableStateOf("") }
            var selectedWt by remember { mutableStateOf("") }

            // 1. BMI Card (Soft Gradient Segments)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.1f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Text(L10n.get("bmi_live", lang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                         Spacer(Modifier.width(8.dp))
                         val statusKey = when { bmi < 18.5 -> "bmi_status_under"; bmi < 24.9 -> "bmi_status_healthy"; bmi < 29.9 -> "bmi_status_over"; else -> "bmi_status_obese" }
                         Text("${String.format("%.1f", bmi)} (${L10n.get(statusKey, lang)})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    SoftSegmentGauge(value = bmi, min = 15f, max = 35f, 
                        segments = listOf(18.5f to ColorBpBlue, 24.9f to ColorGreen, 29.9f to ColorHrOrange, 35f to ColorBpRed),
                        label = "")
                }
            }
            
            // 2. Unified BP Gauge (WHO Classification)
            // Logic: Max(Grade(SBP), Grade(DBP))
            // 0:Optimal, 1:Normal, 2:HighNormal, 3:Grade1, 4:Grade2, 5:Grade3
            fun getBpGrade(s: Int, d: Int): Int {
                val sG = when { s < 120 -> 0; s < 130 -> 1; s < 140 -> 2; s < 160 -> 3; s < 180 -> 4; else -> 5 }
                val dG = when { d < 80 -> 0; d < 85 -> 1; d < 90 -> 2; d < 100 -> 3; d < 110 -> 4; else -> 5 }
                return maxOf(sG, dG)
            }
            val bpGrade = getBpGrade(sbp, dbp)
            val bpLabel = when(bpGrade) {
                0 -> if(lang=="zh") "理想" else "Optimal"
                1 -> if(lang=="zh") "正常" else "Normal"
                2 -> if(lang=="zh") "正常高值" else "High Normal"
                3 -> if(lang=="zh") "1级高血压" else "Grade 1"
                4 -> if(lang=="zh") "2级高血压" else "Grade 2"
                else -> if(lang=="zh") "3级高血压" else "Grade 3"
            }
            val bpColor = when(bpGrade) {
                0 -> ColorGreen
                1 -> Color(0xFF8BC34A) // Light Green
                2 -> Color(0xFFFFEB3B) // Yellow
                3 -> ColorHrOrange
                4 -> ColorBpRed
                else -> Color(0xFF8B0000) // Dark Red
            }

            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = bpColor.copy(0.1f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if(lang=="zh") "血压分级 (WHO)" else "BP Classification", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(bpLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = bpColor.copy(alpha = 1f))
                     }
                     // 0..6 Scale. Center of block is n+0.5
                     SoftSegmentGauge(value = bpGrade + 0.5f, min = 0f, max = 6f, 
                        segments = listOf(
                            1f to ColorGreen, 
                            2f to Color(0xFF8BC34A), 
                            3f to Color(0xFFFFEB3B), 
                            4f to ColorHrOrange, 
                            5f to ColorBpRed, 
                            6f to Color(0xFF8B0000)
                        ), 
                        label = "")
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(L10n.get("bp_chart", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(0.6f))
                if(selectedBp.isNotEmpty()) Text(selectedBp, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ColorBpRed)
            }
            Spacer(Modifier.height(4.dp))
            BpChart(filtered, lang) { e -> selectedBp = e }
            
            Spacer(Modifier.height(16.dp))
            // WEIGHT ONLY
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                 Text(if(lang=="zh") "体重趋势" else "Weight Trend", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(0.6f))
                 if(selectedWt.isNotEmpty()) Text(selectedWt, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ColorWeightPurple)
            }
            Spacer(Modifier.height(4.dp))
            WeightChart(filtered, lang) { e -> selectedWt = e } // Renamed from VitalChart
            Spacer(Modifier.height(24.dp))
        } else {
            Box(Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) { Text(L10n.get("no_data", lang)) }
        }
    }
}

@Composable
fun BpChart(records: List<HealthRecord>, lang: String, onSelection: (String) -> Unit) {
    val rev = records.reversed()
    val sLabel = if (lang == "zh") "收缩压" else "SBP"
    val dLabel = if (lang == "zh") "舒张压" else "DBP"
    
    AndroidView(factory = { ctx -> LineChart(ctx).apply { 
        description.isEnabled = false; xAxis.position = XAxis.XAxisPosition.BOTTOM; 
        axisRight.isEnabled = false; axisLeft.setSpaceTop(20f); axisLeft.setSpaceBottom(20f)
        xAxis.setDrawGridLines(false); extraBottomOffset = 5f
        axisLeft.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = "${v.toInt()} mmHg" }
        
        // Listener
        setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
             override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                 e?.let { 
                    val idx = it.x.toInt()
                    if(idx in rev.indices) {
                        val r = rev[idx]
                        onSelection("${r.date.takeLast(5)}: ${r.sbp}/${r.dbp}")
                    }
                 }
             }
             override fun onNothingSelected() { onSelection("") }
        })

    }}, update = { chart ->
        val sSet = LineDataSet(rev.mapIndexed { i, r -> Entry(i.toFloat(), r.sbp!!.toFloat()) }, sLabel).apply { color = ColorBpRed.toArgb(); lineWidth = 3f; setDrawValues(false); setDrawCircles(true); setCircleColor(ColorBpRed.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb() }
        val dSet = LineDataSet(rev.mapIndexed { i, r -> Entry(i.toFloat(), r.dbp!!.toFloat()) }, dLabel).apply { color = ColorBpBlue.toArgb(); lineWidth = 3f; setDrawValues(false); setDrawCircles(true); setCircleColor(ColorBpBlue.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb() }
        chart.xAxis.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = if(v.toInt() in rev.indices) rev[v.toInt()].date.takeLast(5) else "" }
        chart.data = LineData(sSet, dSet); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(180.dp))
}

@Composable
fun WeightChart(records: List<HealthRecord>, lang: String, onSelection: (String) -> Unit) {
    val rev = records.reversed()
    val wLabel = if (lang == "zh") "体重 (kg)" else "Weight (kg)"

    AndroidView(factory = { ctx -> LineChart(ctx).apply { 
        description.isEnabled = false; xAxis.position = XAxis.XAxisPosition.BOTTOM; 
        
        // Single Left Axis for Weight
        axisLeft.setSpaceTop(20f); axisLeft.setSpaceBottom(20f)
        axisRight.isEnabled = false // Disable Right Axis
        
        xAxis.setDrawGridLines(false); extraBottomOffset = 5f 
        
        axisLeft.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = "${v.toInt()} kg" }

        // Listener
        setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
             override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                 e?.let { 
                    val idx = it.x.toInt()
                    if(idx in rev.indices) {
                        val r = rev[idx]
                        onSelection("${r.date.takeLast(5)}: ${r.weight} kg")
                    }
                 }
             }
             override fun onNothingSelected() { onSelection("") }
        })
    }}, update = { chart ->
        val wSet = LineDataSet(rev.mapIndexed { i, r -> Entry(i.toFloat(), r.weight!!) }, wLabel).apply { 
            color = ColorWeightPurple.toArgb(); lineWidth = 3f; setDrawCircles(true); setCircleColor(ColorWeightPurple.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb()
        }
        chart.xAxis.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = if(v.toInt() in rev.indices) rev[v.toInt()].date.takeLast(5) else "" }
        chart.data = LineData(wSet); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(180.dp))
}

@Composable
fun KnowledgeScreen(viewModel: HealthViewModel) {
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    val lang = activeProfile?.language ?: "zh"
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(L10n.get("medical_ref", lang), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { activeProfile?.let { viewModel.saveProfile(it.copy(language = if(lang == "zh") "en" else "zh")) } }) {
                Text(if(lang == "zh") "Switch to English" else "切换中文")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(L10n.get("who_bp", lang), style = MaterialTheme.typography.titleMedium, color = ColorBpRed, fontWeight = FontWeight.Bold)
                Divider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                KnowledgeRow("${L10n.get("bp_safe", lang)} (< 120/80)", ColorBpBlue)
                KnowledgeRow("${L10n.get("bp_normal", lang)} (120-139/80-89)", ColorGreen)
                KnowledgeRow("${L10n.get("bp_warning", lang)} (140-159/90-99)", ColorHrOrange)
                KnowledgeRow("${L10n.get("bp_hazard", lang)} (> 160/100)", ColorBpRed)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(L10n.get("bmi_guide", lang), style = MaterialTheme.typography.titleMedium, color = ColorBpBlue, fontWeight = FontWeight.Bold)
                Divider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                KnowledgeRow("${L10n.get("bmi_status_under", lang)} (< 18.5)", ColorBpBlue)
                KnowledgeRow("${L10n.get("bmi_status_healthy", lang)} (18.5-24.9)", ColorGreen)
                KnowledgeRow("${L10n.get("bmi_status_over", lang)} (25-29.9)", ColorHrOrange)
                KnowledgeRow("${L10n.get("bmi_status_obese", lang)} (> 30)", ColorBpRed)
            }
        }
        
        // Data Source Transparency
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if(lang == "zh") "算法与数据来源" else "Source & Algorithm", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Divider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                
                Text(if(lang == "zh") "1. 中文黄历：基于《协纪辨方书》之建除十二神循环推算。" else "1. Almanac: Calculated based on the 'Jian-Chu 12 Day Officers' cycle from traditional astronomy.", fontSize = 13.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(if(lang == "zh") "2. 星座运势：基于西方回归黄道 (Tropical Zodiac) 系统推算。" else "2. Horoscope: Based on the Western Tropical Zodiac system.", fontSize = 13.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(if(lang == "zh") "3. 健康标准：WHO 2024 高血压与BMI指南。" else "3. Health Refs: WHO 2024 Guidelines for Hypertension & BMI.", fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun KnowledgeRow(label: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape)); Spacer(Modifier.width(14.dp)); Text(label, fontSize = 15.sp)
    }
}

@Composable
fun HistoryScreen(viewModel: HealthViewModel) {
    val records by viewModel.allRecords.collectAsState()
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    val lang = activeProfile?.language ?: "zh"
    
    // UI State
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    // I/O Logic
    val context = LocalContext.current
    
    // 1. Import Launcher
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val input = context.contentResolver.openInputStream(it)?.bufferedReader().use { r -> r?.readText() }
                if (input != null) viewModel.importCsv(input)
            } catch(e: Exception) { e.printStackTrace() }
        }
    }
    
    // 2. Save File Launcher
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(viewModel.getExportCsv().toByteArray())
                }
            } catch(e: Exception) { e.printStackTrace() }
        }
    }

    // Dialogs
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(if(lang=="zh") "导出数据" else "Export Data") },
            text = { Text(if(lang=="zh") "请选择导出方式：" else "Choose export method:") },
            confirmButton = {
                // Save to File
                TextButton(onClick = { 
                    showExportDialog = false
                    saveLauncher.launch("HealthRecords_${activeProfile?.name}.csv")
                }) { Text(if(lang=="zh") "保存为文件" else "Save to File") }
            },
            dismissButton = {
                Row {
                    // Share Text
                    TextButton(onClick = { 
                        showExportDialog = false
                        val csv = viewModel.getExportCsv()
                        val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, csv); putExtra(Intent.EXTRA_TITLE, "HealthRecords.csv") }
                        context.startActivity(Intent.createChooser(i, "Share"))
                    }) { Text(if(lang=="zh") "分享文本" else "Share Text") }
                    
                    // Explicit Cancel
                    TextButton(onClick = { showExportDialog = false }) { 
                        Text(if(lang=="zh") "取消" else "Cancel", color = MaterialTheme.colorScheme.error) 
                    }
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(if(lang=="zh") "导入数据" else "Import Data") },
            text = { Text(if(lang=="zh") "将从CSV文件合并数据。请确保格式正确(Date,SBP,DBP,HR,Weight)。" else "Merge data from CSV. Ensure format: Date,SBP,DBP,HR,Weight.") },
            confirmButton = {
                TextButton(onClick = { 
                    showImportDialog = false
                    importLauncher.launch("text/*") 
                }) { Text(if(lang=="zh") "选择文件" else "Select File") }
            },
            dismissButton = { 
                TextButton(onClick = { showImportDialog = false }) { Text(if(lang=="zh") "取消" else "Cancel") } 
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Sticky Header / Tools
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(L10n.get("nav_history", lang), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Export Button -> Dialog
                    IconButton(onClick = { showExportDialog = true }) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) }

                    // Import Button -> Dialog
                    IconButton(onClick = { showImportDialog = true }) { Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary) }

                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { activeProfile?.let { viewModel.saveProfile(it.copy(language = if(lang == "zh") "en" else "zh")) } }, contentPadding = PaddingValues(0.dp)) {
                         Text(if(lang == "zh") "En" else "中", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Table Header
        item {
            val dateH = if(lang=="zh") "日期" else "Date"
            val bpH = if(lang=="zh") "血压" else "BP"
            val hrH = if(lang=="zh") "心率" else "HR"
            val wtH = if(lang=="zh") "体重" else "Weight"
            
            Row(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dateH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1.2f)) // More space for date
                Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.SpaceBetween) {
                     Text(bpH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                     Text(hrH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                     Text(wtH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                }
            }
            Divider()
        }
        items(records) { r ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(r.date.take(10), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), fontSize = 13.sp)
                    Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.SpaceBetween) {
                         Text("${r.sbp}/${r.dbp}", color = ColorBpRed, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                         Text("${r.hr}", color = ColorHrOrange, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                         Text("${r.weight}", color = ColorWeightPurple, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun SoftSegmentGauge(value: Float, min: Float, max: Float, segments: List<Pair<Float, Color>>, label: String) {
    // Segments: List of (UpperLimit, Color). 
    // Example BMI: (18.5, Blue), (24.9, Green), (29.9, Orange), (Max, Red)
    // We construct distinct color blocks with tiny transition zones (e.g. 2% of width)
    
    val totalRange = max - min
    val stops = mutableListOf<Pair<Float, Color>>()
    var currentStart = 0f
    
    // Build gradient stops
    segments.forEachIndexed { index, (limit, color) ->
        val safeLimit = limit.coerceAtMost(max)
        val endFraction = ((safeLimit - min) / totalRange).coerceIn(0f, 1f)
        
        // Hard start (or transition from prev)
        stops.add(currentStart to color)
        // Soft end (transition zone start)
        val transitionWidth = 0.05f // 5% transition
        val blockEnd = (endFraction - transitionWidth/2).coerceAtLeast(currentStart)
        stops.add(blockEnd to color)
        
        currentStart = endFraction // Next block starts here (implicit transition between blockEnd and endFraction)
    }

    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(String.format("%.1f", value), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
            .background(androidx.compose.ui.graphics.Brush.horizontalGradient(colorStops = stops.toTypedArray())))
        
        // Triangle Indicator
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)
            val offset = maxWidth * fraction - 5.dp 
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.offset(x = offset).size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}
