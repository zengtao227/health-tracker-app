import SwiftUI
import SwiftData

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @Query private var profiles: [UserProfile]
    @Query(sort: \HealthRecord.date, order: .reverse) private var records: [HealthRecord]
    
    @State private var selectedTab = 0
    @State private var currentUserId = 1
    
    var currentProfile: UserProfile? {
        profiles.first { $0.id == currentUserId }
    }
    
    var lang: String {
        currentProfile?.language ?? "zh"
    }
    
    var userRecords: [HealthRecord] {
        records.filter { $0.userId == currentUserId }
    }
    
    var body: some View {
        TabView(selection: $selectedTab) {
            HomeView(
                profile: currentProfile,
                profiles: profiles,
                currentUserId: $currentUserId,
                lang: lang,
                onSave: saveRecord,
                onProfileUpdate: updateProfile,
                onCreate: createProfile,
                onDeleteProfile: deleteProfile,
                onLangToggle: toggleLanguage
            )
            .tabItem {
                Image(systemName: "house.fill")
                Text(L10n.get("nav_home", lang))
            }
            .tag(0)
            
            TrendsView(
                records: userRecords,
                profile: currentProfile,
                lang: lang,
                onLangToggle: toggleLanguage
            )
            .tabItem {
                Image(systemName: "chart.line.uptrend.xyaxis")
                Text(L10n.get("nav_trends", lang))
            }
            .tag(1)
            
            HistoryView(
                records: userRecords,
                profile: currentProfile,
                lang: lang,
                onLangToggle: toggleLanguage,
                onImport: importRecords,
                onDelete: deleteRecords
            )
            .tabItem {
                Image(systemName: "clock.fill")
                Text(L10n.get("nav_history", lang))
            }
            .tag(2)
            
            KnowledgeView(
                lang: lang,
                onLangToggle: toggleLanguage
            )
            .tabItem {
                Image(systemName: "book.fill")
                Text(L10n.get("nav_knowledge", lang))
            }
            .tag(3)
        }
        .onAppear {
            ensureDefaultProfile()
        }
    }
    
    // MARK: - Actions
    
    private func ensureDefaultProfile() {
        if profiles.isEmpty {
            let profile = UserProfile(id: 1, name: "P1")
            modelContext.insert(profile)
            try? modelContext.save()
        }
    }
    
    private func saveRecord(sbp: Int?, dbp: Int?, hr: Int?, weight: Double?) {
        let record = HealthRecord(
            userId: currentUserId,
            date: Date(),
            sbp: sbp,
            dbp: dbp,
            hr: hr,
            weight: weight
        )
        modelContext.insert(record)
        try? modelContext.save()
    }
    
    private func updateProfile(_ profile: UserProfile) {
        try? modelContext.save()
    }
    
    private func createProfile(name: String) {
        let newProfile = UserProfile(id: (profiles.map { $0.id }.max() ?? 0) + 1, name: name)
        modelContext.insert(newProfile)
        currentUserId = newProfile.id
        try? modelContext.save()
    }
    
    private func deleteProfile(_ profile: UserProfile) {
        // 1. Delete all health records for this user
        let id = profile.id
        try? modelContext.delete(model: HealthRecord.self, where: #Predicate { $0.userId == id })
        
        // 2. Delete the profile itself
        modelContext.delete(profile)
        
        // 3. Reset to another user if available
        if let first = profiles.first(where: { $0.id != id }) {
            currentUserId = first.id
        } else {
            // If no users left, create default P1
            let p1 = UserProfile(id: 1, name: "P1")
            modelContext.insert(p1)
            currentUserId = 1
        }
        try? modelContext.save()
    }

    private func toggleLanguage() {
        if let profile = currentProfile {
            profile.language = profile.language == "zh" ? "en" : "zh"
            try? modelContext.save()
        }
    }
    
    private func importRecords(data: [(date: Date, sbp: Int?, dbp: Int?, hr: Int?, weight: Double?)]) {
        for item in data {
            let record = HealthRecord(
                userId: currentUserId,
                date: item.date,
                sbp: item.sbp,
                dbp: item.dbp,
                hr: item.hr,
                weight: item.weight
            )
            modelContext.insert(record)
        }
        try? modelContext.save()
    }
    
    private func deleteRecords(ids: Set<UUID>) {
        // Iterate and delete
        // Note: Delete by ID predicate is tricky with Sets in SwiftData in one go, 
        // so we can iterate over the in-memory records that we already fetched.
        // We have `records` available which is [HealthRecord].
        
        for record in records {
            if ids.contains(record.id) {
                modelContext.delete(record)
            }
        }
        try? modelContext.save()
    }
}

#Preview {
    ContentView()
        .modelContainer(for: [HealthRecord.self, UserProfile.self], inMemory: true)
}
