package tech.dev.codegen.network.parser

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import tech.dev.codegen.network.model.ApiEndpoint
import tech.dev.codegen.network.model.ApiParam

object NetworkSpecMapper {

    fun mapEndpoints(openApi: OpenAPI, tagFilter: String?): List<ApiEndpoint> {
        val endpoints = mutableListOf<ApiEndpoint>()

        openApi.paths?.forEach { (path, pathItem) ->
            pathItem.readOperationsMap().forEach { (httpMethod, operation) ->
                if (tagFilter.isNullOrBlank() || operation.tags?.contains(tagFilter) == true) {
                    endpoints.add(mapOperation(path, httpMethod.name, operation))
                }
            }
        }

        return endpoints
    }

    private fun mapOperation(path: String, method: String, operation: Operation): ApiEndpoint {
        val queryParams = mutableListOf<ApiParam>()
        val pathParams = mutableListOf<ApiParam>()

        operation.parameters?.forEach { param ->
            val type = TypeResolver.resolve(param.schema, "IGNORED").toString()
            val isRequired = param.required == true
            when (param.`in`) {
                "query" -> queryParams.add(ApiParam(param.name, type, isRequired))
                "path"  -> pathParams.add(ApiParam(param.name, type, isRequired))
            }
        }

        val requestBodyType = operation.requestBody?.content?.get("application/json")?.schema?.let {
            it.`$ref`?.substringAfterLast("/") ?: it.name
        }

        // Determine response type — handle both object ($ref) and array (type=array, items.$ref)
        val responseSchema = operation.responses?.get("200")?.content?.get("application/json")?.schema
        val isList: Boolean
        val responseType: String
        if (responseSchema != null) {
            when {
                responseSchema.type == "array" || responseSchema.types?.contains("array") == true -> {
                    isList = true
                    val itemRef = responseSchema.items?.`$ref`?.substringAfterLast("/")
                        ?: responseSchema.items?.name
                    responseType = itemRef ?: "Unit"
                }
                responseSchema.`$ref` != null -> {
                    isList = false
                    responseType = responseSchema.`$ref`!!.substringAfterLast("/")
                }
                else -> {
                    isList = false
                    responseType = responseSchema.name ?: "Unit"
                }
            }
        } else {
            isList = false
            responseType = "Unit"
        }

        // Prefer operationId from spec (already camelCase per OpenAPI convention)
        val opId = operation.operationId
            ?.let { snakeToCamel(it) }
            ?: generateOperationId(method, path)

        return ApiEndpoint(
            operationId = opId,
            method = method,
            path = path,
            summary = operation.summary,
            requestBodyType = requestBodyType,
            responseType = responseType,
            isList = isList,
            queryParams = queryParams,
            pathParams = pathParams
        )
    }

    /** Converts snake_case or mixed operationId to lowerCamelCase */
    private fun snakeToCamel(s: String): String =
        s.split("_").mapIndexed { i, part ->
            if (i == 0) part else part.replaceFirstChar { it.uppercase() }
        }.joinToString("")

    private fun generateOperationId(method: String, path: String): String {
        val cleanPath = path.replace(Regex("[^a-zA-Z0-9]"), "_").trim('_')
        return "${method.lowercase()}_$cleanPath".replace(Regex("_+([a-z])")) {
            it.groupValues[1].uppercase()
        }.replace("_", "")
    }
}
