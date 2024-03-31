package com.restaurant.be.hello.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.restaurant.be.hello.domain.entity.Hello
import com.restaurant.be.hello.domain.entity.QHello

class HelloRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : HelloRepositoryCustom {
    override fun findHellos(): List<Hello> {
        return queryFactory
            .selectFrom(QHello.hello)
            .fetch()
    }
}
