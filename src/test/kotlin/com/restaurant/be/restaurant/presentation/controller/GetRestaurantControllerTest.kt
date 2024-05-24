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
import com.restaurant.be.restaurant.presentation.domain.entity.Category
import com.restaurant.be.restaurant.presentation.domain.entity.RestaurantCategory
import com.restaurant.be.restaurant.presentation.dto.GetRestaurantsResponse
import com.restaurant.be.restaurant.presentation.dto.common.RestaurantDto
import com.restaurant.be.restaurant.repository.CategoryRepository
import com.restaurant.be.restaurant.repository.RestaurantCategoryRepository
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
    private val restaurantRepository: RestaurantRepository,
    private val categoryRepository: CategoryRepository,
    private val restaurantCategoryRepository: RestaurantCategoryRepository
) : CustomDescribeSpec() {
    private val restaurantUrl = "/v1/restaurants"
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).apply {
        val module = SimpleModule()
        module.addDeserializer(Page::class.java, PageDeserializer(RestaurantDto::class.java))
        this.registerModule(module)
    }

    init {
        beforeEach {
            setUpUser("test@gmail.com", userRepository)
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

        describe("#get restaurant basic test") {
            it("when no saved should return empty") {
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
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when saved should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(name = "목구멍 율전점")
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점"
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

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
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }
        }

        describe("#get restaurant simple filter test") {
            it("when category filter should return restaurant") {
                // given
                val category = categoryRepository.save(Category(name = "한식"))
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                restaurantCategoryRepository.save(
                    RestaurantCategory(
                        restaurantId = restaurantEntity.id,
                        categoryId = category.id ?: 0
                    )
                )
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    category = "한식"
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("categories", "한식")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when category filter should return empty") {
                // given
                val category = categoryRepository.save(Category(name = "한식"))
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                restaurantCategoryRepository.save(
                    RestaurantCategory(
                        restaurantId = restaurantEntity.id,
                        categoryId = category.id ?: 0
                    )
                )
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    category = "한식"
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("categories", "양식")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when category filter in default setting should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점"
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("categories", "한식")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when discount filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    discountContent = "성대생 할인 10%"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    discountContent = "성대생 할인 10%"
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("discountForSkku", "true")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when discount filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    discountContent = "성대생 할인 10%"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    discountContent = "성대생 할인 10%"
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("discountForSkku", "false")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when discount filter in default setting should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    discountContent = null
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("discountForSkku", "true")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when discount filter in default setting should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    discountContent = null
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("discountForSkku", "false")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when like filter should return restaurant") {}

            it("when like filter should return empty") {}

            it("when like filter in default setting should return empty") {}

            it("when naverRatingAvg filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    naverRatingAvg = 4.5
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("naverRatingAvg", "4.5")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when naverRatingAvg filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    naverRatingAvg = 4.5
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("naverRatingAvg", "5.0")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when naverReviewCount filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    naverReviewCount = 100
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("naverReviewCount", "100")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when naverReviewCount filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    naverReviewCount = 100
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("naverReviewCount", "200")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when priceMax filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    menus = listOf(
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 10000
                        ),
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 20000
                        )
                    )
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("priceMax", "10000")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when priceMax filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    menus = listOf(
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 10000
                        ),
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 20000
                        )
                    )
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("priceMax", "20000")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when priceMax filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    menus = listOf(
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 10000
                        ),
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 20000
                        )
                    )
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("priceMax", "5000")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when priceMin filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    menus = listOf(
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 10000
                        ),
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 20000
                        )
                    )
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("priceMin", "10000")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when priceMin filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    menus = listOf(
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 10000
                        ),
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 20000
                        )
                    )
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("priceMin", "20000")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when priceMin filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    menus = listOf(
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 10000
                        ),
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 20000
                        )
                    )
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("priceMin", "30000")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when priceMin and priceMax filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    menus = listOf(
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 10000
                        ),
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 20000
                        )
                    )
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("priceMin", "10000")
                        .param("priceMax", "20000")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when priceMin and priceMax filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    menus = listOf(
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 10000
                        ),
                        RestaurantUtil.generateMenuDocument(
                            restaurantId = restaurantEntity.id,
                            price = 20000
                        )
                    )
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("priceMin", "20001")
                        .param("priceMax", "30000")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when query filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점"
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when query filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점"
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "피자")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when ratingAvg filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    ratingAvg = 4.5
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    ratingAvg = 4.5
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("ratingAvg", "4.5")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 1
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점"
            }

            it("when ratingAvg filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    ratingAvg = 4.5
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    ratingAvg = 4.5
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("ratingAvg", "5.0")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }

            it("when reviewCount filter should return restaurant") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    reviewCount = 100
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    reviewCount = 100
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("reviewCount", "100")
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

            it("when reviewCount filter should return empty") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    reviewCount = 100
                )
                restaurantRepository.save(restaurantEntity)
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    reviewCount = 100
                )
                elasticsearchTemplate.save(restaurantDocument)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("reviewCount", "200")
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
                actualResult.data!!.restaurants.content.size shouldBe 0
            }
        }
    }
}
