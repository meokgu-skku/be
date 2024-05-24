package com.restaurant.be.restaurant.presentation.domain.service

import com.restaurant.be.restaurant.presentation.dto.GetCategoryResponse
import com.restaurant.be.restaurant.presentation.dto.common.CategoryDto
import com.restaurant.be.restaurant.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCategoryService(
    private val categoryRepository: CategoryRepository
) {
    @Transactional(readOnly = true)
    fun getCategories(): GetCategoryResponse {
        val categories = categoryRepository.findAll()

        return GetCategoryResponse(
            categories = categories.map { CategoryDto(it.id ?: 0, it.name) }
        )
    }
}
