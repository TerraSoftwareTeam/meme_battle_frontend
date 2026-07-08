import tech.dev.codegen.feature.tasks.GenerateFeatureTask
import tech.dev.codegen.network.task.GenerateNetworkModuleTask
import tech.dev.codegen.network.task.SwitchNetworkVersionTask
import tech.dev.codegen.network.task.RemoveNetworkVersionTask

tasks.register<GenerateFeatureTask>("generateFeature") {
    group = "tech.dev.codegen"
    description = "Generates a new KMP feature"
}

tasks.register<GenerateNetworkModuleTask>("generateNetworkModule") {
    group = "network-codegen"
    description = "Generates a new versioned network module from an OpenAPI spec."
}

tasks.register<SwitchNetworkVersionTask>("switchNetworkVersion") {
    group = "network-codegen"
    description = "Switches the current network module alias to a specific version."
}

tasks.register<RemoveNetworkVersionTask>("removeNetworkVersion") {
    group = "network-codegen"
    description = "Removes a specific network module version (or the latest if not specified)."
}
