package tech.dev.codegen.network.parser

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions

object OpenApiParser {
    fun parse(specLocation: String): OpenAPI {
        val options = ParseOptions().apply {
            isResolve = true // Resolve $ref automatically
            isResolveFully = false
        }
        val result = OpenAPIV3Parser().readLocation(specLocation, null, options)
        if (result.messages.isNotEmpty()) {
            println("⚠️ Parser warnings/errors:")
            result.messages.forEach { println(" - $it") }
        }
        return result.openAPI ?: throw IllegalArgumentException("Failed to parse OpenAPI spec from $specLocation")
    }
}
