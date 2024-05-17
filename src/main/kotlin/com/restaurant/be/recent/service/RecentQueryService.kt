package com.restaurant.be.recent.service

import com.restaurant.be.common.exception.NotFoundUserEmailException
import com.restaurant.be.common.redis.RedisRepository
import com.restaurant.be.recent.presentation.dto.DeleteRecentQueriesRequest
import com.restaurant.be.recent.presentation.dto.RecentQueriesDto
import com.restaurant.be.recent.presentation.dto.RecentQueriesResponse
import com.restaurant.be.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class RecentQueryService(
    private val redisRepository: RedisRepository,
    private val userRepository: UserRepository
) {

    fun getRecentQueries(email: String): RecentQueriesResponse {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()

        val queries = redisRepository.getSearchQueries(user.id ?: 0)

        return RecentQueriesResponse(
            recentQueries = queries?.map { RecentQueriesDto(it) } ?: listOf()
        )
    }

    fun deleteRecentQueries(
        email: String,
        request: DeleteRecentQueriesRequest
    ): RecentQueriesResponse {
        val user = userRepository.findByEmail(email) ?: throw NotFoundUserEmailException()

        if (request.query == null) {
            redisRepository.deleteSearchQueries(user.id ?: 0)
        } else {
            redisRepository.deleteSpecificQuery(user.id ?: 0, request.query)
        }

        val queries = redisRepository.getSearchQueries(user.id ?: 0)

        return RecentQueriesResponse(
            recentQueries = queries?.map { RecentQueriesDto(it) } ?: listOf()
        )
    }
}
