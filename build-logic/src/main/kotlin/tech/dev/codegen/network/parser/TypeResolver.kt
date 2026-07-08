package tech.dev.codegen.network.parser

import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.ArraySchema
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName

object TypeResolver {

    fun resolve(schema: Schema<*>, dtoPackage: String): TypeName {
        // 1. Check $ref
        val ref = schema.`$ref`
        if (ref != null) {
            val name = ref.substringAfterLast("/")
            return ClassName(dtoPackage, name)
        }

        // 2. Check anyOf and oneOf (FastAPI nullable or union)
        val unions = listOfNotNull(schema.anyOf, schema.oneOf).flatten()
        if (unions.isNotEmpty()) {
            val nonNullSchema = unions.firstOrNull { it.type != "null" && it.types?.contains("null") != true }
            if (nonNullSchema != null) {
                return resolve(nonNullSchema, dtoPackage)
            }
        }

        // 3. Extract type string (handling OAS 3.1 types array)
        val typeStr = schema.type ?: schema.types?.firstOrNull { it != "null" }

        // If schema has a name (and it's an object), it's a DTO
        if (typeStr == "object" && schema.name != null) {
             return ClassName(dtoPackage, schema.name)
        }

        return when (typeStr) {
            "integer" -> {
                when (schema.format) {
                    "int64" -> Long::class.asTypeName()
                    else -> Int::class.asTypeName()
                }
            }
            "number" -> {
                when (schema.format) {
                    "float" -> Float::class.asTypeName()
                    else -> Double::class.asTypeName()
                }
            }
            "boolean" -> Boolean::class.asTypeName()
            "string" -> String::class.asTypeName()
            "array" -> {
                val itemsSchema = schema.items ?: (schema as? ArraySchema)?.items
                if (itemsSchema != null) {
                    val itemType = resolve(itemsSchema, dtoPackage)
                    List::class.asTypeName().parameterizedBy(itemType)
                } else {
                    List::class.asTypeName().parameterizedBy(ClassName("kotlinx.serialization.json", "JsonElement"))
                }
            }
            else -> ClassName("kotlinx.serialization.json", "JsonElement") // fallback
        }
    }
}
