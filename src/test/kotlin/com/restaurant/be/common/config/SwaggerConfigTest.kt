package com.restaurant.be.common.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.swagger.annotations.ApiModelProperty

class SwaggerConfigTest : DescribeSpec({
    describe("PageModel") {

        it("should have default values set correctly") {
            // When
            val pageModel = SwaggerConfig.PageModel()

            // Then
            pageModel.page shouldBe 0
            pageModel.size shouldBe 0
            pageModel.sort shouldBe listOf<String>()
        }

        it("should have correct ApiModelProperty annotations") {
            // Given
            val pageField = SwaggerConfig.PageModel::class.java.getDeclaredField("page")
            val sizeField = SwaggerConfig.PageModel::class.java.getDeclaredField("size")
            val sortField = SwaggerConfig.PageModel::class.java.getDeclaredField("sort")

            // When
            val pageAnnotation = pageField.getAnnotation(ApiModelProperty::class.java)
            val sizeAnnotation = sizeField.getAnnotation(ApiModelProperty::class.java)
            val sortAnnotation = sortField.getAnnotation(ApiModelProperty::class.java)

            // Then
            pageAnnotation.value shouldBe "페이지 번호(0..N)"
            pageAnnotation.example shouldBe "0"

            sizeAnnotation.value shouldBe "페이지 크기"
            sizeAnnotation.allowableValues shouldBe "range[0, 100]"
            sizeAnnotation.example shouldBe "0"

            sortAnnotation.value shouldBe "정렬(사용법: 컬럼명,ASC|DESC)"
        }
    }
})
