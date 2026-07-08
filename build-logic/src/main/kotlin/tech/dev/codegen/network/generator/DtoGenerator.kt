package tech.dev.codegen.network.generator

import io.swagger.v3.oas.models.media.Schema
import com.squareup.kotlinpoet.*
import tech.dev.codegen.network.parser.TypeResolver
import java.io.File

object DtoGenerator {

    fun generate(moduleDir: File, pkg: String, schemas: Map<String, Schema<*>>?) {
        if (schemas == null) return
        val dtoDir = File(moduleDir, "src/main/kotlin/${pkg.replace(".", "/")}/dto").also { it.mkdirs() }

        schemas.forEach { (name, schema) ->
            if (schema.enum != null && schema.enum.isNotEmpty()) {
                val fileSpec = generateEnumClass(pkg, name, schema)
                fileSpec.writeTo(File(moduleDir, "src/main/kotlin"))
            } else if (schema.type == "object" || schema.properties != null) {
                val fileSpec = generateDtoClass(pkg, name, schema)
                fileSpec.writeTo(File(moduleDir, "src/main/kotlin"))
            }
        }
    }

    private fun generateDtoClass(pkg: String, name: String, schema: Schema<*>): FileSpec {
        val typeSpecBuilder = TypeSpec.classBuilder(name)
            .addModifiers(KModifier.DATA)
            .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))

        val primaryConstructor = FunSpec.constructorBuilder()

        schema.properties?.forEach { (propName, propSchema) ->
            val isRequired = schema.required?.contains(propName) == true
            val isExplicitlyNullable = propSchema.nullable == true || 
                propSchema.types?.contains("null") == true || 
                propSchema.anyOf?.any { it.type == "null" || it.types?.contains("null") == true } == true

            val isNullable = !isRequired || isExplicitlyNullable
            val typeName = TypeResolver.resolve(propSchema, "$pkg.dto").copy(nullable = isNullable)
            
            val paramBuilder = ParameterSpec.builder(propName, typeName)
            if (isNullable) {
                paramBuilder.defaultValue("null")
            }
            
            val propBuilder = PropertySpec.builder(propName, typeName)
                .initializer(propName)
                .addAnnotation(
                    AnnotationSpec.builder(ClassName("kotlinx.serialization", "SerialName"))
                        .addMember("%S", propName)
                        .build()
                )

            primaryConstructor.addParameter(paramBuilder.build())
            typeSpecBuilder.addProperty(propBuilder.build())
        }

        typeSpecBuilder.primaryConstructor(primaryConstructor.build())

        return FileSpec.builder("$pkg.dto", name)
            .addType(typeSpecBuilder.build())
            .build()
    }

    private fun generateEnumClass(pkg: String, name: String, schema: Schema<*>): FileSpec {
        val typeSpecBuilder = TypeSpec.enumBuilder(name)
            .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))

        schema.enum.forEach { enumValue ->
            val valueStr = enumValue.toString()
            var constantName = valueStr.replace(Regex("[^a-zA-Z0-9]"), "_").uppercase()
            if (constantName.isEmpty()) constantName = "EMPTY"
            if (constantName.first().isDigit()) constantName = "VALUE_$constantName"
            
            val constantSpec = TypeSpec.anonymousClassBuilder()
                .addAnnotation(
                    AnnotationSpec.builder(ClassName("kotlinx.serialization", "SerialName"))
                        .addMember("%S", valueStr)
                        .build()
                )
                .build()
                
            typeSpecBuilder.addEnumConstant(constantName, constantSpec)
        }

        return FileSpec.builder("$pkg.dto", name)
            .addType(typeSpecBuilder.build())
            .build()
    }
}
