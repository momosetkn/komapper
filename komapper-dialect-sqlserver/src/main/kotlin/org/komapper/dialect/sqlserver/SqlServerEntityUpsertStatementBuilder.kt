package org.komapper.dialect.sqlserver

import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.AliasManager
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.TableNameType
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getNonAutoIncrementProperties

internal class SqlServerEntityUpsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: SqlServerDialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    entities: List<ENTITY>
) : EntityUpsertStatementBuilder<ENTITY> {

    private val target = context.target
    private val excluded = context.excluded
    private val aliasManager = UpsertAliasManager(target, excluded)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)
    private val sourceStatementBuilder = SourceStatementBuilder(dialect, context, entities)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        buf.append("merge into ")
        table(target, TableNameType.NAME_AND_ALIAS)
        buf.append(" using (")
        buf.append(sourceStatementBuilder.build())
        buf.append(") as ")
        table(excluded, TableNameType.ALIAS_ONLY)
        buf.append(" (")
        for (p in context.target.properties()) {
            columnWithoutAlias(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        buf.append(" on ")
        val excludedPropertyMap = excluded.properties().associateBy { it.name }
        for (key in context.keys) {
            column(key)
            buf.append(" = ")
            column(excludedPropertyMap[key.name]!!)
            buf.append(" and ")
        }
        buf.cutBack(5)
        if (context.duplicateKeyType == DuplicateKeyType.UPDATE) {
            buf.append(" when matched then update set ")
            for ((left, right) in assignments) {
                column(left)
                buf.append(" = ")
                operand(right)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        buf.append(" when not matched then insert (")
        for (p in target.getNonAutoIncrementProperties()) {
            columnWithoutAlias(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values (")
        for (p in excluded.getNonAutoIncrementProperties()) {
            columnWithoutAlias(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        buf.append(";")
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>, tableNameType: TableNameType) {
        support.visitTableExpression(expression, tableNameType)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }

    private fun columnWithoutAlias(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }

    private class UpsertAliasManager(
        val target: TableExpression<*>,
        val excluded: TableExpression<*>
    ) : AliasManager {

        override val index: Int = 0

        override fun getAlias(expression: TableExpression<*>): String {
            return when (expression) {
                target -> "t"
                excluded -> excluded.tableName()
                else -> ""
            }
        }
    }

    private class SourceStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
        val dialect: SqlServerDialect,
        val context: EntityUpsertContext<ENTITY, ID, META>,
        val entities: List<ENTITY>
    ) {

        private val buf = StatementBuffer()

        fun build(): Statement {
            val properties = context.target.properties()
            buf.append("values ")
            for (entity in entities) {
                buf.append("(")
                for (p in properties) {
                    buf.bind(p.toValue(entity))
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append("), ")
            }
            buf.cutBack(2)
            return buf.toStatement()
        }

        private fun column(expression: ColumnExpression<*, *>) {
            val name = expression.getCanonicalColumnName(dialect::enquote)
            buf.append(name)
        }
    }
}