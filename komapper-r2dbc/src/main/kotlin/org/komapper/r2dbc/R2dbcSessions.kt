package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.Logger
import org.komapper.r2dbc.spi.R2dbcSessionFactory
import java.util.ServiceLoader

object R2dbcSessions {
    fun get(connectionFactory: ConnectionFactory, logger: Logger): R2dbcSession {
        val loader = ServiceLoader.load(R2dbcSessionFactory::class.java)
        val factory = loader.firstOrNull()
        return factory?.create(connectionFactory, logger) ?: DefaultR2DbcSession(connectionFactory)
    }
}