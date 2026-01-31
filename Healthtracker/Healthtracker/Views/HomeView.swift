import SwiftUI

struct HomeView: View {
    let profile: UserProfile?
    let profiles: [UserProfile]
    @Binding var currentUserId: Int
    let lang: String
    let onSave: (Int?, Int?, Int?, Double?) -> Void
    let onProfileUpdate: (UserProfile) -> Void
    let onCreate: (String) -> Void
    
    @State private var sbpText = ""
    @State private var dbpText = ""
    @State private var hrText = ""
    @State private var weightText = ""
    @State private var showingUserPicker = false
    
    private let almanac = LunarService.getLocalAlmanac()
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Date & Almanac Card
                    dateCard
                    
                    // Quick Record Form
                    recordForm
                }
                .padding()
            }
            .navigationTitle(L10n.get("nav_home", lang))
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    userSelector
                }
            }
        }
    }
    
    // MARK: - Date Card
    
    private var dateCard: some View {
        VStack(spacing: 12) {
            // Gregorian Date
            Text(formattedDate)
                .font(.title)
                .fontWeight(.bold)
            
            // Lunar Date
            Text(almanac.lunarDate)
                .font(.headline)
                .foregroundStyle(.secondary)
            
            Divider()
            
            // Yi & Ji
            HStack(spacing: 30) {
                VStack(alignment: .leading) {
                    Text("宜")
                        .font(.caption)
                        .foregroundStyle(.green)
                    Text(almanac.yi)
                        .font(.subheadline)
                }
                
                VStack(alignment: .leading) {
                    Text("忌")
                        .font(.caption)
                        .foregroundStyle(.red)
                    Text(almanac.ji)
                        .font(.subheadline)
                }
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
        )
    }
    
    private var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy年MM月dd日 EEEE"
        formatter.locale = Locale(identifier: lang == "zh" ? "zh_CN" : "en_US")
        return formatter.string(from: Date())
    }
    
    // MARK: - Record Form
    
    private var recordForm: some View {
        VStack(spacing: 16) {
            Text(L10n.get("quick_record", lang))
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            // Blood Pressure Row
            HStack(spacing: 12) {
                inputField(
                    title: L10n.get("sbp", lang),
                    unit: "mmHg",
                    text: $sbpText,
                    color: .red
                )
                
                inputField(
                    title: L10n.get("dbp", lang),
                    unit: "mmHg",
                    text: $dbpText,
                    color: .blue
                )
            }
            
            // HR & Weight Row
            HStack(spacing: 12) {
                inputField(
                    title: L10n.get("hr", lang),
                    unit: "bpm",
                    text: $hrText,
                    color: .orange
                )
                
                inputField(
                    title: L10n.get("weight", lang),
                    unit: "kg",
                    text: $weightText,
                    color: .purple
                )
            }
            
            // Save Button
            Button(action: saveRecord) {
                Text(L10n.get("save", lang))
                    .font(.headline)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(.blue)
                    )
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
        )
    }
    
    private func inputField(title: String, unit: String, text: Binding<String>, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption)
                .foregroundStyle(color)
            
            HStack {
                TextField("0", text: text)
                    #if os(iOS)
                    .keyboardType(.decimalPad)
                    #endif
                    .textFieldStyle(.roundedBorder)
                
                Text(unit)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity)
    }
    
    // MARK: - User Selector
    
    private var userSelector: some View {
        Menu {
            ForEach(profiles, id: \.id) { profile in
                Button(profile.name) {
                    currentUserId = profile.id
                }
            }
            Divider()
            Button("+ New User") {
                onCreate("P\(profiles.count + 1)")
            }
        } label: {
            HStack {
                Image(systemName: "person.circle.fill")
                Text(profile?.name ?? "P1")
                    .fontWeight(.medium)
            }
        }
    }
    
    // MARK: - Actions
    
    private func saveRecord() {
        let sbp = Int(sbpText)
        let dbp = Int(dbpText)
        let hr = Int(hrText)
        let weight = Double(weightText)
        
        onSave(sbp, dbp, hr, weight)
        
        // Clear fields
        sbpText = ""
        dbpText = ""
        hrText = ""
        weightText = ""
    }
}

#Preview {
    HomeView(
        profile: UserProfile(id: 1, name: "Test"),
        profiles: [UserProfile(id: 1, name: "Test")],
        currentUserId: .constant(1),
        lang: "zh",
        onSave: { _, _, _, _ in },
        onProfileUpdate: { _ in },
        onCreate: { _ in }
    )
}
