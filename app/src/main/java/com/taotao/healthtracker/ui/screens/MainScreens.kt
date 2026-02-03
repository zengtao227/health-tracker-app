@file:Suppress("unused", "UNUSED_PARAMETER", "SpellCheckingInspection")

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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.taotao.healthtracker.data.entity.HealthRecord
import com.taotao.healthtracker.data.entity.UserProfile
import com.taotao.healthtracker.domain.DateUtils
import com.taotao.healthtracker.viewmodel.HealthViewModel
import com.taotao.healthtracker.ui.L10n
import java.text.SimpleDateFormat
import java.util.*

val ColorBpRed = Color(0xFFFF4848)
val ColorBpBlue = Color(0xFF2196F3)
val ColorHrOrange = Color(0xFFFF9800)
val ColorWeightPurple = Color(0xFF9C27B0)
val ColorGreen = Color(0xFF43A047)
val ColorGlucose = Color(0xFF009688) // Teal
val ColorUric = Color(0xFF795548) // Brown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(viewModel: HealthViewModel, onSaveSuccess: () -> Unit) {
    val profiles by viewModel.userProfiles.collectAsState()
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    
    val appLang = activeProfile?.language ?: "zh"
    val insLang = activeProfile?.insightLanguage ?: "zh"
    val almanac by viewModel.currentAlmanac.collectAsState()
    
    // Module Visibility Logic
    val modules = remember(activeProfile?.enabledModules) {
        activeProfile?.enabledModules?.split(",")?.map { it.trim() } ?: listOf("bp", "weight", "hr")
    }
    val showBp = modules.contains("bp")
    val showWeight = modules.contains("weight")
    val showHr = modules.contains("hr")
    val showGlucose = modules.contains("glucose")
    val showUric = modules.contains("uric")
    
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
    var glucoseText by remember { mutableStateOf("") }
    var uricText by remember { mutableStateOf("") }

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
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(24.dp).clickable { isExpanded = !isExpanded }, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f).clickable { isExpanded = !isExpanded }) {
                        Text("${L10n.get("active_user", appLang)}: ${activeProfile?.name ?: "P1"}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${L10n.get("ht", appLang)}: ${activeProfile?.height ?: 0f}cm | ${L10n.get("born", appLang)}: ${activeProfile?.birthYear}-${activeProfile?.birthMonth}-${activeProfile?.birthDay}", 
                             style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    
                    // --- User Selector & Add ---
                    var showUserMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showUserMenu = true }) {
                        Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary)
                        DropdownMenu(expanded = showUserMenu, onDismissRequest = { showUserMenu = false }) {
                            profiles.forEach { p ->
                                DropdownMenuItem(text = { Text(p.name) }, onClick = { viewModel.switchUser(p.id); showUserMenu = false })
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text(if(appLang == "zh") "+ 新用户" else "+ New User", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                                onClick = { 
                                    viewModel.createNewUser("P${profiles.size + 1}")
                                    showUserMenu = false
                                }
                            )
                        }
                    }

                    // Dual Language Switcher
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
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        var showDeleteConfirm by remember { mutableStateOf(false) }
                        
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
                        }, modifier = Modifier.weight(1f)) { Text(L10n.get("save", appLang)) }

                        OutlinedButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.weight(0.6f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if(appLang == "zh") "删除" else "Del", fontSize = 12.sp)
                        }

                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text(if(appLang == "zh") "确认删除？" else "Confirm Delete") },
                                text = { Text(if(appLang == "zh") "该用户的所有记录都将被永久抹除。" else "All records for this user will be permanently deleted.") },
                                confirmButton = {
                                    TextButton(onClick = { 
                                        activeProfile?.let { viewModel.deleteProfile(it.id) }
                                        showDeleteConfirm = false
                                        isExpanded = false
                                    }) { Text(if(appLang == "zh") "确定" else "Confirm", color = ColorBpRed) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) { Text(if(appLang == "zh") "取消" else "Cancel") }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- 2. Central Date Display ---
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(westernDateStr, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            if (appLang == "zh") {
                Text(almanac.lunar, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
            if (showBp) {
                OutlinedTextField(value = sbpText, onValueChange = { sbpText = it }, label = { Text(L10n.get("sbp", appLang)) }, textStyle = MaterialTheme.typography.displaySmall, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = dbpText, onValueChange = { dbpText = it }, label = { Text(L10n.get("dbp", appLang)) }, textStyle = MaterialTheme.typography.displaySmall, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (showHr) {
                    OutlinedTextField(value = hrText, onValueChange = { hrText = it }, label = { Text(L10n.get("hr", appLang)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                if (showWeight) {
                    OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text(L10n.get("weight", appLang)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            }
            
            // Optional Modules
            if (showGlucose) {
                OutlinedTextField(value = glucoseText, onValueChange = { glucoseText = it }, label = { Text(L10n.get("glucose", appLang)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            if (showUric) {
                OutlinedTextField(value = uricText, onValueChange = { uricText = it }, label = { Text(L10n.get("uric_acid", appLang)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        }
        
        // --- 3.5 Real-time Analysis Preview (AddScreen) ---
        val liveSbp = sbpText.toIntOrNull()
        val liveDbp = dbpText.toIntOrNull()
        val liveWeight = weightText.toFloatOrNull()
        val profileHeight = (activeProfile?.height ?: 175f)
        
        if ((showBp && liveSbp != null && liveDbp != null) || (showWeight && liveWeight != null)) {
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.width(310.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.1f))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if(appLang == "zh") "实时分析 (当前输入)" else "Live Analysis", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    
                    if (showBp && liveSbp != null && liveDbp != null) {
                        val s = liveSbp; val d = liveDbp
                        val (label, color) = when {
                            s < 120 && d < 80 -> (if(appLang=="zh") "理想" else "Optimal") to ColorGreen
                            s < 140 && d < 90 -> (if(appLang=="zh") "正常" else "Normal") to Color(0xFF8BC34A)
                            s < 160 && d < 100 -> (if(appLang=="zh") "一级增高" else "Grade 1") to ColorHrOrange
                            else -> (if(appLang=="zh") "严重增高" else "Grade 2+") to ColorBpRed
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(color, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("BP: $s/$d - $label", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    if (showWeight && liveWeight != null) {
                        val hM = profileHeight / 100f
                        val liveBmi = liveWeight / (hM * hM)
                        val (sKey, color) = when { liveBmi < 18.5 -> "bmi_status_under" to ColorBpBlue; liveBmi < 24.9 -> "bmi_status_healthy" to ColorGreen; liveBmi < 29.9 -> "bmi_status_over" to ColorHrOrange; else -> "bmi_status_obese" to ColorBpRed }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(color, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("BMI: ${String.format("%.1f", liveBmi)} (${L10n.get(sKey, appLang)})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val s = sbpText.toIntOrNull(); val d = dbpText.toIntOrNull(); val h = hrText.toIntOrNull(); val w = weightText.toFloatOrNull()
                val glu = glucoseText.toFloatOrNull(); val uri = uricText.toFloatOrNull()
                
                val hasBp = s != null && d != null
                val hasData = hasBp || (showWeight && w != null) || (showHr && h != null) || (showGlucose && glu != null) || (showUric && uri != null)
                
                if (hasData) {
                    viewModel.saveRecord(HealthRecord(
                        userId = activeProfile?.id ?: 1, 
                        date = DateUtils.getCurrentDate(), 
                        sbp = s, dbp = d, hr = h, weight = w,
                        bloodGlucose = glu, uricAcid = uri
                    ))
                    sbpText = ""; dbpText = ""; hrText = ""; weightText = ""; glucoseText = ""; uricText = ""
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("今日黄历", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        Text(almanac.lunar, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(color = ColorGreen, shape = RoundedCornerShape(4.dp)) { 
                                Text("宜", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) 
                            }
                            Spacer(Modifier.height(10.dp))
                            
                            val yiWords = almanac.yi.split(" ").filter { it.isNotBlank() }
                            val balancedYi = if (yiWords.size >= 4) {
                                val mid = (yiWords.size + 1) / 2
                                yiWords.take(mid).joinToString(" ") + "\n" + yiWords.drop(mid).joinToString(" ")
                            } else {
                                almanac.yi
                            }
                            
                            Text(
                                text = balancedYi,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                            )
                        }
                        Box(Modifier.width(1.dp).height(80.dp).background(MaterialTheme.colorScheme.onSurface.copy(0.1f)).align(Alignment.CenterVertically))
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(color = ColorBpRed, shape = RoundedCornerShape(4.dp)) { 
                                Text("忌", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) 
                            }
                            Spacer(Modifier.height(10.dp))
                            
                            val jiWords = almanac.ji.split(" ").filter { it.isNotBlank() }
                            val balancedJi = if (jiWords.size >= 4) {
                                val mid = (jiWords.size + 1) / 2
                                jiWords.take(mid).joinToString(" ") + "\n" + jiWords.drop(mid).joinToString(" ")
                            } else {
                                almanac.ji
                            }
                            
                            Text(
                                text = balancedJi,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                            )
                        }
                    }
                } else {
                    Text("Zodiac Insight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text(almanac.lunar, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: HealthViewModel) {
    val records by viewModel.allRecords.collectAsState()
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    val lang = activeProfile?.language ?: "zh"
    
    // Module Visibility Logic
    val modules = remember(activeProfile?.enabledModules) {
        activeProfile?.enabledModules?.split(",")?.map { it.trim() } ?: listOf("bp", "weight", "hr")
    }
    val showBp = modules.contains("bp")
    val showWeight = modules.contains("weight")
    val showHr = modules.contains("hr")
    val showGlucose = modules.contains("glucose")
    val showUric = modules.contains("uric")

    var range by remember { mutableStateOf("30") } // 7, 30, 365, MAX
    
    val filtered = remember(records, range) {
        if (range == "MAX") records else {
            val limit = if(range == "7") 7 else if(range == "30") 30 else 365
            records.takeLast(limit)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(L10n.get("nav_trends", lang), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            
            // Range Selector
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("7", "30", "365", "MAX").forEach { r ->
                    val selected = range == r
                    Surface(
                        onClick = { range = r },
                        shape = RoundedCornerShape(16.dp),
                        color = if(selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        border = if(!selected) BorderStroke(1.dp, Color.Gray.copy(0.3f)) else null
                    ) {
                        Text(if(r=="365") "1Y" else r, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                             color = if(selected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = { activeProfile?.let { viewModel.saveProfile(it.copy(language = if(lang == "zh") "en" else "zh")) } }, contentPadding = PaddingValues(0.dp)) {
                    Text(if(lang == "zh") "En" else "中", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (records.isNotEmpty()) {
            val last = records.first() // Always use the ABSOLUTE latest record for summary
            val heightM = (activeProfile?.height ?: 175f) / 100f
            val bmi = if (heightM > 0 && last.weight != null) (last.weight ?: 0f) / (heightM * heightM) else 0f
            // 修复：如果最新记录没有血压数据，使用最近一条有血压数据的记录
            val lastBpRecord = records.firstOrNull { it.sbp != null && it.dbp != null } ?: last
            val sbp = lastBpRecord.sbp ?: 0
            val dbp = lastBpRecord.dbp ?: 0
            
            // Interactive States
            var selectedBp by remember { mutableStateOf("") }
            var selectedWt by remember { mutableStateOf("") }
            var selectedHr by remember { mutableStateOf("") }
            var selectedGlu by remember { mutableStateOf("") }
            var selectedUric by remember { mutableStateOf("") }

            // 1. BMI Card (Soft Gradient Segments)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.1f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Text(L10n.get("bmi_live", lang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                         Spacer(Modifier.width(8.dp))
                         val activeWeight = last.weight ?: 0f
                         val statusKey = when { bmi < 18.5 -> "bmi_status_under"; bmi < 24.9 -> "bmi_status_healthy"; bmi < 29.9 -> "bmi_status_over"; else -> "bmi_status_obese" }
                         val bmiText = if (activeWeight > 0) String.format("%.1f", bmi) else "--"
                         Text("$bmiText (${L10n.get(statusKey, lang)})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    SoftSegmentGauge(value = if(last.weight != null) bmi else 0f, min = 15f, max = 35f, 
                        segments = listOf(18.5f to ColorBpBlue, 24.9f to ColorGreen, 29.9f to ColorHrOrange, 35f to ColorBpRed),
                        label = "")
                }
            }
            
            // 2. Unified BP Gauge (WHO Classification)
            if (showBp) {
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
            }
            
            Spacer(Modifier.height(16.dp))
            
            // WEIGHT
            if (showWeight) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                     Text(if(lang=="zh") "体重趋势" else "Weight Trend", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(0.6f))
                     if(selectedWt.isNotEmpty()) Text(selectedWt, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ColorWeightPurple)
                }
                Spacer(Modifier.height(4.dp))
                WeightChart(filtered, lang) { e -> selectedWt = e }
                Spacer(Modifier.height(16.dp))
            }

            // GLUCOSE
            if (showGlucose) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                     Text(L10n.get("glucose", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(0.6f))
                     if(selectedGlu.isNotEmpty()) Text(selectedGlu, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ColorGlucose)
                }
                Spacer(Modifier.height(4.dp))
                GlucoseChart(filtered, lang) { e -> selectedGlu = e }
                Spacer(Modifier.height(16.dp))
            }

            // URIC ACID
            if (showUric) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                     Text(L10n.get("uric_acid", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(0.6f))
                     if(selectedUric.isNotEmpty()) Text(selectedUric, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ColorUric)
                }
                Spacer(Modifier.height(4.dp))
                UricAcidChart(filtered, lang) { e -> selectedUric = e }
            }
            
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
    
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    
    AndroidView(factory = { ctx -> LineChart(ctx).apply { 
        description.isEnabled = false; xAxis.position = XAxis.XAxisPosition.BOTTOM; 
        axisRight.isEnabled = false; axisLeft.setSpaceTop(20f); axisLeft.setSpaceBottom(20f)
        xAxis.setDrawGridLines(false); extraBottomOffset = 5f
        
        // Colors
        xAxis.textColor = textColor
        axisLeft.textColor = textColor
        legend.textColor = textColor
        
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
        val sEntries = rev.mapIndexedNotNull { i, r -> r.sbp?.let { Entry(i.toFloat(), it.toFloat()) } }
        val dEntries = rev.mapIndexedNotNull { i, r -> r.dbp?.let { Entry(i.toFloat(), it.toFloat()) } }
        
        val sSet = LineDataSet(sEntries, sLabel).apply { color = ColorBpRed.toArgb(); lineWidth = 3f; setDrawValues(true); valueTextColor = Color.White.toArgb(); valueTextSize = 10f; setDrawCircles(true); setCircleColor(ColorBpRed.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb() }
        val dSet = LineDataSet(dEntries, dLabel).apply { color = ColorBpBlue.toArgb(); lineWidth = 3f; setDrawValues(true); valueTextColor = Color.White.toArgb(); valueTextSize = 10f; setDrawCircles(true); setCircleColor(ColorBpBlue.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb() }
        chart.xAxis.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = if(v.toInt() in rev.indices) rev[v.toInt()].date.takeLast(5) else "" }
        chart.data = LineData(sSet, dSet); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(180.dp))
}

@Composable
fun WeightChart(records: List<HealthRecord>, lang: String, onSelection: (String) -> Unit) {
    val rev = records.reversed()
    val wLabel = if (lang == "zh") "体重 (kg)" else "Weight (kg)"

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(factory = { ctx -> LineChart(ctx).apply { 
        description.isEnabled = false; xAxis.position = XAxis.XAxisPosition.BOTTOM; 
        
        // Single Left Axis for Weight
        axisLeft.setSpaceTop(20f); axisLeft.setSpaceBottom(20f)
        axisRight.isEnabled = false // Disable Right Axis
        
        xAxis.setDrawGridLines(false); extraBottomOffset = 5f 
        
        // Colors
        xAxis.textColor = textColor
        axisLeft.textColor = textColor
        legend.textColor = textColor

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
        val wEntries = rev.mapIndexedNotNull { i, r -> r.weight?.let { Entry(i.toFloat(), it) } }
        val wSet = LineDataSet(wEntries, wLabel).apply { 
            color = ColorWeightPurple.toArgb(); lineWidth = 3f; setDrawCircles(true); setCircleColor(ColorWeightPurple.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb()
            setDrawValues(true); valueTextColor = Color.White.toArgb(); valueTextSize = 10f
        }
        chart.xAxis.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = if(v.toInt() in rev.indices) rev[v.toInt()].date.takeLast(5) else "" }
        chart.data = LineData(wSet); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(180.dp))
}

@Composable
fun HrChart(records: List<HealthRecord>, lang: String, onSelection: (String) -> Unit) {
    val rev = records.reversed()
    val label = if (lang == "zh") "心率" else "HR"

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(factory = { ctx -> LineChart(ctx).apply { 
        description.isEnabled = false; xAxis.position = XAxis.XAxisPosition.BOTTOM; 
        axisLeft.setSpaceTop(20f); axisLeft.setSpaceBottom(20f)
        axisRight.isEnabled = false 
        xAxis.setDrawGridLines(false); extraBottomOffset = 5f 
        
        xAxis.textColor = textColor
        axisLeft.textColor = textColor
        legend.textColor = textColor

        axisLeft.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = "${v.toInt()} bpm" }

        setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
             override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                 e?.let { 
                    val idx = it.x.toInt()
                    if(idx in rev.indices) {
                        val r = rev[idx]
                        onSelection("${r.date.takeLast(5)}: ${r.hr}")
                    }
                 }
             }
             override fun onNothingSelected() { onSelection("") }
        })
    }}, update = { chart ->
        val entries = rev.mapIndexedNotNull { i, r -> r.hr?.let { Entry(i.toFloat(), it.toFloat()) } }
        val set = LineDataSet(entries, label).apply { 
            color = ColorHrOrange.toArgb(); lineWidth = 3f; setDrawCircles(true); setCircleColor(ColorHrOrange.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb()
        }
        chart.xAxis.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = if(v.toInt() in rev.indices) rev[v.toInt()].date.takeLast(5) else "" }
        chart.data = LineData(set); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(180.dp))
}

@Composable
fun GlucoseChart(records: List<HealthRecord>, lang: String, onSelection: (String) -> Unit) {
    val rev = records.reversed()
    val label = if (lang == "zh") "血糖" else "Glucose"

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(factory = { ctx -> LineChart(ctx).apply { 
        description.isEnabled = false; xAxis.position = XAxis.XAxisPosition.BOTTOM; 
        axisLeft.setSpaceTop(20f); axisLeft.setSpaceBottom(20f)
        axisRight.isEnabled = false 
        xAxis.setDrawGridLines(false); extraBottomOffset = 5f 
        
        xAxis.textColor = textColor; axisLeft.textColor = textColor; legend.textColor = textColor

        setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
             override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                 e?.let { 
                    val idx = it.x.toInt()
                    if(idx in rev.indices) {
                        val r = rev[idx]
                        onSelection("${r.date.takeLast(5)}: ${r.bloodGlucose}")
                    }
                 }
             }
             override fun onNothingSelected() { onSelection("") }
        })
    }}, update = { chart ->
        val entries = rev.mapIndexedNotNull { i, r -> r.bloodGlucose?.let { Entry(i.toFloat(), it) } }
        val set = LineDataSet(entries, label).apply { 
            color = ColorGlucose.toArgb(); lineWidth = 3f; setDrawCircles(true); setCircleColor(ColorGlucose.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb()
        }
        chart.xAxis.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = if(v.toInt() in rev.indices) rev[v.toInt()].date.takeLast(5) else "" }
        chart.data = LineData(set); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(180.dp))
}

@Composable
fun UricAcidChart(records: List<HealthRecord>, lang: String, onSelection: (String) -> Unit) {
    val rev = records.reversed()
    val label = if (lang == "zh") "尿酸" else "Uric Acid"

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(factory = { ctx -> LineChart(ctx).apply { 
        description.isEnabled = false; xAxis.position = XAxis.XAxisPosition.BOTTOM; 
        axisLeft.setSpaceTop(20f); axisLeft.setSpaceBottom(20f)
        axisRight.isEnabled = false 
        xAxis.setDrawGridLines(false); extraBottomOffset = 5f 
        
        xAxis.textColor = textColor; axisLeft.textColor = textColor; legend.textColor = textColor

        setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
             override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                 e?.let { 
                    val idx = it.x.toInt()
                    if(idx in rev.indices) {
                        val r = rev[idx]
                        onSelection("${r.date.takeLast(5)}: ${r.uricAcid}")
                    }
                 }
             }
             override fun onNothingSelected() { onSelection("") }
        })
    }}, update = { chart ->
        val entries = rev.mapIndexedNotNull { i, r -> r.uricAcid?.let { Entry(i.toFloat(), it) } }
        val set = LineDataSet(entries, label).apply { 
            color = ColorUric.toArgb(); lineWidth = 3f; setDrawCircles(true); setCircleColor(ColorUric.toArgb()); circleRadius = 4f; highLightColor = Color.Transparent.toArgb()
        }
        chart.xAxis.valueFormatter = object : ValueFormatter() { override fun getFormattedValue(v: Float) = if(v.toInt() in rev.indices) rev[v.toInt()].date.takeLast(5) else "" }
        chart.data = LineData(set); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(180.dp))
}

@Composable
fun SettingsScreen(viewModel: HealthViewModel) {
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    val lang = activeProfile?.language ?: "zh"
    
    val modules = remember(activeProfile?.enabledModules) {
        activeProfile?.enabledModules?.split(",")?.map { it.trim() } ?: listOf("bp", "weight", "hr")
    }

    fun toggleModule(code: String) {
        activeProfile?.let { p ->
            val current = modules.toMutableList()
            if (current.contains(code)) current.remove(code) else current.add(code)
            viewModel.saveProfile(p.copy(enabledModules = current.joinToString(",")))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(L10n.get("nav_ref", lang), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { activeProfile?.let { viewModel.saveProfile(it.copy(language = if(lang == "zh") "en" else "zh")) } }) {
                Text(if(lang == "zh") "Switch to English" else "切换中文")
            }
        }

        // --- Module Settings ---
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.1f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(L10n.get("modules_title", lang), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Divider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if(lang=="zh") "血压监测 (BP)" else "Blood Pressure", fontSize = 15.sp)
                    Switch(checked = modules.contains("bp"), onCheckedChange = { toggleModule("bp") })
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if(lang=="zh") "体重记录 (Weight)" else "Body Weight", fontSize = 15.sp)
                    Switch(checked = modules.contains("weight"), onCheckedChange = { toggleModule("weight") })
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if(lang=="zh") "心率监测 (Heart Rate)" else "Heart Rate", fontSize = 15.sp)
                    Switch(checked = modules.contains("hr"), onCheckedChange = { toggleModule("hr") })
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(L10n.get("enable_glucose", lang), fontSize = 15.sp)
                    Switch(checked = modules.contains("glucose"), onCheckedChange = { toggleModule("glucose") })
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(L10n.get("enable_uric", lang), fontSize = 15.sp)
                    Switch(checked = modules.contains("uric"), onCheckedChange = { toggleModule("uric") })
                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(viewModel: HealthViewModel) {
    val records by viewModel.allRecords.collectAsState()
    val activeProfile by viewModel.currentUserProfile.collectAsState()
    val lang = activeProfile?.language ?: "zh"
    
    // Module Visibility Logic
    val modules = remember(activeProfile?.enabledModules) {
        activeProfile?.enabledModules?.split(",")?.map { it.trim() } ?: listOf("bp", "weight", "hr")
    }
    val showBp = modules.contains("bp")
    val showWeight = modules.contains("weight")
    val showHr = modules.contains("hr")
    val showGlucose = modules.contains("glucose")
    val showUric = modules.contains("uric")
    
    // UI State
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDeleteAlert by remember { mutableStateOf(false) }
    
    // Selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedRecords by remember { mutableStateOf(setOf<HealthRecord>()) }

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
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showDeleteAlert = false },
            title = { Text(L10n.get("confirm_delete", lang)) },
            text = { Text(L10n.get("delete_msg", lang)) },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.deleteRecords(selectedRecords.toList())
                    selectedRecords = emptySet()
                    isSelectionMode = false 
                    showDeleteAlert = false
                }) { Text(L10n.get("delete", lang), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlert = false }) { Text(L10n.get("cancel", lang)) }
            }
        )
    }

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
            text = { Text(if(lang=="zh") "将从CSV文件合并数据。请确保格式正确(Date,SBP,DBP,HR,Weight,Glucose,UricAcid)。" else "Merge data from CSV. Ensure format: Date,SBP,DBP,HR,Weight,Glucose,UricAcid.") },
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
        stickyHeader {
            Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), color = MaterialTheme.colorScheme.background) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        Text("${selectedRecords.size} selected", style = MaterialTheme.typography.titleMedium)
                        Row {
                            IconButton(onClick = { 
                                if(selectedRecords.size == records.size) selectedRecords = emptySet() 
                                else selectedRecords = records.toSet() 
                            }) { Icon(Icons.Default.CheckCircle, null) }
                            
                            IconButton(onClick = { if(selectedRecords.isNotEmpty()) showDeleteAlert = true }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            
                            IconButton(onClick = { isSelectionMode = false; selectedRecords = emptySet() }) { Icon(Icons.Default.Close, null) }
                        }
                    } else {
                        Text(L10n.get("nav_history", lang), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isSelectionMode = true }) { Icon(Icons.Default.Edit, null) } // Enable Selection
                            IconButton(onClick = { showExportDialog = true }) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) }
                            IconButton(onClick = { showImportDialog = true }) { Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary) }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = { activeProfile?.let { viewModel.saveProfile(it.copy(language = if(lang == "zh") "en" else "zh")) } }, contentPadding = PaddingValues(0.dp)) {
                                 Text(if(lang == "zh") "En" else "中", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
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
            val gluH = if(lang=="zh") "血糖" else "Glu"
            val uricH = if(lang=="zh") "尿酸" else "Uric"
            
            Row(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if(isSelectionMode) Spacer(Modifier.width(32.dp))
                Text(dateH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1.2f)) // More space for date
                Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.SpaceBetween) {
                     if (showBp) Text(bpH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                     if (showHr) Text(hrH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                     if (showWeight) Text(wtH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                     if (showGlucose) Text(gluH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                     if (showUric) Text(uricH, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                }
            }
            Divider()
        }
        items(records) { r ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                if(isSelectionMode) {
                    if(selectedRecords.contains(r)) selectedRecords -= r else selectedRecords += r
                }
            }) {
                if(isSelectionMode) {
                    Checkbox(checked = selectedRecords.contains(r), onCheckedChange = { chk -> 
                         if(chk) selectedRecords += r else selectedRecords -= r
                    })
                }
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(r.date.take(10), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), fontSize = 13.sp)
                        Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.SpaceBetween) {
                             if (showBp) Text("${r.sbp ?: "-"}/${r.dbp ?: "-"}", color = ColorBpRed, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                             if (showHr) Text("${r.hr ?: "-"}", color = ColorHrOrange, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                             if (showWeight) Text("${r.weight ?: "-"}", color = ColorWeightPurple, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                             if (showGlucose) Text("${r.bloodGlucose ?: "-"}", color = ColorGlucose, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                             if (showUric) Text("${r.uricAcid ?: "-"}", color = ColorUric, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        }
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
    segments.forEach { (limit, color) ->
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
