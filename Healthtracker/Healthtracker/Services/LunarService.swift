import Foundation

/// Lunar calendar and almanac service
struct LunarService {
    struct AlmanacResult {
        let lunarDate: String
        let yi: String  // Auspicious
        let ji: String  // Inauspicious
    }
    
    static func getLocalAlmanac(for date: Date = Date()) -> AlmanacResult {
        // Simplified lunar calculation
        // In production, use a proper lunar calendar library
        let calendar = Calendar(identifier: .chinese)
        let components = calendar.dateComponents([.year, .month, .day], from: date)
        
        let month = components.month ?? 1
        let day = components.day ?? 1
        
        let monthNames = ["正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"]
        let dayNames = ["初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"]
        
        let monthStr = month <= monthNames.count ? monthNames[month - 1] : "正"
        let dayStr = day <= dayNames.count ? dayNames[day - 1] : "初一"
        
        let lunarDate = "\(monthStr)月\(dayStr)"
        
        // Simplified yi/ji based on day
        let yiOptions = ["祈福 求嗣", "出行 交易", "嫁娶 订盟", "开市 立券", "安床 移徙", "修造 动土"]
        let jiOptions = ["开市 动土", "嫁娶 远行", "安葬 破土", "词讼 出行", "入宅 安门", "祈福 求医"]
        
        let yi = yiOptions[day % yiOptions.count]
        let ji = jiOptions[(day + 3) % jiOptions.count]
        
        return AlmanacResult(lunarDate: lunarDate, yi: yi, ji: ji)
    }
}
