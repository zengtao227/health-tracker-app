import SwiftUI

struct KnowledgeView: View {
    let lang: String
    let onLangToggle: () -> Void
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Title Header
                    HStack {
                        Text(L10n.get("medical_ref", lang))
                            .font(.title2)
                            .bold()
                        Spacer()
                        Button(action: onLangToggle) {
                            Text(lang == "zh" ? "Switch to English" : "切换中文")
                                .font(.subheadline)
                        }
                    }
                    .padding(.horizontal)
                    
                    // Card 1: WHO BP Standards (Mirrored from Android)
                    VStack(alignment: .leading, spacing: 16) {
                        Text(L10n.get("who_bp", lang))
                            .font(.headline)
                            .foregroundStyle(.red)
                        Divider()
                        
                        KnowledgeRow(
                            label: "\(L10n.get("bp_safe", lang)) (< 120/80)",
                            color: .blue
                        )
                        KnowledgeRow(
                            label: "\(L10n.get("bp_norm_ref", lang)) (120-139/80-89)",
                            color: .green
                        )
                        KnowledgeRow(
                            label: "\(L10n.get("bp_warning", lang)) (140-159/90-99)",
                            color: .orange
                        )
                        KnowledgeRow(
                            label: "\(L10n.get("bp_hazard", lang)) (> 160/100)",
                            color: .red
                        )
                    }
                    .padding()
                    .background(RoundedRectangle(cornerRadius: 16).fill(.ultraThinMaterial))
                    
                    // Card 2: BMI Guide (Mirrored from Android)
                    VStack(alignment: .leading, spacing: 16) {
                        Text(L10n.get("bmi_guide", lang))
                            .font(.headline)
                            .foregroundStyle(.blue)
                        Divider()
                        
                        KnowledgeRow(
                            label: "\(L10n.get("bmi_status_under", lang)) (< 18.5)",
                            color: .blue
                        )
                        KnowledgeRow(
                            label: "\(L10n.get("bmi_status_healthy", lang)) (18.5-24.9)",
                            color: .green
                        )
                        KnowledgeRow(
                            label: "\(L10n.get("bmi_status_over", lang)) (25-29.9)",
                            color: .orange
                        )
                        KnowledgeRow(
                            label: "\(L10n.get("bmi_status_obese", lang)) (> 30)",
                            color: .red
                        )
                    }
                    .padding()
                    .background(RoundedRectangle(cornerRadius: 16).fill(.ultraThinMaterial))
                    
                    // Card 3: Source & Algorithm (Mirrored from Android)
                    VStack(alignment: .leading, spacing: 12) {
                        Label(lang == "zh" ? "算法与数据来源" : "Source & Algorithm", systemImage: "info.circle")
                            .font(.headline)
                        
                        Divider()
                        
                        Text(lang == "zh" ? "1. 中文黄历：基于《协纪辨方书》之建除十二神循环推算。" : "1. Almanac: Calculated based on the 'Jian-Chu 12 Day Officers' cycle from traditional astronomy.")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        
                        Text(lang == "zh" ? "2. 星座运势：基于西方回归黄道 (Tropical Zodiac) 系统推算。" : "2. Horoscope: Based on the Western Tropical Zodiac system.")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        
                        Text(lang == "zh" ? "3. 健康标准：WHO 2024 高血压与BMI指南。" : "3. Health Refs: WHO 2024 Guidelines for Hypertension & BMI.")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    .padding()
                    .background(RoundedRectangle(cornerRadius: 16).fill(.ultraThinMaterial))
                }
                .padding()
            }
            .navigationTitle(L10n.get("nav_knowledge", lang))
        }
    }
}

struct KnowledgeRow: View {
    let label: String
    let color: Color
    
    var body: some View {
        HStack {
            Circle()
                .fill(color)
                .frame(width: 10, height: 10)
            Text(label)
                .font(.subheadline)
            Spacer()
        }
    }
}

#Preview {
    KnowledgeView(lang: "zh", onLangToggle: {})
}
