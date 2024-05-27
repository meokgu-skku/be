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
import com.restaurant.be.restaurant.domain.entity.Category
import com.restaurant.be.restaurant.domain.entity.RestaurantCategory
import com.restaurant.be.restaurant.domain.entity.RestaurantLike
import com.restaurant.be.restaurant.presentation.controller.dto.GetRestaurantResponse
import com.restaurant.be.restaurant.presentation.controller.dto.GetRestaurantsResponse
import com.restaurant.be.restaurant.presentation.controller.dto.common.RestaurantDto
import com.restaurant.be.restaurant.repository.CategoryRepository
import com.restaurant.be.restaurant.repository.RestaurantCategoryRepository
import com.restaurant.be.restaurant.repository.RestaurantLikeRepository
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
    private val restaurantCategoryRepository: RestaurantCategoryRepository,
    private val restaurantLikeRepository: RestaurantLikeRepository
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

        describe("#get restaurants basic test") {
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

        describe("#get restaurants simple filter test") {
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

            it("when like filter should return restaurant") {
                // given
                val newUser = userRepository.findByEmail("test@gmail.com")

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

                restaurantLikeRepository.save(
                    RestaurantLike(
                        userId = newUser?.id ?: 0,
                        restaurantId = restaurantEntity.id
                    )
                )

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("like", "true")
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

            it("when like filter should return restaurant") {
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
                        .param("query", "목구멍 율전점")
                        .param("like", "false")
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

            it("when like filter should return empty") {
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
                        .param("query", "목구멍 율전점")
                        .param("like", "true")
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

            it("when like filter should return empty") {
                // given
                val newUser = userRepository.findByEmail("test@gmail.com")
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

                restaurantLikeRepository.save(
                    RestaurantLike(
                        userId = newUser?.id ?: 0,
                        restaurantId = restaurantEntity.id
                    )
                )

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("query", "목구멍 율전점")
                        .param("like", "false")
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

            it("when no like filter should return restaurant") {
                // given
                val newUser = userRepository.findByEmail("test@gmail.com")
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

                restaurantLikeRepository.save(
                    RestaurantLike(
                        userId = newUser?.id ?: 0,
                        restaurantId = restaurantEntity.id
                    )
                )

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
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantsResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantsResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurants.content.size shouldBe 0
            }
        }

        describe("#get restaurants composite filter test") {
            it("when all filter should return restaurant") {
                // given
                val user = userRepository.findByEmail("test@gmail.com")
                val category = categoryRepository.save(Category(name = "한식"))
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    ratingAvg = 4.5,
                    reviewCount = 100,
                    discountContent = "성대생 할인 10%",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                restaurantCategoryRepository.save(
                    RestaurantCategory(
                        restaurantId = restaurantEntity.id,
                        categoryId = category.id ?: 0
                    )
                )
                restaurantLikeRepository.save(
                    RestaurantLike(
                        userId = user?.id ?: 0,
                        restaurantId = restaurantEntity.id
                    )
                )
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    category = "한식",
                    naverRatingAvg = 4.0,
                    naverReviewCount = 50,
                    ratingAvg = 4.5,
                    reviewCount = 100,
                    discountContent = "성대생 할인 10%",
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
                        .param("categories", "한식")
                        .param("discountContent", "true")
                        .param("like", "true")
                        .param("naverRatingAvg", "4.0")
                        .param("naverReviewCount", "50")
                        .param("ratingAvg", "4.5")
                        .param("reviewCount", "100")
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

            it("when all filter should return empty") {
                // given
                val user = userRepository.findByEmail("test@gmail.com")
                val category = categoryRepository.save(Category(name = "한식"))
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점",
                    ratingAvg = 4.5,
                    reviewCount = 100,
                    discountContent = "성대생 할인 10%",
                    menus = mutableListOf(
                        RestaurantUtil.generateMenuEntity(price = 10000),
                        RestaurantUtil.generateMenuEntity(price = 20000)
                    )
                )
                restaurantRepository.save(restaurantEntity)
                restaurantCategoryRepository.save(
                    RestaurantCategory(
                        restaurantId = restaurantEntity.id,
                        categoryId = category.id ?: 0
                    )
                )
                restaurantLikeRepository.save(
                    RestaurantLike(
                        userId = user?.id ?: 0,
                        restaurantId = restaurantEntity.id
                    )
                )
                val restaurantDocument = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity.id,
                    name = "목구멍 율전점",
                    category = "한식",
                    naverRatingAvg = 4.0,
                    naverReviewCount = 50,
                    ratingAvg = 4.4,
                    reviewCount = 100,
                    discountContent = "성대생 할인 10%",
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
                        .param("categories", "한식")
                        .param("discountContent", "true")
                        .param("like", "true")
                        .param("naverRatingAvg", "4.0")
                        .param("naverReviewCount", "50")
                        .param("ratingAvg", "4.5")
                        .param("reviewCount", "100")
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
                actualResult.data!!.restaurants.content.size shouldBe 0
            }
        }

        describe("#get restaurants pagination test") {
            it("when no data and set size 1 should return empty") {
                // given
                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("size", "1")
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

            it("when 1 data and set size 1 should return 1") {
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
                        .param("size", "1")
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

            it("when 2 data and set size 1 page 0 should return 1's restaurant") {
                // given
                val restaurantEntity1 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점1"
                )
                restaurantRepository.save(restaurantEntity1)
                val restaurantDocument1 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity1.id,
                    name = "목구멍 율전점1"
                )
                elasticsearchTemplate.save(restaurantDocument1)

                val restaurantEntity2 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점2"
                )
                restaurantRepository.save(restaurantEntity2)
                val restaurantDocument2 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity2.id,
                    name = "목구멍 율전점2"
                )
                elasticsearchTemplate.save(restaurantDocument2)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("size", "1")
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
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점1"
            }

            it("when 2 data and set size 1 page 1 should return 2's restaurant") {
                // given
                val restaurantEntity1 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점1"
                )
                restaurantRepository.save(restaurantEntity1)
                val restaurantDocument1 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity1.id,
                    name = "목구멍 율전점1"
                )
                elasticsearchTemplate.save(restaurantDocument1)

                val restaurantEntity2 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점2"
                )
                restaurantRepository.save(restaurantEntity2)
                val restaurantDocument2 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity2.id,
                    name = "목구멍 율전점2"
                )
                elasticsearchTemplate.save(restaurantDocument2)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("size", "1")
                        .param("page", "1")
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
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점2"
            }
        }

        describe("#get restaurants sort test") {
            it("when basic sort should return sorted restaurants by _score") {
                // given
                val restaurantEntity1 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점1"
                )
                restaurantRepository.save(restaurantEntity1)
                val restaurantDocument1 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity1.id,
                    name = "목구멍 율전점1"
                )
                elasticsearchTemplate.save(restaurantDocument1)

                val restaurantEntity2 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점2"
                )
                restaurantRepository.save(restaurantEntity2)
                val restaurantDocument2 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity2.id,
                    name = "목구멍 율전점2"
                )
                elasticsearchTemplate.save(restaurantDocument2)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("customSort", "BASIC")
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
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점1"
                actualResult.data!!.restaurants.content[1].name shouldBe "목구멍 율전점2"
            }

            it("when closely_desc sort should return sorted restaurants by closely_desc") {
                // given
                val restaurantEntity1 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점1",
                    longitude = 127.123457,
                    latitude = 37.123457
                )
                restaurantRepository.save(restaurantEntity1)
                val restaurantDocument1 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity1.id,
                    name = "목구멍 율전점1",
                    longitude = 127.123457,
                    latitude = 37.123457
                )
                elasticsearchTemplate.save(restaurantDocument1)

                val restaurantEntity2 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점2",
                    ratingAvg = 4.0,
                    longitude = 127.123456,
                    latitude = 37.123456
                )
                restaurantRepository.save(restaurantEntity2)
                val restaurantDocument2 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity2.id,
                    name = "목구멍 율전점2",
                    longitude = 127.123456,
                    latitude = 37.123456
                )
                elasticsearchTemplate.save(restaurantDocument2)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("customSort", "CLOSELY_DESC")
                        .param("longitude", "127.123456")
                        .param("latitude", "37.123456")
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
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점2"
                actualResult.data!!.restaurants.content[1].name shouldBe "목구멍 율전점1"
            }

            it("when rating_desc sort should return sorted restaurants by rating_desc") {
                // given
                val restaurantEntity1 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점1",
                    ratingAvg = 4.0
                )
                restaurantRepository.save(restaurantEntity1)
                val restaurantDocument1 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity1.id,
                    name = "목구멍 율전점1",
                    ratingAvg = 4.0
                )
                elasticsearchTemplate.save(restaurantDocument1)

                val restaurantEntity2 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점2",
                    ratingAvg = 4.5
                )
                restaurantRepository.save(restaurantEntity2)
                val restaurantDocument2 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity2.id,
                    name = "목구멍 율전점2",
                    ratingAvg = 4.5
                )
                elasticsearchTemplate.save(restaurantDocument2)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("customSort", "RATING_DESC")
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
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점2"
                actualResult.data!!.restaurants.content[1].name shouldBe "목구멍 율전점1"
            }

            it("when review_desc sort should return sorted restaurants by review_desc") {
                // given
                val restaurantEntity1 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점1",
                    reviewCount = 100
                )
                restaurantRepository.save(restaurantEntity1)
                val restaurantDocument1 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity1.id,
                    name = "목구멍 율전점1",
                    reviewCount = 100
                )
                elasticsearchTemplate.save(restaurantDocument1)

                val restaurantEntity2 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점2",
                    reviewCount = 200
                )
                restaurantRepository.save(restaurantEntity2)
                val restaurantDocument2 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity2.id,
                    name = "목구멍 율전점2",
                    reviewCount = 200
                )
                elasticsearchTemplate.save(restaurantDocument2)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("customSort", "REVIEW_COUNT_DESC")
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
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점2"
                actualResult.data!!.restaurants.content[1].name shouldBe "목구멍 율전점1"
            }

            it("when like_count_desc sort should return sorted restaurants by like_count_desc") {
                // given
                val restaurantEntity1 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점1"
                )
                restaurantRepository.save(restaurantEntity1)
                val restaurantDocument1 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity1.id,
                    name = "목구멍 율전점1"
                )
                elasticsearchTemplate.save(restaurantDocument1)

                val restaurantEntity2 = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점2",
                    likeCount = 1
                )
                restaurantRepository.save(restaurantEntity2)
                val restaurantDocument2 = RestaurantUtil.generateRestaurantDocument(
                    id = restaurantEntity2.id,
                    name = "목구멍 율전점2",
                    likeCount = 1
                )
                elasticsearchTemplate.save(restaurantDocument2)
                elasticsearchTemplate.indexOps(RestaurantDocument::class.java).refresh()

                val user = userRepository.findByEmail("test@gmail.com")
                restaurantLikeRepository.save(
                    RestaurantLike(
                        userId = user?.id ?: 0,
                        restaurantId = restaurantEntity2.id
                    )
                )

                // when
                val result = mockMvc.perform(
                    get(restaurantUrl)
                        .param("customSort", "LIKE_COUNT_DESC")
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
                actualResult.data!!.restaurants.content[0].name shouldBe "목구멍 율전점2"
                actualResult.data!!.restaurants.content[1].name shouldBe "목구멍 율전점1"
            }
        }

        describe("#get restaurant test") {
            it("when restaurant exist should return restaurant") {
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
                    get("$restaurantUrl/${restaurantEntity.id}")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurant.name shouldBe "목구멍 율전점"
            }

            it("when restaurant not exist should return empty") {
                // given
                // when
                val result = mockMvc.perform(
                    get("$restaurantUrl/1")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data shouldBe null
                actualResult.message shouldBe "해당 식당 정보가 존재하지 않습니다."
            }

            it("when liked restaurant should return liked true") {
                // given
                val user = userRepository.findByEmail("test@gmail.com")
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)
                restaurantLikeRepository.save(
                    RestaurantLike(
                        userId = user?.id ?: 0,
                        restaurantId = restaurantEntity.id
                    )
                )

                val result = mockMvc.perform(
                    get("$restaurantUrl/${restaurantEntity.id}")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurant.isLike shouldBe true
            }

            it("when not liked restaurant should return liked false") {
                // given
                val restaurantEntity = RestaurantUtil.generateRestaurantEntity(
                    name = "목구멍 율전점"
                )
                restaurantRepository.save(restaurantEntity)

                val result = mockMvc.perform(
                    get("$restaurantUrl/${restaurantEntity.id}")
                )
                    .also {
                        println(it.andReturn().response.contentAsString)
                    }
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val responseContent = result.response.getContentAsString(Charset.forName("UTF-8"))
                val responseType =
                    object : TypeReference<CommonResponse<GetRestaurantResponse>>() {}
                val actualResult: CommonResponse<GetRestaurantResponse> = objectMapper.readValue(
                    responseContent,
                    responseType
                )

                // then
                actualResult.data!!.restaurant.isLike shouldBe false
            }
        }
    }
}
