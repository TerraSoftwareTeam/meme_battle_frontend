package tech.dev.codegen.network.model

data class ApiEndpoint(
    val operationId: String,
    val method: String, // GET, POST, etc
    val path: String,
    val summary: String?,
    val requestBodyType: String?, // DTO class name or primitive
    val responseType: String,    // DTO class name, primitive, or "List<X>"
    val isList: Boolean,         // true if response is array
    val queryParams: List<ApiParam>,
    val pathParams: List<ApiParam>
)

data class ApiParam(
    val name: String,
    val type: String,
    val isRequired: Boolean
)
