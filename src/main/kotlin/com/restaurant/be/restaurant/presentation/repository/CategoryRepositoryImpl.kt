package com.restaurant.be.restaurant.presentation.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.restaurant.presentation.domain.entity.QCategory
import org.springframework.stereotype.Repository

@Repository
class CategoryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CategoryRepository {

    override fun findDistinctName(): List<String> {
        val qCategory = QCategory.category
        return queryFactory.select(qCategory.name).from(qCategory).distinct().fetch()
    }
}
