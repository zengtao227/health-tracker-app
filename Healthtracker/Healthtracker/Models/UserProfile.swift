import Foundation
import SwiftData

@Model
final class UserProfile {
    @Attribute(.unique) var id: Int
    var name: String = "P1"
    var height: Double = 175.0  // cm
    var birthday: Date = Calendar.current.date(byAdding: .year, value: -30, to: Date()) ?? Date()
    var language: String = "zh"  // "zh" or "en"
    
    init(id: Int = 1, name: String = "P1", height: Double = 175.0, language: String = "zh") {
        self.id = id
        self.name = name
        self.height = height
        self.language = language
    }
    
    var heightInMeters: Double {
        height / 100.0
    }
}
