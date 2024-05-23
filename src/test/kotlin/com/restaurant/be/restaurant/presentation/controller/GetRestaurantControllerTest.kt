package com.restaurant.be.restaurant.presentation.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.PageDeserializer
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.RestaurantDocument
import com.restaurant.be.common.util.RestaurantUtil
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantsResponse
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.Charset
import javax.transaction.Transactional

@IntegrationTest
@Transactional
class GetRestaurantControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val elasticsearchTemplate: ElasticsearchRestTemplate,
    private val restaurantRepository: RestaurantRepository
) : CustomDescribeSpec() {
    private val restaurantUrl = "/v1/restaurants"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
    }

    init {
        beforeEach {
            val indexOperations = elasticsearchTemplate.indexOps(RestaurantDocument::class.java)
            if (indexOperations.exists()) {
                indexOperations.delete()
            }
            indexOperations.create()
            indexOperations.putMapping(indexOperations.createMapping())
        }

        afterEach {
            val indexOperations = elasticsearchTemplate.indexOps(RestaurantDocument::class.java)
            if (indexOperations.exists()) {
                indexOperations.delete()
            }
        }

        describe("#get restaurant when no saved") {
            it("should return empty") {
                setUpUser("test@gmail.com", userRepository)
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

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType = object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                actualResult.data!!.restaurants.content.size shouldBe 0
            }
        }

        describe("#get restaurant when saved") {
            it("should return restaurant") {
                // given
                setUpUser("test@gmail.com", userRepository)
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(name = "목구멍 율전점")
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점"
                )
                elasticsearchTemplate.save(restaurantDocument)

                // when
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

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType = object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }
        }
    }
}
