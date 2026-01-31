import SwiftUI
import Charts

struct TrendsView: View {
    let records: [HealthRecord]
    let profile: UserProfile?
    let lang: String
    let onLangToggle: () -> Void
    
    @State private var selectedRange: String = "30D"
    @State private var selectedBPValue: String = ""
    @State private var selectedWeightValue: String = ""
    
    private let ranges = ["7D", "30D", "1Y", "All"]
    
    var filteredRecords: [HealthRecord] {
        let now = Date()
        let calendar = Calendar.current
        
        return records.filter { record in
            switch selectedRange {
            case "7D":
                return calendar.dateComponents([.day], from: record.date, to: now).day ?? 999 <= 7
            case "30D":
                return calendar.dateComponents([.day], from: record.date, to: now).day ?? 999 <= 30
            case "1Y":
                return calendar.dateComponents([.day], from: record.date, to: now).day ?? 999 <= 365
            default:
                return true
            }
        }.sorted { $0.date < $1.date }
    }
    
    var latestRecord: HealthRecord? {
        records.first
    }
    
    var bmi: Double {
        guard let weight = latestRecord?.weight,
              let heightM = profile?.heightInMeters,
              heightM > 0 else { return 0 }
        return weight / (heightM * heightM)
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // Filter Row
                    filterRow
                    
                    if !records.isEmpty {
                        // BMI Gauge Card
                        bmiCard
                        
                        // BP WHO Gauge Card
                        bpCard
                        
                        // BP Trend Chart
                        bpChartCard
                        
                        // Weight Trend Chart
                        weightChartCard
                    } else {
                        emptyState
                    }
                }
                .padding()
            }
            .navigationTitle(L10n.get("nav_trends", lang))
        }
    }
    
    // MARK: - Filter Row
    
    private var filterRow: some View {
        HStack {
            Text("\(L10n.get("nav_trends", lang)): \(profile?.name ?? "P1")")
                .font(.headline)
                .fontWeight(.black)
            
            Spacer()
            
            // Range Picker
            HStack(spacing: 4) {
                ForEach(ranges, id: \.self) { range in
                    Button(range) {
                        selectedRange = range
                    }
                    .font(.caption)
                    .fontWeight(selectedRange == range ? .bold : .regular)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(
                        Capsule()
                            .fill(selectedRange == range ? Color.blue : Color.clear)
                    )
                    .foregroundStyle(selectedRange == range ? .white : .primary)
                }
            }
            
            // Language Toggle
            Button(action: onLangToggle) {
                Text(lang == "zh" ? "En" : "中")
                    .font(.caption)
                    .fontWeight(.bold)
            }
        }
    }
    
    // MARK: - BMI Card
    
    private var bmiCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(L10n.get("bmi_analysis", lang))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                
                Spacer()
                
                Text(String(format: "%.1f", bmi))
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundStyle(bmiColor)
                
                Text(bmiStatus)
                    .font(.caption)
                    .foregroundStyle(bmiColor)
            }
            
            // Gradient Gauge
            GaugeView(
                value: bmi,
                range: 15...35,
                colors: [.blue, .green, .yellow, .orange, .red],
                stops: [18.5, 24, 28, 32]
            )
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(bmiColor.opacity(0.1))
        )
    }
    
    private var bmiColor: Color {
        switch bmi {
        case ..<18.5: return .blue
        case 18.5..<24: return .green
        case 24..<28: return .yellow
        case 28..<32: return .orange
        default: return .red
        }
    }
    
    private var bmiStatus: String {
        switch bmi {
        case ..<18.5: return L10n.get("underweight", lang)
        case 18.5..<24: return L10n.get("normal", lang)
        case 24..<28: return L10n.get("overweight", lang)
        default: return L10n.get("obese", lang)
        }
    }
    
    // MARK: - BP Card (WHO Classification)
    
    private var bpCard: some View {
        let sbp = latestRecord?.sbp ?? 0
        let dbp = latestRecord?.dbp ?? 0
        let grade = getBPGrade(sbp: sbp, dbp: dbp)
        
        return VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(L10n.get("bp_classification", lang))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                
                Spacer()
                
                Text("\(sbp)/\(dbp)")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundStyle(bpColor(for: grade))
                
                Text(bpLabel(for: grade))
                    .font(.caption)
                    .foregroundStyle(bpColor(for: grade))
            }
            
            // WHO Grade Gauge (0-6 scale)
            GaugeView(
                value: Double(grade) + 0.5,
                range: 0...6,
                colors: [.green, Color(red: 0.54, green: 0.76, blue: 0.29), .yellow, .orange, .red, Color(red: 0.55, green: 0, blue: 0)],
                stops: [1, 2, 3, 4, 5]
            )
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(bpColor(for: grade).opacity(0.1))
        )
    }
    
    private func getBPGrade(sbp: Int, dbp: Int) -> Int {
        let sGrade: Int
        switch sbp {
        case ..<120: sGrade = 0
        case 120..<130: sGrade = 1
        case 130..<140: sGrade = 2
        case 140..<160: sGrade = 3
        case 160..<180: sGrade = 4
        default: sGrade = 5
        }
        
        let dGrade: Int
        switch dbp {
        case ..<80: dGrade = 0
        case 80..<85: dGrade = 1
        case 85..<90: dGrade = 2
        case 90..<100: dGrade = 3
        case 100..<110: dGrade = 4
        default: dGrade = 5
        }
        
        return max(sGrade, dGrade)
    }
    
    private func bpColor(for grade: Int) -> Color {
        switch grade {
        case 0: return .green
        case 1: return Color(red: 0.54, green: 0.76, blue: 0.29)
        case 2: return .yellow
        case 3: return .orange
        case 4: return .red
        default: return Color(red: 0.55, green: 0, blue: 0)
        }
    }
    
    private func bpLabel(for grade: Int) -> String {
        switch grade {
        case 0: return L10n.get("optimal", lang)
        case 1: return L10n.get("bp_normal", lang)
        case 2: return L10n.get("high_normal", lang)
        case 3: return L10n.get("grade1", lang)
        case 4: return L10n.get("grade2", lang)
        default: return L10n.get("grade3", lang)
        }
    }
    
    // MARK: - BP Chart
    
    private var bpChartCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(L10n.get("bp_trend", lang))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                
                if !selectedBPValue.isEmpty {
                    Spacer()
                    Text(selectedBPValue)
                        .font(.caption)
                        .foregroundStyle(.blue)
                }
            }
            
            Chart {
                ForEach(filteredRecords, id: \.id) { record in
                    if let sbp = record.sbp {
                        LineMark(
                            x: .value("Date", record.date),
                            y: .value("SBP", sbp)
                        )
                        .foregroundStyle(.red)
                        .symbol(Circle())
                    }
                    
                    if let dbp = record.dbp {
                        LineMark(
                            x: .value("Date", record.date),
                            y: .value("DBP", dbp)
                        )
                        .foregroundStyle(.blue)
                        .symbol(Circle())
                    }
                }
            }
            .frame(height: 180)
            .chartXAxis {
                AxisMarks(values: .stride(by: .day, count: max(filteredRecords.count / 5, 1))) { value in
                    AxisValueLabel(format: .dateTime.month(.abbreviated).day())
                }
            }
            .chartOverlay { proxy in
                GeometryReader { geo in
                    Rectangle().fill(.clear).contentShape(Rectangle())
                        .onTapGesture { location in
                            handleChartTap(at: location, proxy: proxy, geo: geo, type: .bp)
                        }
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    // MARK: - Weight Chart
    
    private var weightChartCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(L10n.get("weight_trend", lang))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                
                if !selectedWeightValue.isEmpty {
                    Spacer()
                    Text(selectedWeightValue)
                        .font(.caption)
                        .foregroundStyle(.purple)
                }
            }
            
            Chart {
                ForEach(filteredRecords, id: \.id) { record in
                    if let weight = record.weight {
                        LineMark(
                            x: .value("Date", record.date),
                            y: .value("Weight", weight)
                        )
                        .foregroundStyle(.purple)
                        .symbol(Circle())
                    }
                }
            }
            .frame(height: 180)
            .chartXAxis {
                AxisMarks(values: .stride(by: .day, count: max(filteredRecords.count / 5, 1))) { value in
                    AxisValueLabel(format: .dateTime.month(.abbreviated).day())
                }
            }
            .chartOverlay { proxy in
                GeometryReader { geo in
                    Rectangle().fill(.clear).contentShape(Rectangle())
                        .onTapGesture { location in
                            handleChartTap(at: location, proxy: proxy, geo: geo, type: .weight)
                        }
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    private enum ChartType {
        case bp, weight
    }
    
    private func handleChartTap(at location: CGPoint, proxy: ChartProxy, geo: GeometryProxy, type: ChartType) {
        let xPos = location.x - geo[proxy.plotFrame!].origin.x
        guard let date: Date = proxy.value(atX: xPos) else { return }
        
        // Find nearest record
        if let nearest = filteredRecords.min(by: { abs($0.date.timeIntervalSince(date)) < abs($1.date.timeIntervalSince(date)) }) {
            let formatter = DateFormatter()
            formatter.dateFormat = "MM-dd"
            let dateStr = formatter.string(from: nearest.date)
            
            switch type {
            case .bp:
                selectedBPValue = "\(dateStr): \(nearest.sbp ?? 0)/\(nearest.dbp ?? 0)"
            case .weight:
                selectedWeightValue = "\(dateStr): \(nearest.weight ?? 0) kg"
            }
        }
    }
    
    // MARK: - Empty State
    
    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "chart.line.uptrend.xyaxis")
                .font(.system(size: 60))
                .foregroundStyle(.secondary)
            
            Text(lang == "zh" ? "暂无数据" : "No data yet")
                .font(.headline)
                .foregroundStyle(.secondary)
            
            Text(lang == "zh" ? "请先在首页添加健康记录" : "Please add records from Home")
                .font(.subheadline)
                .foregroundStyle(.tertiary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 60)
    }
}

#Preview {
    TrendsView(
        records: [],
        profile: UserProfile(id: 1, name: "Test"),
        lang: "zh",
        onLangToggle: {}
    )
}
