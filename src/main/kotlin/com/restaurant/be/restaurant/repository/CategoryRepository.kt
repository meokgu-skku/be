package com.restaurant.be.restaurant.repository

interface CategoryRepository {

    fun findDistinctName(): List<String>
}
