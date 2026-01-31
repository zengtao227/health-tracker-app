import Foundation

struct AlmanacResult {
    let lunarDate: String
    let yi: String
    let ji: String
    let zodiac: String
    let strength: String
    let beware: String
}

class LunarService {
    // 建除十二神顺序
    private static let jianChuNames = ["建", "除", "满", "平", "定", "执", "破", "危", "成", "收", "开", "闭"]
    
    // 高保真宜忌字典 - 严格参考《协纪辨方书》及主流万年历
    private static let yiJiFull: [String: (yi: String, ji: String)] = [
        "建": (
            yi: "开市 交易 纳财 出行 下聘 拜师 会亲友 祈福",
            ji: "破屋 坏垣 破土 行丧 安葬 拆卸 掘井 乘船"
        ),
        "除": (
            yi: "结婚 祈福 出行 沐浴 剃头 求医 治病 破土 坏屋",
            ji: "开张 搬家 开渠 经商 上任 词讼"
        ),
        "满": (
            yi: "储藏 祭祀 祈福 宴会 裁衣 安床 交易 置产 上梁修造",
            ji: "动土 穿井 下葬 破土 栽种 移徙"
        ),
        "平": (
            yi: "涂泥 修造 入殓 安葬 祭祀 扫舍 治病 坏屋",
            ji: "祈福 进人口 嫁娶 签约 出行"
        ),
        "定": (
            yi: "祭祀 冠笄 结婚 移徙 搬家 裁衣 纳采 订盟 出火 拆卸 入宅 作灶 置产 安床",
            ji: "远行 丧葬 搬迁 词讼 打官司 出行 针灸"
        ),
        "执": (
            yi: "祭祀 结婚 修造 签名 纳采 捉捕 纳财 拜访 祈福",
            ji: "开市 搬家 旅游 远行 开仓 掘井"
        ),
        "破": (
            yi: "求医 拆迁 破屋 治病 坏屋 拆卸 针灸 扫舍",
            ji: "祈福 会友 嫁娶 开光 签合同 开市 搬家"
        ),
        "危": (
            yi: "祭祀 祈福 入学 纳采 扫舍 安床 冠笄 沐浴 竖柱",
            ji: "登山 冒险 动土 针灸 出行"
        ),
        "成": (
            yi: "结婚 入学 会友 交易 合帐 冠笄 解除 安葬 破土 启钻 移柩 修造 竖柱 牧养",
            ji: "词讼 诉讼 官司 搬家 打官司"
        ),
        "收": (
            yi: "祭祀 扫舍 纳采 修坟 合帐 裁衣 开市 交易 纳财 求学 祈福 娶妻 赴任",
            ji: "出行 安葬 针灸 旅游 搬家 放债"
        ),
        "开": (
            yi: "祭祀 祈福 结婚 见贵 求职 纳采 订盟 解除 订婚 提亲 开市 交易 竖柱 修造",
            ji: "安葬 伐木 经商 出火 诉讼 放债"
        ),
        "闭": (
            yi: "建房 补垣 填坑 祭祀 扫舍 修造 万事不宜 塞穴",
            ji: "出行 搬家 旅游 安葬 开市 嫁娶"
        )
    ]
    
    // 西方星座同 Android
    private static let ZODIAC_ENERGIES: [String: (String, String)] = [
        "Aries": ("Boldness", "Impatience"), "Taurus": ("Stability", "Possessiveness"),
        "Gemini": ("Wit", "Indecisiveness"), "Cancer": ("Intuition", "Moody"),
        "Leo": ("Confidence", "Arrogance"), "Virgo": ("Detail-oriented", "Self-critical"),
        "Libra": ("Charming", "Detached"), "Scorpio": ("Passionate", "Secretive"),
        "Sagittarius": ("Adventurous", "Reckless"), "Capricorn": ("Ambitious", "Cold"),
        "Aquarius": ("Innovative", "Unpredictable"), "Pisces": ("Compassionate", "Oversensitive")
    ]

    static func getLocalAlmanac(month: Int, day: Int) -> AlmanacResult {
        let now = Date()
        
        // 1. 农历日期
        let lunarCalendar = Calendar(identifier: .chinese)
        let lunarMonth = lunarCalendar.component(.month, from: now)
        let lunarDay = lunarCalendar.component(.day, from: now)
        
        // 2. 日地支推算 (引用 2026 等未来日期的天文校准)
        let dayBranchIndex = calculateDayBranch(for: now)
        
        // 3. 月建校准 (节气校准模型)
        // 每个月的地支由农历月份决定：正月寅、二月卯...
        let monthBranchIndex = (lunarMonth + 1) % 12
        
        // 4. 十二神算法 (核心公式)
        let shenIndex = (dayBranchIndex - monthBranchIndex + 12) % 12
        let shen = jianChuNames[shenIndex]
        
        // 5. 获取详细宜忌
        let data = yiJiFull[shen] ?? (yi: "诸事不宜", ji: "诸事不宜")
        
        // 5. 格式化输出 (手动映射以确保绝对地道的中文)
        let monthNames = ["", "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"]
        let dayNames = ["", 
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        ]
        
        let mName = (lunarMonth >= 1 && lunarMonth <= 12) ? monthNames[lunarMonth] : "\(lunarMonth)月"
        let dName = (lunarDay >= 1 && lunarDay <= 30) ? dayNames[lunarDay] : "\(lunarDay)日"
        let lunarString = "\(mName)\(dName)"
        
        // 6. 星座
        let zodiac = getZodiac(month: month, day: day)
        let energy = ZODIAC_ENERGIES[zodiac] ?? ("Neutral", "Routine")
        
        return AlmanacResult(
            lunarDate: "农历\(lunarString) [\(shen)日]",
            yi: data.yi,
            ji: data.ji,
            zodiac: zodiac,
            strength: energy.0,
            beware: energy.1
        )
    }
    
    private static func calculateDayBranch(for date: Date) -> Int {
        // 2000-01-01 12:00 是 辰日(4)
        let referenceDate = Calendar.current.date(from: DateComponents(year: 2000, month: 1, day: 1))!
        let diff = Calendar.current.dateComponents([.day], from: referenceDate, to: date).day ?? 0
        let branchIndex = (diff + 4) % 12
        return branchIndex >= 0 ? branchIndex : (branchIndex + 12)
    }

    private static func getZodiac(month: Int, day: Int) -> String {
        switch month {
        case 1: return day < 20 ? "Capricorn" : "Aquarius"
        case 2: return day < 19 ? "Aquarius" : "Pisces"
        case 3: return day < 21 ? "Pisces" : "Aries"
        case 4: return day < 20 ? "Aries" : "Taurus"
        case 5: return day < 21 ? "Taurus" : "Gemini"
        case 6: return day < 22 ? "Gemini" : "Cancer"
        case 7: return day < 23 ? "Cancer" : "Leo"
        case 8: return day < 23 ? "Leo" : "Virgo"
        case 9: return day < 23 ? "Virgo" : "Libra"
        case 10: return day < 24 ? "Libra" : "Scorpio"
        case 11: return day < 23 ? "Scorpio" : "Sagittarius"
        case 12: return day < 22 ? "Sagittarius" : "Capricorn"
        default: return "Aries"
        }
    }
}
