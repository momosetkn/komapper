package org.komapper.processor

import com.google.devtools.ksp.symbol.Nullability
import org.komapper.processor.ClassNames.AutoIncrement
import org.komapper.processor.ClassNames.Clock
import org.komapper.processor.ClassNames.ConcurrentHashMap
import org.komapper.processor.ClassNames.EntityDescriptor
import org.komapper.processor.ClassNames.EntityMetamodel
import org.komapper.processor.ClassNames.EntityMetamodelImplementor
import org.komapper.processor.ClassNames.IdAssignment
import org.komapper.processor.ClassNames.IdContext
import org.komapper.processor.ClassNames.MetamodelDeclaration
import org.komapper.processor.ClassNames.PropertyDescriptor
import org.komapper.processor.ClassNames.PropertyMetamodel
import org.komapper.processor.ClassNames.PropertyMetamodelImpl
import org.komapper.processor.ClassNames.Sequence
import org.komapper.processor.ClassNames.UUID
import java.io.PrintWriter
import java.time.ZonedDateTime

internal class EntityMetamodelGenerator(
    private val entity: Entity,
    private val metaObject: String,
    private val aliases: List<String>,
    private val packageName: String,
    private val entityTypeName: String,
    private val simpleName: String,
    private val w: PrintWriter
) : Runnable {

    private val idTypeName: String = if (entity.idProperties.size == 1) {
        entity.idProperties[0].typeName
    } else {
        "List<Any>"
    }

    private val constructorParamList = listOf(
        "table: String = \"${entity.table.name}\"",
        "catalog: String = \"${entity.table.catalog}\"",
        "schema: String = \"${entity.table.schema}\"",
        "alwaysQuote: Boolean = ${entity.table.alwaysQuote}",
        "disableSequenceAssignment: Boolean = false",
        "declarations: List<$MetamodelDeclaration<$entityTypeName, $idTypeName, $simpleName>> = emptyList()"
    ).joinToString(", ")

    override fun run() {
        w.println("@file:Suppress(\"ClassName\", \"PrivatePropertyName\", \"UNUSED_PARAMETER\", \"unused\", \"RemoveRedundantQualifierName\", \"MemberVisibilityCanBePrivate\", \"RedundantNullableReturnType\")")

        if (packageName.isNotEmpty()) {
            w.println("package $packageName")
            w.println()
        }
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@$EntityMetamodelImplementor")
        w.println("class $simpleName private constructor($constructorParamList) : $EntityMetamodel<$entityTypeName, $idTypeName, $simpleName> {")
        w.println("    private val __tableName = table")
        w.println("    private val __catalogName = catalog")
        w.println("    private val __schemaName = schema")
        w.println("    private val __alwaysQuote = alwaysQuote")
        w.println("    private val __disableSequenceAssignment = disableSequenceAssignment")
        w.println("    private val __declarations = declarations")

        entityDescriptor()

        propertyMetamodels()

        klass()
        tableName()
        catalogName()
        schemaName()
        alwaysQuote()
        declarations()

        idAssignment()
        idProperties()
        versionProperty()
        createdAtProperty()
        updatedAtProperty()
        properties()
        toId()
        getId()
        preInsert()
        preUpdate()
        postUpdate()

        newEntity()
        newMetamodel()
        clone()

        companionObject()

        w.println("}")
        w.println()

        utils()
    }

    private fun entityDescriptor() {
        w.println("    private object $EntityDescriptor {")
        val sequenceIdExists = entity.properties.any {
            when (it.kind) {
                is PropertyKind.Id -> it.kind.idKind is IdKind.Sequence
                else -> false
            }
        }
        if (sequenceIdExists) {
            w.println("        val __idContextMap: $ConcurrentHashMap<$UUID, $IdContext> = $ConcurrentHashMap()")
        }
        for (p in entity.properties) {
            val exteriorTypeName = p.typeName
            val interiorTypeName = p.valueClass?.property?.typeName ?: p.typeName
            val exteriorClass = "$exteriorTypeName::class"
            val interiorClass = "$interiorTypeName::class"
            val columnName = "\"${p.column.name}\""
            val alwaysQuote = "${p.column.alwaysQuote}"
            val getter = "{ it.$p }"
            val setter = "{ e, v -> e.copy($p = v) }"
            val wrap = if (p.valueClass == null) "{ it }" else "{ ${p.valueClass}(it) }"
            val unwrap = if (p.valueClass == null) "{ it }" else "{ it.${p.valueClass.property} }"
            val nullable = if (p.nullability == Nullability.NULLABLE) "true" else "false"
            val propertyDescriptor =
                "$PropertyDescriptor<$entityTypeName, $exteriorTypeName, $interiorTypeName>"
            w.println("        val $p = $propertyDescriptor($exteriorClass, $interiorClass, \"$p\", $columnName, $alwaysQuote, $getter, $setter, $wrap, $unwrap, $nullable)")
        }
        w.println("    }")
    }

    private fun propertyMetamodels() {
        for (p in entity.properties) {
            val exteriorTypeName = p.typeName
            val interiorTypeName = p.valueClass?.property?.typeName ?: p.typeName
            val propertyMetamodel =
                "$PropertyMetamodel<$entityTypeName, $exteriorTypeName, $interiorTypeName>"
            w.println("    val $p: $propertyMetamodel by lazy { $PropertyMetamodelImpl(this, $EntityDescriptor.$p) }")
        }
    }

    private fun klass() {
        w.println("    override fun klass() = $entityTypeName::class")
    }

    private fun tableName() {
        w.println("    override fun tableName() = __tableName")
    }

    private fun catalogName() {
        w.println("    override fun catalogName() = __catalogName")
    }

    private fun schemaName() {
        w.println("    override fun schemaName() = __schemaName")
    }

    private fun alwaysQuote() {
        w.println("    override fun alwaysQuote() = __alwaysQuote")
    }

    private fun declarations() {
        w.println("    override fun declarations() = __declarations")
    }

    private fun idAssignment() {
        val pair = entity.properties.mapNotNull {
            when (it.kind) {
                is PropertyKind.Id -> {
                    val idKind = it.kind.idKind
                    if (idKind != null) it to idKind else null
                }
                else -> null
            }
        }.firstOrNull()

        w.print("    override fun idAssignment(): $IdAssignment<$entityTypeName>? = ")
        if (pair != null) {
            val (p, idKind) = pair
            val assignment = when (idKind) {
                is IdKind.AutoIncrement -> {
                    "$AutoIncrement(::toId, $p)"
                }
                is IdKind.Sequence -> {
                    val paramList = listOf(
                        "::toId",
                        "$p",
                        "$EntityDescriptor.__idContextMap",
                        "\"${idKind.name}\"",
                        "\"${idKind.catalog}\"",
                        "\"${idKind.schema}\"",
                        "${idKind.alwaysQuote}",
                        "${idKind.startWith}",
                        "${idKind.incrementBy}",
                        "__disableSequenceAssignment"
                    ).joinToString(", ")
                    "$Sequence($paramList)"
                }
            }
            w.println(assignment)
        } else {
            w.println("null")
        }
    }

    private fun idProperties() {
        val idNameList = entity.idProperties.joinToString { it.toString() }
        w.println("    override fun idProperties(): List<$PropertyMetamodel<$entityTypeName, *, *>> = listOf($idNameList)")
    }

    private fun versionProperty() {
        w.println("    override fun versionProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = ${entity.versionProperty}")
    }

    private fun createdAtProperty() {
        w.println("    override fun createdAtProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = ${entity.createdAtProperty}")
    }

    private fun updatedAtProperty() {
        w.println("    override fun updatedAtProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = ${entity.updatedAtProperty}")
    }

    private fun properties() {
        val nameList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { it.toString() }
        w.println("    override fun properties(): List<$PropertyMetamodel<$entityTypeName, *, *>> = listOf($nameList)")
    }

    private fun toId() {
        val body = if (entity.idProperties.size == 1) {
            val p = entity.idProperties[0]
            val id = when (p.valueClass?.property?.typeName ?: p.typeName) {
                "kotlin.Int" -> "generatedKey.toInt()"
                "kotlin.Long" -> "generatedKey"
                "kotlin.UInt" -> "generatedKey.toUInt()"
                else -> null
            }
            if (id == null) "null" else "this.$p.wrap($id)"
        } else {
            "null"
        }
        w.println("    override fun toId(generatedKey: Long): $idTypeName? = $body")
    }

    private fun getId() {
        val body = if (entity.idProperties.size == 1) {
            val p = entity.idProperties[0]
            val nullable = p.nullability == Nullability.NULLABLE
            "e.$p" + if (nullable) " ?: error(\"The id property '$p' must not null.\")" else ""
        } else {
            val list = entity.idProperties.joinToString {
                val nullable = it.nullability == Nullability.NULLABLE
                "e.$it" + if (nullable) " ?: error(\"The id property '$it' must not null.\")" else ""
            }
            "listOf($list)"
        }
        w.println("    override fun getId(e: $entityTypeName): $idTypeName = $body")
    }

    private fun preInsert() {
        val version = entity.versionProperty?.let {
            val nullable = it.nullability == Nullability.NULLABLE
            if (it.valueClass == null) {
                val tag = it.literalTag
                "$it = e.$it${if (nullable) " ?: 0$tag" else ""}"
            } else {
                val tag = it.valueClass.property.literalTag
                "$it = e.$it${if (nullable) " ?: ${it.valueClass}(0$tag)" else ""}"
            }
        }
        val createdAt = entity.createdAtProperty?.let {
            if (it.valueClass == null) {
                "$it = ${it.typeName}.now(c)"
            } else {
                "$it = ${it.typeName}(${it.valueClass.property.typeName}.now(c))"
            }
        }
        val updatedAt = entity.updatedAtProperty?.let {
            if (it.valueClass == null) {
                "$it = ${it.typeName}.now(c)"
            } else {
                "$it = ${it.typeName}(${it.valueClass.property.typeName}.now(c))"
            }
        }
        val paramList = listOfNotNull(version, createdAt, updatedAt).joinToString()
        val body = if (paramList == "") {
            "e"
        } else {
            "e.copy($paramList)"
        }
        w.println("    override fun preInsert(e: $entityTypeName, c: $Clock): $entityTypeName = $body")
    }

    private fun preUpdate() {
        val updatedAt = entity.updatedAtProperty?.let {
            if (it.valueClass == null) {
                "$it = ${it.typeName}.now(c)"
            } else {
                "$it = ${it.typeName}(${it.valueClass.property.typeName}.now(c))"
            }
        }
        val body = if (updatedAt == null) {
            "e"
        } else {
            "e.copy($updatedAt)"
        }
        w.println("    override fun preUpdate(e: $entityTypeName, c: $Clock): $entityTypeName = $body")
    }

    private fun postUpdate() {
        val version = entity.versionProperty?.let {
            val nullable = it.nullability == Nullability.NULLABLE
            if (it.valueClass == null) {
                val tag = it.literalTag
                "$it = e.$it${if (nullable) "?" else ""}.inc()${if (nullable) " ?: 0$tag" else ""}"
            } else {
                val tag = it.valueClass.property.literalTag
                "$it = ${it.valueClass}(e.$it${if (nullable) "?" else ""}.${it.valueClass.property}${if (nullable) "?" else ""}.inc()${if (nullable) " ?: 0$tag" else ""})"
            }
        }
        val body = if (version == null) {
            "e"
        } else {
            "e.copy($version)"
        }
        w.println("    override fun postUpdate(e: $entityTypeName): $entityTypeName = $body")
    }

    private fun newEntity() {
        val argList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { p ->
            val nullability = if (p.nullability == Nullability.NULLABLE) "?" else ""
            "$p = m[this.$p] as ${p.typeName}$nullability"
        }
        w.println("    override fun newEntity(m: Map<$PropertyMetamodel<*, *, *>, Any?>) = $entityTypeName($argList)")
    }

    private fun newMetamodel() {
        val paramList =
            "table: String, catalog: String, schema: String, alwaysQuote: Boolean, disableSequenceAssignment: Boolean, declarations: List<$MetamodelDeclaration<$entityTypeName, $idTypeName, $simpleName>>"
        w.println("    override fun newMetamodel($paramList) = $simpleName(table, catalog, schema, alwaysQuote, disableSequenceAssignment, declarations)")
    }

    private fun clone() {
        w.println("    fun clone($constructorParamList) = $simpleName(table, catalog, schema, alwaysQuote, disableSequenceAssignment, declarations)")
    }

    private fun companionObject() {
        w.println("    companion object {")
        for (alias in aliases) {
            w.println("        val $alias = $simpleName()")
        }
        w.println("    }")
    }

    private fun utils() {
        for (alias in aliases) {
            w.println("val $metaObject.$alias get() = $simpleName.$alias")
        }
    }
}