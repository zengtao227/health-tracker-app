import Foundation

/// CSV import/export service
struct CSVService {
    static let header = "Date,SBP,DBP,HR,Weight"
    
    /// Generate CSV string from records
    static func exportToCSV(records: [HealthRecord]) -> String {
        var lines = [header]
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        
        for record in records {
            let dateStr = formatter.string(from: record.date)
            let sbp = record.sbp.map { String($0) } ?? ""
            let dbp = record.dbp.map { String($0) } ?? ""
            let hr = record.hr.map { String($0) } ?? ""
            let weight = record.weight.map { String($0) } ?? ""
            lines.append("\(dateStr),\(sbp),\(dbp),\(hr),\(weight)")
        }
        
        return lines.joined(separator: "\n")
    }
    
    /// Parse CSV string to record data
    static func parseCSV(_ content: String) -> [(date: Date, sbp: Int?, dbp: Int?, hr: Int?, weight: Double?)] {
        var results: [(date: Date, sbp: Int?, dbp: Int?, hr: Int?, weight: Double?)] = []
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        
        let lines = content.components(separatedBy: .newlines)
        
        for (index, line) in lines.enumerated() {
            // Skip header
            if index == 0 && line.lowercased().contains("date") { continue }
            
            let parts = line.components(separatedBy: ",")
            guard parts.count >= 1, !parts[0].isEmpty else { continue }
            
            let dateStr = parts[0].trimmingCharacters(in: .whitespaces)
            guard let date = formatter.date(from: dateStr.prefix(10).description) else { continue }
            
            let sbp = parts.count > 1 ? Int(parts[1].trimmingCharacters(in: .whitespaces)) : nil
            let dbp = parts.count > 2 ? Int(parts[2].trimmingCharacters(in: .whitespaces)) : nil
            let hr = parts.count > 3 ? Int(parts[3].trimmingCharacters(in: .whitespaces)) : nil
            let weight = parts.count > 4 ? Double(parts[4].trimmingCharacters(in: .whitespaces)) : nil
            
            results.append((date: date, sbp: sbp, dbp: dbp, hr: hr, weight: weight))
        }
        
        return results
    }
}
