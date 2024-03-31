package com.restaurant.be.hello.domain.service

import com.restaurant.be.hello.presentation.dto.HelloRequest
import com.restaurant.be.hello.presentation.dto.HelloResponse
import com.restaurant.be.hello.repository.HelloRepository
import org.springframework.stereotype.Service

@Service
class HelloService(
    private val helloRepository: HelloRepository
) {

    fun saveHello(request: HelloRequest): HelloResponse {
        val hello = helloRepository.save(request.toEntity())
        return HelloResponse(listOf(hello))
    }

    fun findHellos(): HelloResponse {
        val hellos = helloRepository.findHellos()
        return HelloResponse(hellos)
    }
}
