package com.restaurant.be.common.redis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisTemplate

class RedisRepositoryTest : DescribeSpec({

    val redisTemplate = mockk<RedisTemplate<String, String>>()
    val listOperations = mockk<ListOperations<String, String>>()
    val redisRepository = RedisRepository(redisTemplate)

    describe("RedisRepository") {

        describe("getRecommendation") {
            it("should return empty list when no recommendations are found") {
                // Given
                val userId = 1L
                val key = "RECOMMENDATION:$userId"
                every { redisTemplate.opsForValue().get(key) } returns null
                every { redisTemplate.opsForValue().get("RECOMMENDATION:0") } returns null

                // When
                val recommendations = redisRepository.getRecommendation(userId)

                // Then
                recommendations shouldBe emptyList()
            }
        }

        describe("deleteSpecificQuery") {
            it("should print message when no instance of query is found in the list") {
                // Given
                val userId = 1L
                val queryToRemove = "testQuery"
                val key = "SR:$userId"
                every { redisTemplate.opsForList() } returns listOperations
                every { listOperations.remove(key, 0, queryToRemove) } returns 0

                // Redirect stdout to capture print statements
                val stdout = System.out
                val outContent = java.io.ByteArrayOutputStream()
                System.setOut(java.io.PrintStream(outContent))

                // When
                redisRepository.deleteSpecificQuery(userId, queryToRemove)

                // Then
                verify { listOperations.remove(key, 0, queryToRemove) }
                outContent.toString() shouldBe "No instance of '$queryToRemove' found in the list.\n"

                // Restore stdout
                System.setOut(stdout)
            }
        }
    }
})
