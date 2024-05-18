package com.restaurant.be.restaurant.presentation.repository

interface CategoryRepository {

    fun findDistinctName(): List<String>
}
