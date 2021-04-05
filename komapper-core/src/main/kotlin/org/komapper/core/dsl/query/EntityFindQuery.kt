package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.EntitySelectOptionDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.metamodel.ColumnInfo

interface EntityFindQuery<ENTITY, R> : Query<R> {
    fun where(declaration: WhereDeclaration): EntityFindQuery<ENTITY, R>
    fun orderBy(vararg items: ColumnInfo<*>): EntityFindQuery<ENTITY, R>
    fun offset(value: Int): EntityFindQuery<ENTITY, R>
    fun limit(value: Int): EntityFindQuery<ENTITY, R>
    fun forUpdate(): EntityFindQuery<ENTITY, R>
    fun option(declaration: EntitySelectOptionDeclaration): EntityFindQuery<ENTITY, R>
}

internal data class EntityFindQueryImpl<ENTITY, R>(
    private val query: EntitySelectQuery<ENTITY>,
    private val transformer: (ListQuery<ENTITY>) -> Query<R>
) :
    EntityFindQuery<ENTITY, R> {

    override fun where(declaration: WhereDeclaration): EntityFindQueryImpl<ENTITY, R> {
        val newQuery = query.where(declaration)
        return copy(query = newQuery)
    }

    override fun orderBy(vararg items: ColumnInfo<*>): EntityFindQueryImpl<ENTITY, R> {
        val newQuery = query.orderBy(*items)
        return copy(query = newQuery)
    }

    override fun limit(value: Int): EntityFindQuery<ENTITY, R> {
        val newQuery = query.limit(value)
        return copy(query = newQuery)
    }

    override fun offset(value: Int): EntityFindQueryImpl<ENTITY, R> {
        val newQuery = query.offset(value)
        return copy(query = newQuery)
    }

    override fun forUpdate(): EntityFindQueryImpl<ENTITY, R> {
        val newQuery = query.forUpdate()
        return copy(query = newQuery)
    }

    override fun option(declaration: EntitySelectOptionDeclaration): EntityFindQuery<ENTITY, R> {
        val newQuery = query.option(declaration)
        return copy(query = newQuery)
    }

    override fun execute(config: DatabaseConfig): R {
        return transformer(query).execute(config)
    }

    override fun statement(dialect: Dialect): Statement {
        return query.statement(dialect)
    }
}