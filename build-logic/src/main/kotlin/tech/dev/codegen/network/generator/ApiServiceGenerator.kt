package tech.dev.codegen.network.generator

import tech.dev.codegen.network.model.ApiEndpoint
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import kotlin.collections.List

object ApiServiceGenerator {

    fun generate(moduleDir: File, pkg: String, serviceName: String, endpoints: List<ApiEndpoint>) {
        val interfaceName = "${serviceName.replaceFirstChar { it.uppercase() }}ApiService"
        
        val typeSpec = TypeSpec.interfaceBuilder(interfaceName)

        endpoints.forEach { endpoint ->
            val funSpec = FunSpec.builder(endpoint.operationId)
                .addModifiers(KModifier.SUSPEND)
                .addModifiers(KModifier.ABSTRACT)
            
            endpoint.summary?.let { funSpec.addKdoc("%L\n", it) }

            endpoint.pathParams.forEach { param ->
                funSpec.addParameter(param.name, getTypeName(param.type, pkg).copy(nullable = !param.isRequired))
            }
            endpoint.queryParams.forEach { param ->
                funSpec.addParameter(param.name, getTypeName(param.type, pkg).copy(nullable = !param.isRequired))
            }

            if (endpoint.requestBodyType != null) {
                funSpec.addParameter("body", ClassName("$pkg.dto", endpoint.requestBodyType))
            }

            val baseType = if (endpoint.responseType != "Unit") {
                ClassName("$pkg.dto", endpoint.responseType)
            } else {
                Unit::class.asTypeName()
            }
            val returnTypeName: TypeName = if (endpoint.isList && endpoint.responseType != "Unit") {
                List::class.asTypeName().parameterizedBy(baseType)
            } else {
                baseType
            }

            funSpec.returns(
                ClassName("com.dev.memebattle.core.network.call", "NetworkResult")
                    .parameterizedBy(returnTypeName)
            )

            typeSpec.addFunction(funSpec.build())
        }

        val fileSpec = FileSpec.builder("$pkg.api", interfaceName)
            .addType(typeSpec.build())
            .build()

        fileSpec.writeTo(File(moduleDir, "src/main/kotlin"))
    }

    private fun getTypeName(type: String, pkg: String): TypeName {
        return when (type) {
            "kotlin.Int" -> Int::class.asTypeName()
            "kotlin.Long" -> Long::class.asTypeName()
            "kotlin.Float" -> Float::class.asTypeName()
            "kotlin.Double" -> Double::class.asTypeName()
            "kotlin.Boolean" -> Boolean::class.asTypeName()
            "kotlin.String" -> String::class.asTypeName()
            else -> ClassName("$pkg.dto", type.substringAfterLast("."))
        }
    }
}
