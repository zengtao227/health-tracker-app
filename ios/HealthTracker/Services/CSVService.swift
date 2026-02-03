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
    /// Parse CSV string to record data
    /// Parse CSV string to record data
    static func parseCSV(_ content: String) -> [(date: Date, sbp: Int?, dbp: Int?, hr: Int?, weight: Double?)] {
        var results: [(date: Date, sbp: Int?, dbp: Int?, hr: Int?, weight: Double?)] = []
        
        let standardFormatter = DateFormatter()
        standardFormatter.dateFormat = "yyyy-MM-dd"
        
        // Formatter for BP CSV: 01/23/2026 09:12
        let bpFormatter = DateFormatter()
        bpFormatter.dateFormat = "MM/dd/yyyy HH:mm"
        
        let lines = content.components(separatedBy: .newlines)
        guard let header = lines.first?.lowercased() else { return [] }
        
        // Detect Formats
        let isFitbit = header.contains("weight grams")
        let isBP = header.contains("sys") && header.contains("dia") && header.contains("pul")
        
        for (index, line) in lines.enumerated() {
            // Skip header
            if index == 0 { continue }
            let lowerLine = line.lowercased()
            
            // Skip repeated headers
            if lowerLine.contains("date,sbp") || 
               (isFitbit && lowerLine.contains("weight grams")) ||
               (isBP && lowerLine.contains("sys") && lowerLine.contains("dia")) { 
                continue 
            }
            
            let parts = line.components(separatedBy: ",")
            guard parts.count >= 1, !parts[0].isEmpty else { continue }
            
            if isBP {
                // Format: DATE,TIME,SYS,DIA,PUL,BPZ,TAGS
                // Example: 01/23/2026,09:12,128,93,59,Stage 1 Hypertension,
                // Index:   0          1     2   3  4
                
                guard parts.count >= 5 else { continue }
                
                let dateStr = parts[0].trimmingCharacters(in: .whitespaces)
                let timeStr = parts[1].trimmingCharacters(in: .whitespaces)
                let combinedDate = "\(dateStr) \(timeStr)"
                
                guard let date = bpFormatter.date(from: combinedDate) else { continue }
                
                let sbp = Int(parts[2].trimmingCharacters(in: .whitespaces))
                let dbp = Int(parts[3].trimmingCharacters(in: .whitespaces))
                let hr = Int(parts[4].trimmingCharacters(in: .whitespaces))
                
                results.append((date: date, sbp: sbp, dbp: dbp, hr: hr, weight: nil))
                
            } else if isFitbit {
                let dateStr = parts[0].trimmingCharacters(in: .whitespaces)
                let datePrefix = String(dateStr.prefix(10))
                
                guard let date = standardFormatter.date(from: datePrefix) else { continue }
                
                // Fitbit Format: timestamp,weight grams,data source
                let weightGrams = parts.count > 1 ? Double(parts[1].trimmingCharacters(in: .whitespaces)) : nil
                let weightKg = weightGrams != nil ? weightGrams! / 1000.0 : nil
                
                results.append((date: date, sbp: nil, dbp: nil, hr: nil, weight: weightKg))
                
            } else {
                // Standard Format: Date,SBP,DBP,HR,Weight
                let dateStr = parts[0].trimmingCharacters(in: .whitespaces)
                let datePrefix = String(dateStr.prefix(10))
                
                guard let date = standardFormatter.date(from: datePrefix) else { continue }
                
                let sbp = parts.count > 1 ? Int(parts[1].trimmingCharacters(in: .whitespaces)) : nil
                let dbp = parts.count > 2 ? Int(parts[2].trimmingCharacters(in: .whitespaces)) : nil
                let hr = parts.count > 3 ? Int(parts[3].trimmingCharacters(in: .whitespaces)) : nil
                let weight = parts.count > 4 ? Double(parts[4].trimmingCharacters(in: .whitespaces)) : nil
                
                results.append((date: date, sbp: sbp, dbp: dbp, hr: hr, weight: weight))
            }
        }
        
        return results
    }
}
