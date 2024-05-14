package com.restaurant.be.common.config

import com.jillesvangurp.ktsearch.KtorRestClient
import com.jillesvangurp.ktsearch.SearchClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpTimeout
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EsConfig(
    @Value("\${es.host}")
    private val esHost: String,
    @Value("\${es.port}")
    private val esPort: Int
) {

    @Bean
    fun esClient(): SearchClient = SearchClient(
        KtorRestClient(
            host = esHost,
            https = false,
            port = esPort,
            client = ktorClientWithJavaEngine(2000)
        )
    )
}

fun ktorClientWithJavaEngine(
    timeoutMillis: Long
): HttpClient = HttpClient(Java) {
    engine {
        pipelining = true
    }

    install(HttpTimeout) {
        requestTimeoutMillis = timeoutMillis
    }

//    로컬 환경에서만 enable해서 사용
//    install(Logging) {
//        logger = Logger.DEFAULT
//        level = LogLevel.ALL
//    }
}
