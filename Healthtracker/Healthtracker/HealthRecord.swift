import SwiftData
import Foundation

@Model
final class HealthRecord {
    var id: UUID
    var userId: Int
    var date: Date
    var sbp: Int?
    var dbp: Int?
    var hr: Int?
    var weight: Double?
    
    init(
        id: UUID = UUID(),
        userId: Int,
        date: Date,
        sbp: Int? = nil,
        dbp: Int? = nil,
        hr: Int? = nil,
        weight: Double? = nil
    ) {
        self.id = id
        self.userId = userId
        self.date = date
        self.sbp = sbp
        self.dbp = dbp
        self.hr = hr
        self.weight = weight
    }
}
