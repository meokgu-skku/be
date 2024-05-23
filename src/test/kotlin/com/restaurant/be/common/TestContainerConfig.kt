package com.restaurant.be.common

import com.github.dockerjava.api.model.PortBinding
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object TestContainerConfig {

    val DATABASE_NAME: String = "skku"
    val USERNAME: String = "skku-user"
    val PASSWORD: String = "skku-pw"

    // image for linux/arm64/v8 m1 support
    val mysql = DockerImageName.parse("mysql/mysql-server:8.0.26")
        .asCompatibleSubstituteFor("mysql")
        .let { compatibleImageName -> MySQLContainer<Nothing>(compatibleImageName) }
        .apply {
            withDatabaseName(DATABASE_NAME)
            withUsername(USERNAME)
            withPassword(PASSWORD)
            withEnv("MYSQL_USER", USERNAME)
            withEnv("MYSQL_PASSWORD", PASSWORD)
            withEnv("MYSQL_ROOT_PASSWORD", PASSWORD)
            withCommand("--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci")
            withUrlParam("useTimezone", "true")
            withUrlParam("serverTimezone", "Asia/Seoul")
            withCreateContainerCmdModifier {
                it.withPortBindings(PortBinding.parse("33006:3306"))
            }
            start()
        }

    val redisContainer: GenericContainer<*> =
        GenericContainer<Nothing>(DockerImageName.parse("redis:5.0.3-alpine"))
            .apply {
                withCreateContainerCmdModifier {
                    it.withPortBindings(PortBinding.parse("6380:6379"))
                }
                start()
            }

    val elasticsearchContainer: GenericContainer<*> =
        GenericContainer<Nothing>(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.8.1"))
            .apply {
                withCreateContainerCmdModifier {
                    it.withPortBindings(PortBinding.parse("9201:9200"))
                }
                withEnv("discovery.type", "single-node")
                start()
            }

    init {
        // Set system properties to configure Redis and Elasticsearch hosts and ports
        System.setProperty("spring.redis.host", redisContainer.host)
        System.setProperty("spring.redis.port", redisContainer.getMappedPort(6379).toString())
        System.setProperty(
            "spring.elasticsearch.rest.uris",
            "http://${elasticsearchContainer.host}:${elasticsearchContainer.getMappedPort(9200)}"
        )

        // Initialize Elasticsearch
        waitForElasticsearch()
    }

    private fun waitForElasticsearch() {
        val url =
            URL("http://${elasticsearchContainer.host}:${elasticsearchContainer.getMappedPort(9200)}/_cluster/health")
        while (true) {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                if (connection.responseCode == 200) {
                    println("Elasticsearch is ready")
                    break
                } else {
                    println("Waiting for Elasticsearch to be ready")
                }
            } catch (e: Exception) {
                Thread.sleep(1000)
            }
        }
    }

    private fun insertSampleData() {
        val url =
            URL("http://${elasticsearchContainer.host}:${elasticsearchContainer.getMappedPort(9200)}/restaurant/_doc/1")
        val jsonPayload = """
            {
              "id": 1,
              "name": "목구멍 율전점",
              "original_category": "육류,고기요리",
              "address": "경기 수원시 장안구 화산로233번길 46 1층",
              "naver_review_count": 999,
              "number": "031-293-9294",
              "image_url": "https://search.pstatic.net/common/?autoRotate=true&type=w560_sharpen&src=https://ldb-phinf.pstatic.net/20230615_253/1686790793946ISiOc_JPEG/3%B9%F8.jpg",
              "category": "한식",
              "menus": [
                {
                  "restaurant_id" : 1,
                  "menu_name": "하이볼(레몬, 자몽, 얼그레이)",
                  "price": 7000,
                  "description": "시원 상큼한 하이볼3종",
                  "is_representative": true,
                  "image_url": "https://search.pstatic.net/common/?autoRotate=true&quality=95&type=f320_320&src=https://ldb-phinf.pstatic.net/20230525_34/16849976756701d50P_JPEG/Screenshot_20230525_095732_Samsung_Internet.jpg"
                }
              ],
              "review_count": 999,
              "rating_avg": 4.5,
              "like_count": 999
            }
        """.trimIndent()

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        OutputStreamWriter(connection.outputStream).use { it.write(jsonPayload) }

        if (connection.responseCode != 200) {
            println("Failed to insert data into Elasticsearch")
        } else {
            println("Data inserted into Elasticsearch")
        }
    }
}
