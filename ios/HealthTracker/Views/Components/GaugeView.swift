import SwiftUI

/// Gradient gauge with triangle indicator - matches Android SoftSegmentGauge
struct GaugeView: View {
    let value: Double
    let range: ClosedRange<Double>
    let colors: [Color]
    let stops: [Double]  // Transition points between colors
    
    private var fraction: Double {
        let clamped = min(max(value, range.lowerBound), range.upperBound)
        return (clamped - range.lowerBound) / (range.upperBound - range.lowerBound)
    }
    
    private var gradientStops: [Gradient.Stop] {
        guard colors.count > 1 else {
            return [Gradient.Stop(color: colors.first ?? .gray, location: 0)]
        }
        
        var result: [Gradient.Stop] = []
        let totalRange = range.upperBound - range.lowerBound
        
        // Add first color at start
        result.append(Gradient.Stop(color: colors[0], location: 0))
        
        // Add transition points
        for (index, stop) in stops.enumerated() {
            let location = (stop - range.lowerBound) / totalRange
            if index + 1 < colors.count {
                result.append(Gradient.Stop(color: colors[index + 1], location: location))
            }
        }
        
        // Add last color at end
        if let lastColor = colors.last {
            result.append(Gradient.Stop(color: lastColor, location: 1))
        }
        
        return result
    }
    
    var body: some View {
        VStack(spacing: 2) {
            // Gradient Bar
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    // Background gradient
                    RoundedRectangle(cornerRadius: 5)
                        .fill(
                            LinearGradient(
                                stops: gradientStops,
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(height: 10)
                    
                    // Triangle indicator
                    Image(systemName: "arrowtriangle.down.fill")
                        .font(.system(size: 14))
                        .foregroundStyle(.primary)
                        .offset(x: geo.size.width * fraction - 7, y: -8)
                }
            }
            .frame(height: 20)
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        // BMI Gauge
        GaugeView(
            value: 23,
            range: 15...35,
            colors: [.blue, .green, .yellow, .orange, .red],
            stops: [18.5, 24, 28, 32]
        )
        
        // BP WHO Gauge
        GaugeView(
            value: 2.5,
            range: 0...6,
            colors: [.green, .mint, .yellow, .orange, .red, .purple],
            stops: [1, 2, 3, 4, 5]
        )
    }
    .padding()
}
