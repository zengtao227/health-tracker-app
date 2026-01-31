import Foundation

/// Localization helper - equivalent to Android L10n object
struct L10n {
    static func get(_ key: String, _ lang: String) -> String {
        let translations: [String: [String: String]] = [
            "app_name": ["zh": "健康追踪", "en": "Health Tracker"],
            "nav_home": ["zh": "首页", "en": "Home"],
            "nav_trends": ["zh": "趋势", "en": "Trends"],
            "nav_history": ["zh": "历史", "en": "History"],
            "nav_knowledge": ["zh": "参考", "en": "Reference"],
            
            // Home
            "quick_record": ["zh": "快速记录", "en": "Quick Record"],
            "sbp": ["zh": "收缩压", "en": "Systolic"],
            "dbp": ["zh": "舒张压", "en": "Diastolic"],
            "hr": ["zh": "心率", "en": "Heart Rate"],
            "weight": ["zh": "体重", "en": "Weight"],
            "save": ["zh": "保存", "en": "Save"],
            "today": ["zh": "今日", "en": "Today"],
            
            // Trends
            "bmi_analysis": ["zh": "当前BMI分析", "en": "Current BMI Analysis"],
            "bp_classification": ["zh": "血压分级 (WHO)", "en": "BP Classification (WHO)"],
            "bp_trend": ["zh": "血压趋势", "en": "BP Trend"],
            "weight_trend": ["zh": "体重趋势", "en": "Weight Trend"],
            
            // BMI Status
            "underweight": ["zh": "偏瘦", "en": "Underweight"],
            "normal": ["zh": "正常", "en": "Normal"],
            "overweight": ["zh": "超重", "en": "Overweight"],
            "obese": ["zh": "肥胖", "en": "Obese"],
            
            // BP Status (WHO)
            "optimal": ["zh": "理想", "en": "Optimal"],
            "bp_normal": ["zh": "正常", "en": "Normal"],
            "high_normal": ["zh": "正常高值", "en": "High Normal"],
            "grade1": ["zh": "1级高血压", "en": "Grade 1"],
            "grade2": ["zh": "2级高血压", "en": "Grade 2"],
            "grade3": ["zh": "3级高血压", "en": "Grade 3"],
            
            // History
            "date": ["zh": "日期", "en": "Date"],
            "bp": ["zh": "血压", "en": "BP"],
            "export_data": ["zh": "导出数据", "en": "Export Data"],
            "import_data": ["zh": "导入数据", "en": "Import Data"],
            "choose_method": ["zh": "请选择导出方式：", "en": "Choose export method:"],
            "save_to_file": ["zh": "保存为文件", "en": "Save to File"],
            "share_text": ["zh": "分享文本", "en": "Share Text"],
            "cancel": ["zh": "取消", "en": "Cancel"],
            "select_file": ["zh": "选择文件", "en": "Select File"],
            "import_hint": ["zh": "将从CSV文件合并数据。请确保格式正确(Date,SBP,DBP,HR,Weight)。", "en": "Merge data from CSV. Ensure format: Date,SBP,DBP,HR,Weight."],
            
            // Knowledge
            "bp_reference": ["zh": "血压参考标准", "en": "Blood Pressure Reference"],
            "medical_ref": ["zh": "医疗参考", "en": "Medical Reference"],
            
            // Knowledge Details
            "who_bp": ["zh": "WHO 血压标准 (2024)", "en": "WHO BP Standards (2024)"],
            "bp_safe": ["zh": "理想血压", "en": "Optimal"],
            "bp_norm_ref": ["zh": "正常血压", "en": "Normal"],
            "bp_warning": ["zh": "正常高值 (预警)", "en": "High Normal (Warning)"],
            "bp_hazard": ["zh": "高血压 (危险)", "en": "Hypertension (Hazard)"],
            
            "bmi_guide": ["zh": "BMI 指南", "en": "BMI Guide"],
            "bmi_status_under": ["zh": "偏瘦", "en": "Underweight"],
            "bmi_status_healthy": ["zh": "健康体重", "en": "Healthy Weight"],
            "bmi_status_over": ["zh": "超重", "en": "Overweight"],
            "bmi_status_obese": ["zh": "肥胖", "en": "Obese"],
        ]
        
        return translations[key]?[lang] ?? translations[key]?["en"] ?? key
    }
}
