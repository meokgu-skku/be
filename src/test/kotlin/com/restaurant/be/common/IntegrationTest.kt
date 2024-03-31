package com.restaurant.be.common

import com.restaurant.be.BeApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
    classes = [BeApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@Testcontainers
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [IntegrationTestContextInitializer::class])
annotation class IntegrationTest
