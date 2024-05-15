package com.restaurant.be.review

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.exception.DuplicateLikeException
import com.restaurant.be.common.exception.NotFoundReviewException
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.review.domain.entity.Review
import com.restaurant.be.review.presentation.dto.CreateReviewResponse
import com.restaurant.be.review.presentation.dto.GetOneReviewResponse
import com.restaurant.be.review.presentation.dto.LikeReviewRequest
import com.restaurant.be.review.presentation.dto.LikeReviewResponse
import com.restaurant.be.review.presentation.dto.UpdateReviewResponse
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
    @Autowired private val reviewRepository: ReviewRepository
) : CustomDescribeSpec() {
    private val mockRestaurantID = "1"
    private val resource = "reviews"

    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
    @Transactional
    @Test
    fun `사진 없는 리뷰 작성(RequestDto에서 Image List만 비어있을 경우)시 성공한다`() {
        signUpUserService.signUpUser(
            SignUpUserRequest(
                email = "test@gmail.com",
                password = "a12345678",
                nickname = "testname"
            )
        )
        val reviewRequest = ReviewRequestDto(
            rating = 4.0,
            comment = "맛있어요",
            imageUrls = listOf()
        )
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/restaurants/{restaurantID}/$resource", mockRestaurantID)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reviewRequest))
        ).andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS")).andReturn()

        val actualResult: CommonResponse<CreateReviewResponse> =
            objectMapper.readValue(
                result.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
                object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
            )

        actualResult.data!!.review.comment shouldBe "맛있어요"
        actualResult.data!!.review.isLike shouldBe false
        actualResult.data!!.review.imageUrls.size shouldBe 0

        val getResult = mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/v1/restaurants/reviews/{reviewId}",
                actualResult.data!!.review.id
            )
        ).andExpect(status().isOk())
            .andReturn()

        val reviewResult: CommonResponse<GetOneReviewResponse> =
            objectMapper.readValue(
                getResult.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
                object : TypeReference<CommonResponse<GetOneReviewResponse>>() {}
            )
        reviewResult.data!!.review.comment shouldBe "맛있어요"
    }

    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
    @Transactional
    @Test
    fun `리뷰 수정 성공`() {
        signUpUserService.signUpUser(
            SignUpUserRequest(
                email = "test@gmail.com",
                password = "a12345678",
                nickname = "testname"
            )
        )
        val reviewRequest = ReviewRequestDto(
            rating = 4.0,
            comment = "맛있어요",
            imageUrls = listOf("image1", "image2", "image3")
        )

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/restaurants/{restaurantID}/$resource", mockRestaurantID)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reviewRequest))
        ).andReturn()

        val createResult: CommonResponse<CreateReviewResponse> =
            objectMapper.readValue(
                result.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
                object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
            )

        val restaurantId = createResult.data!!.review.restaurantId
        val reviewId = createResult.data!!.review.id

        val reviewUpdateRequest = ReviewRequestDto(
            rating = 1.0,
            comment = "수정했어요",
            imageUrls = listOf("update1", "update2")
        )

        val updateResult = mockMvc.perform(
            MockMvcRequestBuilders.patch(
                "/api/v1/restaurants/reviews/{restaurantId}/reviews/{reviewId}",
                restaurantId,
                reviewId
            ).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reviewUpdateRequest))
        ).andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS")).andReturn()

        val actualResult: CommonResponse<UpdateReviewResponse> =
            objectMapper.readValue(
                updateResult.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
                object : TypeReference<CommonResponse<UpdateReviewResponse>>() {}
            )

        actualResult.data!!.review.comment shouldBe reviewUpdateRequest.comment
        actualResult.data!!.review.imageUrls.size shouldBe reviewUpdateRequest.imageUrls.size
    }

    @WithMockUser(username = "test@gmail.com", roles = ["USER"], password = "a12345678")
    @Transactional
    @Test
    fun `리뷰 삭제 성공`() {
        signUpUserService.signUpUser(
            SignUpUserRequest(
                email = "test@gmail.com",
                password = "a12345678",
                nickname = "testname"
            )
        )
        val reviewRequest = ReviewRequestDto(
            rating = 4.0,
            comment = "맛있어요",
            imageUrls = listOf("image1", "image2", "image3")
        )

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/restaurants/{restaurantID}/$resource", mockRestaurantID)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reviewRequest))
        ).andReturn()

        val createResult: CommonResponse<CreateReviewResponse> =
            objectMapper.readValue(
                result.response.contentAsString.toByteArray(StandardCharsets.ISO_8859_1),
                object : TypeReference<CommonResponse<CreateReviewResponse>>() {}
            )

        val restaurantId = createResult.data!!.review.restaurantId
        val reviewId = createResult.data!!.review.id

        mockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/v1/restaurants/reviews/{restaurantId}/reviews/{reviewId}",
                restaurantId,
                reviewId
            )
        ).andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS"))

        mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/v1/restaurants/reviews/{reviewId}",
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
            comment = "",
            imageUrls = listOf()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/restaurants/{restaurantID}/$resource", mockRestaurantID)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reviewRequest))
        ).andExpect(status().isBadRequest)
    }

    @Nested
    open inner class ReviewListTests {
        @BeforeEach
        open fun setUp() {
            val user = User(
                id = 1L,
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
                    restaurantId = index.toLong(),
                    content = "맛있어요 $index",
                    rating = 5.0,
                    images = mutableListOf()
                )
            }

            reviews.forEach { reviewRepository.save(it) }
        }

        @Test
        @WithMockUser(username = "test@gmail.com", roles = ["USER"])
        @Transactional
        open fun `로그인한 유저가 리뷰 리스트 조회 성공`() {
            val reviewsSaved = reviewRepository.findAll()
            reviewsSaved.size shouldBe 20

            val result = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/restaurants/reviews").param("page", "0").param("size", "5")
            ).andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS")).andReturn()

            val mapper = jacksonObjectMapper()
            val jsonMap = mapper.readValue<Map<String, Any>>(result.response.contentAsString)

            val data = jsonMap["data"] as Map<String, Any>
            val reviews = data["reviews"] as List<Map<String, Any>>

            reviews.size shouldBe 5
            reviews.get(0)?.get("isLike") shouldBe false
        }

        @Test
        @WithMockUser(username = "newUser@gmail.com", roles = ["USER"])
        @Transactional
        open fun `새로운 유저가 자기 자신의 리뷰 목록을 조회`() {
            val newUser = User(
                email = "newUser@gmail.com",
                nickname = "maker1",
                password = "q1w2e3r41",
                withdrawal = false,
                roles = listOf(),
                profileImageUrl = "newuser-profile"
            )

            signUpUserRepository.save(newUser)

            val newReviews = (1..3).map { index ->
                Review(
                    user = newUser,
                    restaurantId = index.toLong(),
                    content = "정말 맛있어요 $index",
                    rating = 5.0,
                    images = mutableListOf()
                )
            }

            newReviews.forEach { reviewRepository.save(it) }

            val result = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/restaurants/my-reviews").param("page", "0").param("size", "5")
            ).andExpect(status().isOk).andExpect(jsonPath("$.result").value("SUCCESS")).andReturn()

            val mapper = jacksonObjectMapper()
            val jsonMap = mapper.readValue<Map<String, Any>>(result.response.contentAsString)

            val data = jsonMap["data"] as Map<String, Any>
            val reviews = data["reviews"] as List<Map<String, Any>>

            reviews.size shouldBe 3

            val reviewsSaved = reviewRepository.findAll()
            reviewsSaved.size shouldBe 23
        }

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
                MockMvcRequestBuilders.post("/api/v1/restaurants/reviews/{reviewId}/like", reviewId)
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
                MockMvcRequestBuilders.post("/api/v1/restaurants/reviews/{reviewId}/like", reviewId)
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(likeRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect { result -> result.resolvedException is DuplicateLikeException }

            val unLikeRequest = LikeReviewRequest(false)

            val unlikeResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/restaurants/reviews/{reviewId}/like", reviewId)
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
