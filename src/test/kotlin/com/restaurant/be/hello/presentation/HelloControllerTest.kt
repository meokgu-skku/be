package com.restaurant.be.hello.presentation

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.restaurant.be.common.CustomDescribeSpec
import com.restaurant.be.common.IntegrationTest
import com.restaurant.be.common.response.CommonResponse
import com.restaurant.be.hello.presentation.dto.HelloRequest
import com.restaurant.be.hello.presentation.dto.HelloResponse
import io.kotest.matchers.shouldBe
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@IntegrationTest
class HelloControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) : CustomDescribeSpec() {

    private val helloUrl = "/hello"

    init {
        describe("#register hello") {
            it("should return hello") {
                val request = HelloRequest("hello")

                val result = mockMvc.perform(
                    post(helloUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val actualResult: CommonResponse<HelloResponse> = objectMapper.readValue(
                    result.response.contentAsString,
                    object : TypeReference<CommonResponse<HelloResponse>>() {}
                )

                actualResult.data!!.hellos.size shouldBe 1
                actualResult.data!!.hellos[0].name shouldBe "hello"
            }
        }

        describe("#retrieve hellos") {
            it("should return hellos") {
                val result = mockMvc.perform(
                    get(helloUrl)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andReturn()

                val actualResult: CommonResponse<HelloResponse> = objectMapper.readValue(
                    result.response.contentAsString,
                    object : TypeReference<CommonResponse<HelloResponse>>() {}
                )

                actualResult.data!!.hellos.size shouldBe 1
                actualResult.data!!.hellos[0].name shouldBe "hello"
            }
        }
    }
}
