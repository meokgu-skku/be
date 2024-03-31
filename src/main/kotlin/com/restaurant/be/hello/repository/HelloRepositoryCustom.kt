package com.restaurant.be.hello.repository

import com.restaurant.be.hello.domain.entity.Hello

interface HelloRepositoryCustom {
    fun findHellos(): List<Hello>
}
