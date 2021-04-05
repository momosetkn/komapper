package org.komapper.core.dsl.context

import org.komapper.core.metamodel.ColumnInfo

internal data class SqlSetOperationContext<T>(
    val component: SqlSetOperationComponent<T>,
    val orderBy: List<ColumnInfo<*>> = listOf()
)

sealed class SqlSetOperationComponent<T> {
    internal class Leaf<T>(val context: SqlSelectContext<*>) : SqlSetOperationComponent<T>()
    internal class Composite<T>(
        val kind: SqlSetOperationKind,
        val left: SqlSetOperationComponent<*>,
        val right: SqlSetOperationComponent<T>
    ) :
        SqlSetOperationComponent<T>()
}

enum class SqlSetOperationKind {
    EXCEPT, INTERSECT, UNION, UNION_ALL
}