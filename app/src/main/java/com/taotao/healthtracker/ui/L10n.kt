package com.taotao.healthtracker.ui

object L10n {
    private val en = mapOf(
        "nav_record" to "Record",
        "nav_history" to "History",
        "nav_trends" to "Trends",
        "nav_ref" to "REF",
        "active_user" to "Active User",
        "user_name" to "Name",
        "ht" to "Height",
        "born" to "Birth Year",
        "save" to "Save Profile",
        "sbp" to "Systolic (SBP)",
        "dbp" to "Diastolic (DBP)",
        "hr" to "Heart Rate",
        "weight" to "Weight (kg)",
        "save_analysis" to "Save & Analysis",
        "daily_log" to "Daily Log",
        "family" to "Family Profiles",
        "bmi_live" to "Live BMI Analysis",
        "bp_chart" to "BLOOD PRESSURE (mmHg)",
        "vital_chart" to "VITALS TREND (HR & WEIGHT)",
        "medical_ref" to "Medical Standards",
        "who_bp" to "WHO Blood Pressure Grades",
        "bmi_guide" to "BMI Classification Guide",
        "bmi_status_healthy" to "Healthy",
        "bmi_status_over" to "Overweight",
        "bmi_status_obese" to "Obese",
        "bmi_status_under" to "Underweight",
        "bp_safe" to "Safe / Ideal",
        "bp_normal" to "Normal",
        "bp_warning" to "Watch / Warning",
        "bp_hazard" to "Hazard / High",
        "no_data" to "No records for this user.",
        "yi" to "Suitable",
        "ji" to "Avoid",
        "today_lunar" to "Today's Almanac",
        "lang_name" to "English"
    )

    private val zh = mapOf(
        "nav_record" to "录入",
        "nav_history" to "历史",
        "nav_trends" to "趋势",
        "nav_ref" to "参考",
        "active_user" to "当前用户",
        "user_name" to "姓名",
        "ht" to "身高",
        "born" to "出生年份",
        "save" to "保存档案",
        "sbp" to "收缩压 (高压)",
        "dbp" to "舒张压 (低压)",
        "hr" to "心率",
        "weight" to "体重 (kg)",
        "save_analysis" to "保存并查看分析",
        "daily_log" to "每日体征录入",
        "family" to "家庭成员",
        "bmi_live" to "当前 BMI 实时分析",
        "bp_chart" to "血压趋势图 (mmHg)",
        "vital_chart" to "心率与体重对比趋势",
        "medical_ref" to "医疗参考标准",
        "who_bp" to "WHO 血压分级参考",
        "bmi_guide" to "BMI 体质指数指南",
        "bmi_status_healthy" to "健康",
        "bmi_status_over" to "超重",
        "bmi_status_obese" to "肥胖",
        "bmi_status_under" to "过轻",
        "bp_safe" to "理想范围",
        "bp_normal" to "正常范围",
        "bp_warning" to "注意/预警",
        "bp_hazard" to "高度危险",
        "no_data" to "当前账号暂无数据",
        "yi" to "宜",
        "ji" to "忌",
        "today_lunar" to "今日黄历",
        "lang_name" to "中文"
    )

    fun get(key: String, lang: String): String {
        val dict = if (lang == "zh") zh else en
        return dict[key] ?: key
    }
}
