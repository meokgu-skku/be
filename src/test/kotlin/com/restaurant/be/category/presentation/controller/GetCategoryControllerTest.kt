package com.restaurant.be.category.presentation.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.restaurant.be.category.domain.entity.Category
import com.restaurant.be.category.presentation.controller.dto.GetCategoriesResponse
import com.restaurant.be.category.repository.CategoryRepository
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.PageDeserializer
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.setUpUser
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.Charset

@IntegrationTest
@Transactional
class GetCategoryControllerTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository
) : CustomDescribeSpec() {
    private val baseUrl = "/v1/restaurants/category"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
        }

        describe("#getCategories basic test") {
            it("when data saved should return categories") {
                // given
                categoryRepository.saveAll(
                    listOf(
                        Category(name = "한식"),
                        Category(name = "중식"),
                        Category(name = "일식"),
                        Category(name = "양식"),
                        Category(name = "분식")
                    )
                )

                // when
                val result = mockMvc.perform(
                    get(baseUrl)
                ).also {
                    println(it.andReturn().response.contentAsString)
                }
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetCategoriesResponse>>() {}
                val actualResult: CommonResponse<GetCategoriesResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.categories.size shouldBe 5
                actualResult.data!!.categories[0].name shouldBe "한식"
                actualResult.data!!.categories[1].name shouldBe "중식"
                actualResult.data!!.categories[2].name shouldBe "일식"
                actualResult.data!!.categories[3].name shouldBe "양식"
                actualResult.data!!.categories[4].name shouldBe "분식"
            }
        }
    }
}
