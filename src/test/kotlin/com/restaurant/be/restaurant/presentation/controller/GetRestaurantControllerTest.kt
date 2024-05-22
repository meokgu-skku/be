package com.restaurant.be.restaurant.presentation.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantsResponse
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import java.security.Principal
import javax.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@IntegrationTest
@Transactional
class GetRestaurantControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    @Autowired private val userRepository: UserRepository,
) : CustomDescribeSpec() {
    private val restaurantUrl = "/v1/restaurants"

    init {
        describe("#get restaurant") {
            it("should return restaurant") {
                setUpUser("test@gmail.com", userRepository)
                SecurityContextHolder.getContext().authentication =
                    PreAuthenticatedAuthenticationToken(
                        Principal { "test@gmail.com" },
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_USER"))
                    )
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    result.response.contentAsString,
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                )

                actualResult.data!!.restaurants.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "restaurant"
            }
        }

        describe("test") {
            it("should return restaurant") {
                SecurityContextHolder.getContext().authentication =
                    PreAuthenticatedAuthenticationToken(
                        Principal { "test@gmail.com" },
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_USER"))
                    )
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    result.response.contentAsString,
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                )

                actualResult.data!!.restaurants.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "restaurant"
            }
        }
    }
}
