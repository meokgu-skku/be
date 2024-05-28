package com.restaurant.be.user.presentation.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.PageDeserializer
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.user.presentation.dto.GetUserResponse
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class GetUserControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/users"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
        }

        describe("#getUser basic test") {
            it("when existed user should return user info") {
                // given
                val user = userRepository.findByEmail("test@gmail.com") ?: throw Exception("User not found")

                // when
                val result = mockMvc.perform(
                    get("$baseUrl/${user.id}")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetUserResponse>>() {}
                val actualResult: CommonResponse<GetUserResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.userDto.id shouldBe user.id
                actualResult.data!!.userDto.email shouldBe user.email
                actualResult.data!!.userDto.nickname shouldBe user.nickname
                actualResult.data!!.userDto.profileImageUrl shouldBe user.profileImageUrl
            }

            it("when not existed user should fail") {
                // given
                // when
                val result = mockMvc.perform(
                    get("$baseUrl/12345")
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.result").value("FAIL"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetUserResponse>>() {}
                val actualResult: CommonResponse<GetUserResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.message shouldBe "존재 하지 않는 유저 입니다."
            }
        }
    }
}
