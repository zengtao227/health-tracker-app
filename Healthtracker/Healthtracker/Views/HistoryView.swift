import SwiftUI
import UniformTypeIdentifiers
#if os(macOS)
import AppKit
#elseif os(iOS)
import UIKit
#endif

struct HistoryView: View {
    let records: [HealthRecord]
    let profile: UserProfile?
    let lang: String
    let onLangToggle: () -> Void
    let onImport: ([(date: Date, sbp: Int?, dbp: Int?, hr: Int?, weight: Double?)]) -> Void
    let onDelete: (Set<UUID>) -> Void
    
    @State private var showingExportDialog = false
    @State private var showingImportDialog = false
    @State private var showingFilePicker = false
    @State private var showingShareSheet = false
    @State private var showingDeleteAlert = false
    @State private var csvContent = ""
    @State private var selection = Set<UUID>()
    
    var body: some View {
        NavigationStack {
            List(selection: $selection) {
                // Header Row
                Section {
                    headerRow
                }
                
                // Data Rows
                ForEach(records, id: \.id) { record in
                    recordRow(record)
                        .tag(record.id)
                }
            }
            .navigationTitle(L10n.get("nav_history", lang))
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    HStack(spacing: 8) {
                        if !selection.isEmpty {
                            Button(role: .destructive) {
                                showingDeleteAlert = true
                            } label: {
                                Image(systemName: "trash")
                            }
                        }
                        
                        #if os(iOS)
                        EditButton()
                        #endif
                        
                        // Export Button
                        Button(action: { showingExportDialog = true }) {
                            Image(systemName: "square.and.arrow.up")
                        }
                        
                        // Import Button
                        Button(action: { showingImportDialog = true }) {
                            Image(systemName: "plus.circle")
                        }
                        
                        // Language Toggle
                        Button(action: onLangToggle) {
                            Text(lang == "zh" ? "En" : "中")
                                .font(.caption)
                                .fontWeight(.bold)
                        }
                    }
                }
            }
            .alert(L10n.get("confirm_delete", lang), isPresented: $showingDeleteAlert) {
                Button(L10n.get("delete", lang), role: .destructive) {
                    onDelete(selection)
                    selection.removeAll()
                }
                Button(L10n.get("cancel", lang), role: .cancel) {}
            } message: {
                Text(L10n.get("delete_message", lang))
            }
            // Export Dialog
            .confirmationDialog(
                L10n.get("export_data", lang),
                isPresented: $showingExportDialog,
                titleVisibility: .visible
            ) {
                Button(L10n.get("share_text", lang)) {
                    csvContent = CSVService.exportToCSV(records: records)
                    showingShareSheet = true
                }
                
                Button(L10n.get("save_to_file", lang)) {
                    csvContent = CSVService.exportToCSV(records: records)
                    saveToFile()
                }
                
                Button(L10n.get("cancel", lang), role: .cancel) {}
            } message: {
                Text(L10n.get("choose_method", lang))
            }
            // Import Dialog
            .confirmationDialog(
                L10n.get("import_data", lang),
                isPresented: $showingImportDialog,
                titleVisibility: .visible
            ) {
                Button(L10n.get("select_file", lang)) {
                    showingFilePicker = true
                }
                
                Button(L10n.get("cancel", lang), role: .cancel) {}
            } message: {
                Text(L10n.get("import_hint", lang))
            }
            // File Picker
            .fileImporter(
                isPresented: $showingFilePicker,
                allowedContentTypes: [.commaSeparatedText, .plainText],
                allowsMultipleSelection: false
            ) { result in
                handleFileImport(result)
            }
            // Share Sheet
            .sheet(isPresented: $showingShareSheet) {
                #if os(iOS)
                ShareSheet(items: [csvContent])
                #else
                MacShareSheet(content: csvContent)
                #endif
            }
        }
    }
    
    // MARK: - Header Row
    
    private var headerRow: some View {
        HStack {
            Text(L10n.get("date", lang))
                .font(.caption)
                .fontWeight(.bold)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Text(L10n.get("bp", lang))
                .font(.caption)
                .fontWeight(.bold)
                .foregroundStyle(.secondary)
                .frame(width: 70)
            
            Text(L10n.get("hr", lang))
                .font(.caption)
                .fontWeight(.bold)
                .foregroundStyle(.secondary)
                .frame(width: 40)
            
            Text(L10n.get("weight", lang))
                .font(.caption)
                .fontWeight(.bold)
                .foregroundStyle(.secondary)
                .frame(width: 50)
        }
    }
    
    // MARK: - Record Row
    
    private func recordRow(_ record: HealthRecord) -> some View {
        HStack {
            Text(formatDate(record.date))
                .font(.subheadline)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Text("\(record.sbp ?? 0)/\(record.dbp ?? 0)")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(.red)
                .frame(width: 70)
            
            Text("\(record.hr ?? 0)")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(.orange)
                .frame(width: 40)
            
            Text(String(format: "%.1f", record.weight ?? 0))
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(.purple)
                .frame(width: 50)
        }
    }
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }
    
    // MARK: - File Handling
    
    private func handleFileImport(_ result: Result<[URL], Error>) {
        switch result {
        case .success(let urls):
            guard let url = urls.first else { return }
            
            // Start accessing security-scoped resource
            guard url.startAccessingSecurityScopedResource() else { return }
            defer { url.stopAccessingSecurityScopedResource() }
            
            do {
                let content = try String(contentsOf: url, encoding: .utf8)
                let parsed = CSVService.parseCSV(content)
                onImport(parsed)
            } catch {
                print("Error reading file: \(error)")
            }
            
        case .failure(let error):
            print("File picker error: \(error)")
        }
    }
    
    private func saveToFile() {
        let fileName = "HealthRecords_\(profile?.name ?? "P1").csv"
        
        guard let documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else { return }
        
        let fileURL = documentsURL.appendingPathComponent(fileName)
        
        do {
            try csvContent.write(to: fileURL, atomically: true, encoding: .utf8)
            // Show share sheet with the file
            csvContent = "File saved to: \(fileURL.path)"
            showingShareSheet = true
        } catch {
            print("Error saving file: \(error)")
        }
    }
}

// MARK: - Share Sheet

#if os(iOS)
struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]
    
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
#else
struct MacShareSheet: View {
    let content: String
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Export Result")
                .font(.headline)
            TextEditor(text: .constant(content))
                .frame(width: 400, height: 200)
                .border(Color.gray, width: 1)
            
            HStack {
                Button("Copy to Clipboard") {
                    NSPasteboard.general.clearContents()
                    NSPasteboard.general.setString(content, forType: .string)
                    dismiss()
                }
                Button("Close") {
                    dismiss()
                }
            }
        }
        .padding()
    }
}
#endif

#Preview {
    HistoryView(
        records: [],
        profile: UserProfile(id: 1, name: "Test"),
        lang: "zh",
        onLangToggle: {},
        onImport: { _ in },
        onDelete: { _ in }
    )
}
