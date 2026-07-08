import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinInitKt.initKoin(additionalModules: [])
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}