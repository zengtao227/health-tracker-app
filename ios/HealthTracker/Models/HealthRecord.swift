import Foundation
import SwiftData

@Model
final class HealthRecord {
    var id: UUID = UUID()
    var userId: Int = 1
    var date: Date = Date()
    var sbp: Int?  // Systolic Blood Pressure
    var dbp: Int?  // Diastolic Blood Pressure
    var hr: Int?   // Heart Rate
    var weight: Double?
    
    init(userId: Int = 1, date: Date = Date(), sbp: Int? = nil, dbp: Int? = nil, hr: Int? = nil, weight: Double? = nil) {
        self.id = UUID()
        self.userId = userId
        self.date = date
        self.sbp = sbp
        self.dbp = dbp
        self.hr = hr
        self.weight = weight
    }
    
    // For CSV export
    var csvRow: String {
        let dateStr = Self.dateFormatter.string(from: date)
        return "\(dateStr),\(sbp ?? 0),\(dbp ?? 0),\(hr ?? 0),\(weight ?? 0)"
    }
    
    static let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        return f
    }()
}
