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
    let onDeleteProfile: (UserProfile) -> Void
    let onLangToggle: () -> Void
    
    @State private var sbpText = ""
    @State private var dbpText = ""
    @State private var hrText = ""
    @State private var weightText = ""
    @State private var showingProfileEdit = false
    @State private var showingDeleteConfirmation = false
    
    @State private var editName = ""
    @State private var editHeight = ""
    @State private var editBirthday = Date()
    
    // Almanac
    
    
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
        let birthDate = profile?.birthday ?? Date()
        let cal = Calendar.current
        let m = cal.component(.month, from: birthDate)
        let d = cal.component(.day, from: birthDate)
        let almanac = LunarService.getLocalAlmanac(month: m, day: d)
        
        return VStack(spacing: 20) {
            VStack(spacing: 4) {
                Text(formattedDate)
                    .font(.system(.title2, design: .serif))
                    .fontWeight(.bold)
                
                if lang == "zh" {
                    Text(almanac.lunarDate)
                        .font(.headline)
                        .foregroundStyle(.secondary)
                } else {
                    Text("\(almanac.zodiac) Season")
                        .font(.headline)
                        .foregroundStyle(.secondary)
                }
            }
            
            Divider().background(Color.primary.opacity(0.1))
            
            HStack(alignment: .top, spacing: 0) {
                if lang == "zh" {
                    StatusItem(label: "宜", value: almanac.yi, color: .green)
                    Divider().frame(height: 40).padding(.horizontal, 10)
                    StatusItem(label: "忌", value: almanac.ji, color: .red)
                } else {
                    StatusItem(label: "STRENGTH", value: almanac.strength, color: .blue)
                    Divider().frame(height: 40).padding(.horizontal, 10)
                    StatusItem(label: "BEWARE", value: almanac.beware, color: .orange)
                }
            }
        }
        .padding(24)
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
                
                Section {
                    Button(role: .destructive) {
                        showingDeleteConfirmation = true
                    } label: {
                        HStack {
                            Image(systemName: "trash")
                            Text(lang == "zh" ? "删除用户" : "Delete User")
                        }
                        .frame(maxWidth: .infinity, alignment: .center)
                    }
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
            .confirmationDialog(
                lang == "zh" ? "确认删除用户？" : "Delete User?",
                isPresented: $showingDeleteConfirmation,
                titleVisibility: .visible
            ) {
                Button(lang == "zh" ? "删除" : "Delete", role: .destructive) {
                    if let p = profile {
                        showingProfileEdit = false
                        onDeleteProfile(p)
                    }
                }
                Button(lang == "zh" ? "取消" : "Cancel", role: .cancel) {}
            } message: {
                Text(lang == "zh" ? "此操作无法撤销。所有健康记录将被永久删除。" : "This action cannot be undone. All health records will be permanently deleted.")
            }
        }
    }
    
    private func saveAction() {
        onSave(Int(sbpText), Int(dbpText), Int(hrText), Double(weightText))
        sbpText = ""; dbpText = ""; hrText = ""; weightText = ""
        #if os(iOS)
        let haptic = UIImpactFeedbackGenerator(style: .medium)
        haptic.impactOccurred()
        #endif
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
        VStack(spacing: 8) {
            Text(label)
                .font(.caption)
                .bold()
                .foregroundStyle(.white)
                .padding(.horizontal, 8)
                .padding(.vertical, 2)
                .background(Capsule().fill(color))
            
            WordFlowView(text: value)
        }
        .frame(maxWidth: .infinity)
    }
}

struct WordFlowView: View {
    let text: String
    
    var body: some View {
        // 使用 Unicode \u{2060} (Word Joiner) 强制中文字符连在一起不被拆散
        let processedText = text.components(separatedBy: " ")
            .filter { !$0.isEmpty }
            .map { word in
                // 在单词的每个字符之间插入 Word Joiner
                word.map { String($0) }.joined(separator: "\u{2060}")
            }
            .joined(separator: " ") // 单词之间保留普通空格，允许在这里换行
        
        Text(processedText)
            .font(.subheadline)
            .bold()
            .multilineTextAlignment(.center)
            .lineSpacing(4)
            .fixedSize(horizontal: false, vertical: true)
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
                .padding(.top, 0)
                #if os(iOS)
                .keyboardType(.decimalPad)
                #endif
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
        onDeleteProfile: { _ in },
        onLangToggle: {}
    )
}
