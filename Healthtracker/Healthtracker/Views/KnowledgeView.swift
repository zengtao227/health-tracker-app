import SwiftUI

struct KnowledgeView: View {
    let lang: String
    let onLangToggle: () -> Void
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Almanac Reference Card
                    almanacReferenceCard
                    
                    // Blood Pressure Reference Card
                    
                    // BMI Reference Card
                    bmiReferenceCard
                }
                .padding()
            }
            .navigationTitle(L10n.get("nav_knowledge", lang))
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: onLangToggle) {
                        Text(lang == "zh" ? "En" : "中")
                            .font(.caption)
                            .fontWeight(.bold)
                    }
                }
            }
        }
    }
    
    // MARK: - BP Reference Card
    
    private var bpReferenceCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(L10n.get("bp_reference", lang))
                .font(.headline)
                .fontWeight(.bold)
            
            VStack(spacing: 8) {
                bpRow(
                    category: lang == "zh" ? "理想" : "Optimal",
                    sbp: "<120",
                    dbp: "<80",
                    color: .green
                )
                
                bpRow(
                    category: lang == "zh" ? "正常" : "Normal",
                    sbp: "120-129",
                    dbp: "80-84",
                    color: Color(red: 0.54, green: 0.76, blue: 0.29)
                )
                
                bpRow(
                    category: lang == "zh" ? "正常高值" : "High Normal",
                    sbp: "130-139",
                    dbp: "85-89",
                    color: .yellow
                )
                
                bpRow(
                    category: lang == "zh" ? "1级高血压" : "Grade 1",
                    sbp: "140-159",
                    dbp: "90-99",
                    color: .orange
                )
                
                bpRow(
                    category: lang == "zh" ? "2级高血压" : "Grade 2",
                    sbp: "160-179",
                    dbp: "100-109",
                    color: .red
                )
                
                bpRow(
                    category: lang == "zh" ? "3级高血压" : "Grade 3",
                    sbp: "≥180",
                    dbp: "≥110",
                    color: Color(red: 0.55, green: 0, blue: 0)
                )
            }
            
            Text(lang == "zh" ? "* 数据来源：WHO/ISH 高血压指南" : "* Source: WHO/ISH Hypertension Guidelines")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
        )
    }
    
    private func bpRow(category: String, sbp: String, dbp: String, color: Color) -> some View {
        HStack {
            Circle()
                .fill(color)
                .frame(width: 12, height: 12)
            
            Text(category)
                .font(.subheadline)
                .frame(width: 90, alignment: .leading)
            
            Spacer()
            
            Text("SBP: \(sbp)")
                .font(.caption)
                .foregroundStyle(.secondary)
            
            Spacer()
            
            Text("DBP: \(dbp)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
    
    // MARK: - BMI Reference Card
    
    private var bmiReferenceCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(L10n.get("bmi_reference", lang))
                .font(.headline)
                .fontWeight(.bold)
            
            VStack(spacing: 8) {
                bmiRow(
                    category: lang == "zh" ? "偏瘦" : "Underweight",
                    range: "<18.5",
                    color: .blue
                )
                
                bmiRow(
                    category: lang == "zh" ? "正常" : "Normal",
                    range: "18.5-23.9",
                    color: .green
                )
                
                bmiRow(
                    category: lang == "zh" ? "超重" : "Overweight",
                    range: "24-27.9",
                    color: .yellow
                )
                
                bmiRow(
                    category: lang == "zh" ? "肥胖" : "Obese",
                    range: "≥28",
                    color: .red
                )
            }
            
            // BMI Formula
            VStack(alignment: .leading, spacing: 4) {
                Text(lang == "zh" ? "BMI计算公式：" : "BMI Formula:")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                
                Text("BMI = Weight(kg) / Height(m)²")
                    .font(.caption)
                    .fontWeight(.medium)
            }
            .padding(.top, 8)
            
            Text(lang == "zh" ? "* 中国成人标准" : "* Chinese adult standards")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
        )
    }
    
    private func bmiRow(category: String, range: String, color: Color) -> some View {
        HStack {
            Circle()
                .fill(color)
                .frame(width: 12, height: 12)
            
            Text(category)
                .font(.subheadline)
                .frame(width: 80, alignment: .leading)
            
            Spacer()
            
            Text("BMI: \(range)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
    // MARK: - Almanac Reference
    private var almanacReferenceCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label(lang == "zh" ? "传统黄历算力" : "Local Almanac Engine", systemImage: "calendar.badge.clock")
                .font(.headline)
            
            Text(lang == "zh" ? "本程序集成了基于时间的传统黄历估算算法，能够根据您的地理位置和当前日期计算出当天的农历日期、干支以及宜忌参考。" : "This app integrates a time-based traditional almanac engine that calculates lunar dates and auspicious activities based on your local time.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            
            Divider()
            
            HStack {
                VStack(alignment: .leading) {
                    Text(lang == "zh" ? "计算来源" : "Source")
                        .font(.caption).bold()
                    Text("Lunar Math 1.0")
                }
                Spacer()
                VStack(alignment: .trailing) {
                    Text(lang == "zh" ? "实时同步" : "Sync")
                        .font(.caption).bold()
                    Text(lang == "zh" ? "无需联网" : "No Cloud Needed")
                }
            }
            .font(.caption)
        }
        .padding()
        .background(RoundedRectangle(cornerRadius: 16).fill(.ultraThinMaterial))
    }
}
