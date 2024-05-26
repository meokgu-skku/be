package com.restaurant.be.review

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.exception.DuplicateLikeException
import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.common.util.RestaurantUtil
import com.restaurant.be.restaurant.repository.RestaurantRepository
import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.LikeReviewRequest
import com.restaurant.be.review.presentation.dto.LikeReviewResponse
import com.restaurant.be.review.presentation.dto.common.ReviewRequestDto
import com.restaurant.be.review.repository.ReviewRepository
import com.restaurant.be.user.domain.entity.QUser.user
import com.restaurant.be.user.domain.entity.User
import com.restaurant.be.user.domain.service.SignUpUserService
import com.restaurant.be.user.presentation.dto.SignUpUserRequest
import com.restaurant.be.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import javax.transaction.Transactional

@IntegrationTest
class ReviewIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val signUpUserService: SignUpUserService,
    @Autowired private val signUpUserRepository: UserRepository,
    @Autowired private val reviewRepository: ReviewRepository,
    @Autowired private val restaurantRepository: RestaurantRepository
) : CustomDescribeSpec() {
    private val mockRestaurantID = "1"
    private val resource = "reviews"

//    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
//    @Transactional
//    @Test
//    fun `사진 없는 리뷰 작성(RequestDto에서 Image List만 비어있을 경우)시 성공한다`() {
//        val restaurantEntity = RestaurantUtil.generateRestaurantEntity(name = "목구멍 율전점")
//        val savedRestaurant = restaurantRepository.save(restaurantEntity)
//
//        signUpUserService.signUpUser(
//            SignUpUserRequest(
//                email = "test@gmail.com",
//                password = "a12345678",
//                nickname = "testname"
//            )
//        )
//        val reviewRequest = ReviewRequestDto(
//            rating = 4.0,
//            content = "맛있어요",
//            imageUrls = listOf()
//        )
//        val result = mockMvc.perform(
//            MockMvcRequestBuilders.post("/v1/restaurants/{restaurantID}/$resource", savedRestaurant.id)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(reviewRequest))
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.result").value("SUCCESS"))
//            .andReturn()
//
//        val actualResult: CommonResponse<CreateReviewResponse> =
//            objectMapper.readValue(
//                result.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
//                object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
//            )
//
//        actualResult.data!!.review.content shouldBe "맛있어요"
//        actualResult.data!!.review.isLike shouldBe false
//        actualResult.data!!.review.imageUrls.size shouldBe 0
//
//        val reviewRequest2 = ReviewRequestDto(
//            rating = 1.0,
//            content = "맛없어요",
//            imageUrls = listOf()
//        )
//        mockMvc.perform(
//            MockMvcRequestBuilders.post("/v1/restaurants/{restaurantID}/$resource", savedRestaurant.id)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(reviewRequest2))
//        )
//        val getResult = mockMvc.perform(
//            MockMvcRequestBuilders.get(
//                "/v1/restaurants/reviews/{reviewId}",
//                actualResult.data!!.review.id
//            )
//        ).andExpect(status().isOk())
//            .andReturn()
//
//        val reviewResult: CommonResponse<GetReviewResponse> =
//            objectMapper.readValue(
//                getResult.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
//                object : TypeReference<CommonResponse<GetReviewResponse>>() {}
//            )
//        reviewResult.data!!.review.content shouldBe "맛있어요"
//        val restaurant = restaurantRepository.findById(reviewResult.data!!.review.restaurantId)
//        restaurant.get().reviewCount shouldBe 2
//        restaurant.get().ratingAvg shouldBe (4.0 + 1.0) / 2
//    }

//    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
//    @Transactional
//    @Test
//    fun `리뷰 수정 성공`() {
//        val restaurantEntity = RestaurantUtil.generateRestaurantEntity(name = "목구멍 율전점")
//        val savedRestaurant = restaurantRepository.save(restaurantEntity)
//        signUpUserService.signUpUser(
//            SignUpUserRequest(
//                email = "test@gmail.com",
//                password = "a12345678",
//                nickname = "testname"
//            )
//        )
//        val reviewRequest = ReviewRequestDto(
//            rating = 4.0,
//            content = "맛있어요",
//            imageUrls = listOf("image1", "image2", "image3")
//        )
//
//        val result = mockMvc.perform(
//            MockMvcRequestBuilders.post("/v1/restaurants/{restaurantID}/$resource", savedRestaurant.id)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(reviewRequest))
//        )
//            .andReturn()
//
//        val createResult: CommonResponse<CreateReviewResponse> =
//            objectMapper.readValue(
//                result.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
//                object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
//            )
//
//        val restaurantId = createResult.data?.review?.restaurantId
//        val reviewId = createResult.data?.review?.id
//
//        val reviewUpdateRequest = UpdateReviewRequest(
//            ReviewRequestDto(
//                rating = 1.0,
//                content = "수정했어요",
//                imageUrls = listOf("update1", "update2")
//            )
//        )
//
//        val updateResult = mockMvc.perform(
//            MockMvcRequestBuilders.patch("/v1/restaurants/reviews/{restaurantId}/reviews/{reviewId}", restaurantId, reviewId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(reviewUpdateRequest))
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.result").value("SUCCESS"))
//            .andReturn()
//
//        val actualResult: CommonResponse<UpdateReviewResponse> =
//            objectMapper.readValue(
//                updateResult.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
//                object : TypeReference<CommonResponse<UpdateReviewResponse>>() {}
//            )
//
//        actualResult.data!!.review.content shouldBe reviewUpdateRequest.review.content
//        actualResult.data!!.review.imageUrls.size shouldBe reviewUpdateRequest.review.imageUrls.size
//
//        val restaurant = restaurantRepository.findById(actualResult.data!!.review.restaurantId)
//        restaurant.get().ratingAvg shouldBe 1.0
//    }

    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
    @Transactional
    @Test
    fun `리뷰 삭제 성공`() {
        val restaurantEntity = RestaurantUtil.generateRestaurantEntity(name = "목구멍 율전점")
        val savedRestaurant = restaurantRepository.save(restaurantEntity)
        signUpUserService.signUpUser(
            SignUpUserRequest(
                email = "test@gmail.com",
                password = "a12345678",
                nickname = "testname"
            )
        )
        val reviewRequest = ReviewRequestDto(
            rating = 4.0,
            content = "맛있어요",
            imageUrls = listOf("image1", "image2", "image3")
        )

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/restaurants/{restaurantID}/$resource", savedRestaurant.id)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reviewRequest))
        ).andReturn()

        val createResult: CommonResponse<CreateReviewResponse> =
            objectMapper.readValue(
                result.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
                object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
            )

        val reviewId = createResult.data!!.review.id

        mockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/v1/restaurants/reviews/{reviewId}",
                reviewId
            )
        ).andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS"))
        val restaurant = restaurantRepository.findById(savedRestaurant.id)
        restaurant.get().reviewCount shouldBe 0
        restaurant.get().ratingAvg shouldBe 0

        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/v1/restaurants/reviews/{reviewId}",
                reviewId
            )
        ).andExpect(status().isBadRequest())
            .andExpect { result -> result.resolvedException is NotFoundReviewException }
    }

    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
    @Transactional
    @Test
    fun `comment가 없으면 오류 반환`() {
        val reviewRequest = ReviewRequestDto(
            rating = 3.0,
            content = "",
            imageUrls = listOf()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/restaurants/{restaurantID}/$resource", mockRestaurantID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Nested
    open inner class ReviewListTests {
        @BeforeEach
        open fun setUp() {
            val user = User(
                email = "test@gmail.com",
                nickname = "maker",
                password = "q1w2e3r4",
                withdrawal = false,
                roles = listOf(),
                profileImageUrl = "maker-profile"
            )

            val savedUser = signUpUserRepository.save(user)

            val reviews = (1..20).map { index ->
                Review(
                    user = savedUser,
                    restaurantId = 1,
                    content = "맛있어요 $index",
                    rating = 5.0,
                    images = mutableListOf()
                )
            }

            reviews.forEach { reviewRepository.save(it) }
        }

//        @Test
//        @WithMockUser(username = "test@gmail.com", roles = ["USER"])
//        @Transactional
//        open fun `로그인한 유저가 리뷰 리스트 조회 성공`() {
//            val reviewsSaved = reviewRepository.findAll()
//            reviewsSaved.size shouldBe 20
//
//            val reviewRequest = ReviewRequestDto(
//                rating = 4.0,
//                content = "맛있어요",
//                imageUrls = listOf()
//            )
//            mockMvc.perform(
//                MockMvcRequestBuilders.post("/v1/restaurants/{restaurantID}/$resource", 2)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(reviewRequest))
//            )
//
//            val result = mockMvc.perform(
//                MockMvcRequestBuilders.get("/v1/restaurants/{restaurantId}/reviews", mockRestaurantID)
//                    .param("page", "0")
//                    .param("size", "5")
//                    .param("sort", "createdAt,DESC")
//            )
//                .andExpect(status().isOk)
//                .andExpect(jsonPath("$.result").value("SUCCESS"))
//                .andReturn()
//
//            val mapper = jacksonObjectMapper()
//            val jsonMap = mapper.readValue<Map<String, Any>>(result.response.contentAsString)
//
//            val data = jsonMap["data"] as Map<String, Any>
//            val reviews = data["reviews"] as List<Map<String, Any>>
//
//            reviews.size shouldBe 5
//
//            reviews.get(0)?.get("isLike") shouldBe false
//            reviews.get(0)?.get("viewCount") shouldBe 0
//
//            val result2 = mockMvc.perform(
//                MockMvcRequestBuilders.get("/v1/restaurants/{restaurantId}/reviews", mockRestaurantID)
//                    .param("page", "4")
//                    .param("size", "5")
//                    .param("sort", "createdAt,DESC")
//            )
//                .andExpect(status().isOk)
//                .andExpect(jsonPath("$.result").value("SUCCESS"))
//                .andReturn()
//            val jsonMap2 = mapper.readValue<Map<String, Any>>(result2.response.contentAsString)
//            val data2 = jsonMap2["data"] as Map<String, Any>
//            val reviews2 = data2["reviews"] as List<Map<String, Any>>
//            val pagination2 = data2["pageable"] as Map<String, Any>
//
//            reviews2.size shouldBe 0
//            pagination2.get("pageNumber") shouldBe 4
//        }

//        @Test
//        @WithMockUser(username = "test@gmail.com", roles = ["USER"])
//        @Transactional
//        open fun `조회수 DESC 리스트 조회에 성공 (자신이 작성한 리뷰에 접근시 조회수가 반영되지 않는다`() {
//            val getReviews = reviewRepository.findAll()
//            val firstReviewId = getReviews.get(1).id
//            val secondReviewId = getReviews.get(2).id
//
//            for (callCount in 1..3) {
//                mockMvc.perform(
//                    MockMvcRequestBuilders.get(
//                        "/v1/restaurants/reviews/{reviewId}",
//                        firstReviewId
//                    )
//                ).andExpect(status().isOk())
//                if (callCount != 3) {
//                    mockMvc.perform(
//                        MockMvcRequestBuilders.get(
//                            "/v1/restaurants/reviews/{reviewId}",
//                            secondReviewId
//                        )
//                    ).andExpect(status().isOk())
//                }
//            }
//
//            val result = mockMvc.perform(
//                MockMvcRequestBuilders.get("/v1/restaurants/{restaurantId}/reviews", mockRestaurantID)
//                    .param("page", "0")
//                    .param("size", "5")
//                    .param("sort", "viewCount,DESC")
//            )
//                .andExpect(status().isOk)
//                .andExpect(jsonPath("$.result").value("SUCCESS"))
//                .andReturn()
//
//            val mapper = jacksonObjectMapper()
//            val jsonMap = mapper.readValue<Map<String, Any>>(result.response.contentAsString)
//
//            val data = jsonMap["data"] as Map<String, Any>
//            val reviews = data["reviews"] as List<Map<String, Any>>
//
//            reviews.size shouldBe 5
//            reviews.get(0)?.get("viewCount") shouldBe 0
//        }

//        @Test
//        @WithMockUser(username = "test@gmail.com", roles = ["USER"])
//        @Transactional
//        open fun `리뷰 리스트 조회는 특정 식당 ID만 가능하다`() {
//            val newRestaurantId = 10
//            val reviewRequest = ReviewRequestDto(
//                rating = 4.0,
//                content = "맛있어요",
//                imageUrls = listOf()
//            )
//            mockMvc.perform(
//                MockMvcRequestBuilders.post("/v1/restaurants/{restaurantID}/$resource", newRestaurantId)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(reviewRequest))
//            )
//            val result = mockMvc.perform(
//                MockMvcRequestBuilders.get("/v1/restaurants/{restaurantId}/reviews", newRestaurantId)
//                    .param("page", "0")
//                    .param("size", "5")
//                    .param("sort", "viewCount,DESC")
//            )
//                .andExpect(status().isOk)
//                .andExpect(jsonPath("$.result").value("SUCCESS"))
//                .andReturn()
//
//            val mapper = jacksonObjectMapper()
//            val jsonMap = mapper.readValue<Map<String, Any>>(result.response.contentAsString)
//
//            val data = jsonMap["data"] as Map<String, Any>
//            val reviews = data["reviews"] as List<Map<String, Any>>
//
//            reviews.size shouldBe 1
//            reviews.get(0)?.get("viewCount") shouldBe 0
//        }

//        @Test
//        @WithMockUser(username = "another@gmail.com", roles = ["USER"])
//        @Transactional
//        open fun `조회수 DESC 리스트 조회에 성공 (자신이 작성하지 않은 리뷰에 접근시 조회수가 반영된다`() {
//            signUpUserService.signUpUser(
//                SignUpUserRequest(
//                    email = "another@gmail.com",
//                    password = "a12345678",
//                    nickname = "another"
//                )
//            )
//            val getReviews = reviewRepository.findAll()
//            val firstReviewId = getReviews.get(1).id
//            val secondReviewId = getReviews.get(2).id
//
//            for (callCount in 1..3) {
//                mockMvc.perform(
//                    MockMvcRequestBuilders.get(
//                        "/v1/restaurants/reviews/{reviewId}",
//                        firstReviewId
//                    )
//                ).andExpect(status().isOk())
//                if (callCount != 3) {
//                    mockMvc.perform(
//                        MockMvcRequestBuilders.get(
//                            "/v1/restaurants/reviews/{reviewId}",
//                            secondReviewId
//                        )
//                    ).andExpect(status().isOk())
//                }
//            }
//
//            val result = mockMvc.perform(
//                MockMvcRequestBuilders.get("/v1/restaurants/{restaurantId}/reviews", mockRestaurantID)
//                    .param("page", "0")
//                    .param("size", "5")
//                    .param("sort", "viewCount,DESC")
//            )
//                .andExpect(status().isOk)
//                .andExpect(jsonPath("$.result").value("SUCCESS"))
//                .andReturn()
//
//            val mapper = jacksonObjectMapper()
//            val jsonMap = mapper.readValue<Map<String, Any>>(result.response.contentAsString)
//
//            val data = jsonMap["data"] as Map<String, Any>
//            val reviews = data["reviews"] as List<Map<String, Any>>
//
//            reviews.size shouldBe 5
//            reviews.get(0)?.get("viewCount") shouldBe 3
//            reviews.get(1)?.get("viewCount") shouldBe 2
//            reviews.get(2)?.get("viewCount") shouldBe 0
//        }

//        @Test
//        @WithMockUser(username = "another@gmail.com", roles = ["USER"])
//        @Transactional
//        open fun `좋아요 DESC 리스트 조회에 성공`() {
//            signUpUserService.signUpUser(
//                SignUpUserRequest(
//                    email = "another@gmail.com",
//                    password = "a12345678",
//                    nickname = "another"
//                )
//            )
//            val getReviews = reviewRepository.findAll()
//            val firstReviewId = getReviews.get(1).id
//            val reviewRestaurantId = getReviews.get(1).restaurantId
//
//            val likeRequest = LikeReviewRequest(true)
//
//            mockMvc.perform(
//                MockMvcRequestBuilders.post("/v1/restaurants/reviews/{reviewId}/like", firstReviewId)
//                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(likeRequest))
//            )
//
//            val result = mockMvc.perform(
//                MockMvcRequestBuilders.get("/v1/restaurants/{restaurantId}/reviews", reviewRestaurantId)
//                    .param("page", "0")
//                    .param("size", "5")
//                    .param("sort", "likeCount,DESC")
//            )
//                .andExpect(status().isOk)
//                .andExpect(jsonPath("$.result").value("SUCCESS"))
//                .andReturn()
//
//            val mapper = jacksonObjectMapper()
//            val jsonMap = mapper.readValue<Map<String, Any>>(result.response.contentAsString)
//
//            val data = jsonMap["data"] as Map<String, Any>
//            val reviews = data["reviews"] as List<Map<String, Any>>
//            val pagination = data["pageable"] as Map<String, Any>
//
//            reviews.size shouldBe 5
//            reviews.get(0)?.get("likeCount") shouldBe 1
//            reviews.get(1)?.get("likeCount") shouldBe 0
//            pagination.get("pageNumber") shouldBe 0
//        }

//        @Test
//        @WithMockUser(username = "newUser@gmail.com", roles = ["USER"])
//        @Transactional
//        open fun `새로운 유저가 자기 자신의 리뷰 목록을 조회`() {
//            val newUser = User(
//                email = "newUser@gmail.com",
//                nickname = "maker1",
//                password = "q1w2e3r41",
//                withdrawal = false,
//                roles = listOf(),
//                profileImageUrl = "newuser-profile"
//            )
//
//            signUpUserRepository.save(newUser)
//
//            val newReviews = (1..3).map { index ->
//                Review(
//                    user = newUser,
//                    restaurantId = index.toLong(),
//                    content = "정말 맛있어요 $index",
//                    rating = 5.0,
//                    images = mutableListOf()
//                )
//            }
//
//            newReviews.forEach { reviewRepository.save(it) }
//
//            mockMvc.perform(
//                MockMvcRequestBuilders.get(
//                    "/v1/restaurants/reviews/{reviewId}",
//                    newReviews.get(0).id
//                )
//            ).andExpect(status().isOk())
//
//            val result = mockMvc.perform(
//                MockMvcRequestBuilders.get("/v1/restaurants/my-reviews")
//                    .param("page", "0")
//                    .param("size", "5")
//                    .param("sort", "viewCount,DESC")
//            ).andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS")).andReturn()
//
//            val mapper = jacksonObjectMapper()
//            val jsonMap = mapper.readValue<Map<String, Any>>(result.response.contentAsString)
//
//            val data = jsonMap["data"] as Map<String, Any>
//            val reviews = data["reviews"] as List<Map<String, Any>>
//
//            reviews.size shouldBe 3
//
//            val reviewsSaved = reviewRepository.findAll()
//            reviewsSaved.size shouldBe 23
//        }

        @Test
        @WithMockUser(username = "newUser@gmail.com", roles = ["USER"])
        @Transactional
        open fun `새로운 유저가 특정 리뷰 좋아요 확인`() {
            val newUser = User(
                email = "newUser@gmail.com",
                nickname = "maker1",
                password = "q1w2e3r41",
                withdrawal = false,
                roles = listOf(),
                profileImageUrl = "newuser-profile"
            )

            signUpUserRepository.save(newUser)

            val likeRequest = LikeReviewRequest(true)

            val reviewId = reviewRepository.findAll().get(0).id

            val result = mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/restaurants/reviews/{reviewId}/like", reviewId)
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(likeRequest))
            )
                .andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS")).andReturn()

            val actualResult: CommonResponse<LikeReviewResponse> =
                objectMapper.readValue(
                    result.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
                    object : TypeReference<CommonResponse<LikeReviewResponse>>() {}
                )

            actualResult.data!!.review.isLike shouldBe true

            mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/restaurants/reviews/{reviewId}/like", reviewId)
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(likeRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect { result -> result.resolvedException is DuplicateLikeException }

            val unLikeRequest = LikeReviewRequest(false)

            val unlikeResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/v1/restaurants/reviews/{reviewId}/like", reviewId)
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(unLikeRequest))
            )
                .andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS")).andReturn()

            val unLikeActualResult: CommonResponse<LikeReviewResponse> =
                objectMapper.readValue(
                    unlikeResult.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
                    object : TypeReference<CommonResponse<LikeReviewResponse>>() {}
                )
            unLikeActualResult.data!!.review.isLike shouldBe false
        }
    }
}
