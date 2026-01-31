import SwiftUI
import SwiftData

@main
struct HealthTrackerApp: App {
    // 强制使用统一的容器
    var container: ModelContainer = {
        let schema = Schema([
            HealthRecord.self,
            UserProfile.self,
        ])
        let config = ModelConfiguration(isStoredInMemoryOnly: false)
        do {
            return try ModelContainer(for: schema, configurations: [config])
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .modelContainer(container)
    }
}
