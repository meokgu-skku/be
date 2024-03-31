package com.restaurant.be.hello.presentation.dto

import com.restaurant.be.hello.domain.entity.Hello

data class HelloDto(
    val id: Long?,
    val name: String
)

data class HelloRequest(
    val name: String
) {
    fun toEntity() = Hello(name = name)
}

data class HelloResponse(
    val hellos: List<HelloDto>
) {
    constructor(hellos: List<Hello>, dummy: Boolean? = null) : this(
        hellos = hellos.map { HelloDto(it.id, it.name) }
    )
}
