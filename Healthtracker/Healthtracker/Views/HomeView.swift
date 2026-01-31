import SwiftUI
import SwiftData

struct HomeView: View {
    let profile: UserProfile?
    let profiles: [UserProfile]
    @Binding var currentUserId: Int
    let lang: String
    let onSave: (Int?, Int?, Int?, Double?) -> Void
    let onProfileUpdate: (UserProfile) -> Void
    let onCreate: (String) -> Void
    let onLangToggle: () -> Void
    
    @State private var sbpText = ""
    @State private var dbpText = ""
    @State private var hrText = ""
    @State private var weightText = ""
    @State private var showingProfileEdit = false
    
    @State private var editName = ""
    @State private var editHeight = ""
    @State private var editBirthday = Date()
    
    private let almanac = LunarService.getLocalAlmanac()
    
    var body: some View {
        NavigationStack {
            ZStack {
                LinearGradient(colors: [.blue.opacity(0.1), .purple.opacity(0.05)], startPoint: .topLeading, endPoint: .bottomTrailing)
                    .ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 24) {
                        dateCard
                        recordForm
                        if let profile = profile {
                            profileSneakPeek(profile)
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle(L10n.get("nav_home", lang))
            .toolbar {
                ToolbarItem(placement: .navigation) {
                    languageToggleButton
                }
                ToolbarItem(placement: .primaryAction) {
                    userSelector
                }
            }
            .sheet(isPresented: $showingProfileEdit) {
                profileEditView
            }
        }
    }
    
    private var languageToggleButton: some View {
        Button(action: onLangToggle) {
            Text(lang == "zh" ? "En" : "中")
                .fontWeight(.bold)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Capsule().stroke(Color.primary.opacity(0.2)))
        }
    }
    
    private var dateCard: some View {
        VStack(spacing: 16) {
            VStack {
                Text(formattedDate)
                    .font(.system(.title2, design: .serif))
                    .fontWeight(.bold)
                Text(almanac.lunarDate)
                    .font(.headline)
                    .foregroundStyle(.secondary)
            }
            HStack(spacing: 40) {
                StatusItem(label: lang == "zh" ? "宜" : "Auspicious", value: almanac.yi, color: .green)
                StatusItem(label: lang == "zh" ? "忌" : "Inauspicious", value: almanac.ji, color: .red)
            }
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 24))
    }
    
    private func profileSneakPeek(_ profile: UserProfile) -> some View {
        HStack {
            Image(systemName: "person.text.rectangle")
                .foregroundStyle(.blue)
            Text(profile.name)
                .bold()
            Spacer()
            Text("\(Int(profile.height))cm")
            Divider().frame(height: 15)
            Text("\(calculateAge(profile.birthday)) \(lang == "zh" ? "岁" : "y/o")")
        }
        .font(.subheadline)
        .padding()
        .background(Capsule().fill(.ultraThinMaterial))
        .onTapGesture {
            prepareEdit()
            showingProfileEdit = true
        }
    }
    
    private var recordForm: some View {
        VStack(spacing: 20) {
            Label(L10n.get("quick_record", lang), systemImage: "plus.circle.fill")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)
            VStack(spacing: 16) {
                HStack(spacing: 16) {
                    ModernInputField(title: L10n.get("sbp", lang), text: $sbpText, icon: "arrow.up.circle", color: .red)
                    ModernInputField(title: L10n.get("dbp", lang), text: $dbpText, icon: "arrow.down.circle", color: .blue)
                }
                HStack(spacing: 16) {
                    ModernInputField(title: L10n.get("hr", lang), text: $hrText, icon: "heart.fill", color: .orange)
                    ModernInputField(title: L10n.get("weight", lang), text: $weightText, icon: "scalemass.fill", color: .purple)
                }
            }
            Button(action: saveAction) {
                Text(L10n.get("save", lang))
                    .font(.headline)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
            }
        }
        .padding()
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 24))
    }
    
    private var profileEditView: some View {
        NavigationStack {
            Form {
                Section(header: Text(lang == "zh" ? "基本信息" : "Basic Info")) {
                    TextField(lang == "zh" ? "姓名" : "Name", text: $editName)
                    TextField(lang == "zh" ? "身高 (cm)" : "Height (cm)", text: $editHeight)
                    DatePicker(lang == "zh" ? "出生日期" : "Birthday", selection: $editBirthday, displayedComponents: .date)
                }
            }
            .navigationTitle(lang == "zh" ? "编辑资料" : "Edit Profile")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(lang == "zh" ? "取消" : "Cancel") { showingProfileEdit = false }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(lang == "zh" ? "保存" : "Save") { updateProfileAction() }
                }
            }
        }
    }
    
    private func saveAction() {
        onSave(Int(sbpText), Int(dbpText), Int(hrText), Double(weightText))
        sbpText = ""; dbpText = ""; hrText = ""; weightText = ""
    }
    
    private func prepareEdit() {
        if let p = profile {
            editName = p.name
            editHeight = String(format: "%.0f", p.height)
            editBirthday = p.birthday
        }
    }
    
    private func updateProfileAction() {
        if let p = profile {
            p.name = editName
            p.height = Double(editHeight) ?? 175.0
            p.birthday = editBirthday
            onProfileUpdate(p)
        }
        showingProfileEdit = false
    }
    
    private func calculateAge(_ birthday: Date) -> Int {
        Calendar.current.dateComponents([.year], from: birthday, to: Date()).year ?? 0
    }
    
    private var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = lang == "zh" ? "M月d日 EEEE" : "MMM d, EEEE"
        formatter.locale = Locale(identifier: lang == "zh" ? "zh_CN" : "en_US")
        return formatter.string(from: Date())
    }
    
    private var userSelector: some View {
        Menu {
            ForEach(profiles, id: \.id) { p in
                Button(p.name) { currentUserId = p.id }
            }
            Divider()
            Button(lang == "zh" ? "+ 新用户" : "+ New User") { onCreate("P\(profiles.count + 1)") }
        } label: {
            Image(systemName: "person.crop.circle.badge.plus")
                .symbolVariant(.fill)
                .font(.title3)
        }
    }
}

struct StatusItem: View {
    let label: String
    let value: String
    let color: Color
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label).font(.caption).foregroundStyle(color).bold()
            Text(value).font(.subheadline).bold()
        }
    }
}

struct ModernInputField: View {
    let title: String
    @Binding var text: String
    let icon: String
    let color: Color
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Label(title, systemImage: icon)
                .font(.caption2)
                .foregroundStyle(color)
            TextField("0", text: $text)
                .textFieldStyle(.roundedBorder)
        }
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
        onCreate: { _ in },
        onLangToggle: {}
    )
}
