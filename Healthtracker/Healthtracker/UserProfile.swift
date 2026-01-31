import SwiftData
import Foundation

@Model
final class UserProfile {
    var id: Int
    var name: String
    var height: Double
    var birthday: Date
    var language: String

    var heightInMeters: Double {
        return height / 100
    }

    convenience init(
        id: Int,
        name: String,
        height: Double = 175,
        birthday: Date = Calendar.current.date(byAdding: .year, value: -30, to: Date()) ?? Date(),
        language: String = "zh"
    ) {
        self.init(id: id, name: name, height: height, birthday: birthday, language: language)
    }

    init(id: Int, name: String, height: Double, birthday: Date, language: String) {
        self.id = id
        self.name = name
        self.height = height
        self.birthday = birthday
        self.language = language
    }
}
