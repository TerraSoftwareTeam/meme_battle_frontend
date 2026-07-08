package tech.dev.codegen.network.generator

import tech.dev.codegen.network.model.ApiEndpoint
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

object ApiServiceImplGenerator {

    fun generate(moduleDir: File, pkg: String, serviceName: String, endpoints: List<ApiEndpoint>) {
        val interfaceName = ClassName("$pkg.api", "${serviceName.replaceFirstChar { it.uppercase() }}ApiService")
        val implName = "${serviceName.replaceFirstChar { it.uppercase() }}ApiServiceImpl"
        
        val clientClass = ClassName("io.ktor.client", "HttpClient")
        
        val typeSpec = TypeSpec.classBuilder(implName)
            .addSuperinterface(interfaceName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("client", clientClass)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("client", clientClass)
                    .initializer("client")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )

        endpoints.forEach { endpoint ->
            val funSpec = FunSpec.builder(endpoint.operationId)
                .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)

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

            // Build Ktor Request
            val method = endpoint.method.lowercase()
            
            // Handle Path Params in URL
            var urlStr = "\"${endpoint.path}\""
            endpoint.pathParams.forEach { param ->
                urlStr = urlStr.replace("{${param.name}}", "\$${param.name}")
            }

            val codeBlock = CodeBlock.builder()
            codeBlock.beginControlFlow("return safeCall")
            codeBlock.beginControlFlow("client.%L(%L)", method, urlStr)
            
            endpoint.queryParams.forEach { param ->
                if (!param.isRequired) {
                    codeBlock.beginControlFlow("if (%L != null)", param.name)
                }
                codeBlock.addStatement("parameter(%S, %L)", param.name, param.name)
                if (!param.isRequired) {
                    codeBlock.endControlFlow()
                }
            }

            if (endpoint.requestBodyType != null) {
                codeBlock.addStatement("setBody(body)")
            }

            codeBlock.endControlFlow()
            if (endpoint.responseType != "Unit") {
                codeBlock.add(".body<com.dev.memebattle.core.network.call.BaseResponse<%T>>().data\n", returnTypeName)
            } else {
                codeBlock.add(".let { Unit }\n")
            }
            codeBlock.endControlFlow()
            
            funSpec.addCode(codeBlock.build())
            typeSpec.addFunction(funSpec.build())
        }

        val fileSpec = FileSpec.builder("$pkg.api", implName)
            .addType(typeSpec.build())
            .addImport("io.ktor.client.call", "body")
            .addImport("io.ktor.client.request", "parameter", "setBody")
            .addImport("io.ktor.client.request", "get", "post", "put", "delete", "patch")
            .addImport("com.dev.memebattle.core.network.call", "safeCall")
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
