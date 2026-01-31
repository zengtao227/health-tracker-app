import Foundation
import SwiftData

@Model
final class UserProfile {
    @Attribute(.unique) var id: Int
    var name: String = "P1"
    var height: Double = 175.0  // cm
    var age: Int = 30
    var language: String = "zh"  // "zh" or "en"
    
    init(id: Int = 1, name: String = "P1", height: Double = 175.0, age: Int = 30, language: String = "zh") {
        self.id = id
        self.name = name
        self.height = height
        self.age = age
        self.language = language
    }
    
    var heightInMeters: Double {
        height / 100.0
    }
}
