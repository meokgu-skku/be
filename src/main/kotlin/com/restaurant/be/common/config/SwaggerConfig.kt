package com.restaurant.be.common.config

import com.fasterxml.classmate.TypeResolver
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpMethod
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.ResponseBuilder
import springfox.documentation.schema.AlternateTypeRule
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.service.SecurityScheme
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.security.Principal

@Configuration
@EnableSwagger2
class SwaggerConfig(
    private val typeResolver: TypeResolver
) {
    @Bean
    fun api(): Docket {
        val commonResponse = setCommonResponse()
        return Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false)
            .globalResponses(HttpMethod.GET, commonResponse)
            .globalResponses(HttpMethod.POST, commonResponse)
            .globalResponses(HttpMethod.PUT, commonResponse)
            .globalResponses(HttpMethod.PATCH, commonResponse)
            .globalResponses(HttpMethod.DELETE, commonResponse)
            .alternateTypeRules(
                AlternateTypeRule(
                    typeResolver.resolve(Pageable::class.java),
                    typeResolver.resolve(PageModel::class.java)
                )
            )
            .consumes(getConsumeContentTypes()).produces(getProduceContentTypes())
            .apiInfo(apiInfo()).select()
            .apis(RequestHandlerSelectors.basePackage("com.restaurant.be"))
            .paths(PathSelectors.ant("/**"))
            .build()
            .pathMapping("/")
            .ignoredParameterTypes(Principal::class.java)
            .securityContexts(listOf(securityContext()))
            .securitySchemes(listOf<SecurityScheme>(apiKey()))
    }

    private fun setCommonResponse() =
        listOf(
            ResponseBuilder().code("200").description("정상 처리(성공)").build(),
            ResponseBuilder().code("401").description("토큰 만료 또는 비정상 토큰 또는 권한 없음").build(),
            ResponseBuilder().code("404").description("존재하지 않는 api 요청 ").build(),
            ResponseBuilder().code("500").description("내부 서버 오류(문의 필요)").build()
        )

    private fun apiKey(): ApiKey {
        return ApiKey("JWT", "Authorization", "header")
    }

    private fun securityContext(): SecurityContext {
        return SecurityContext.builder().securityReferences(defaultAuth())
            .forPaths(PathSelectors.any()).build()
    }

    private fun defaultAuth(): List<SecurityReference> {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return listOf(SecurityReference("JWT", authorizationScopes))
    }

    private fun getConsumeContentTypes(): Set<String> =
        setOf(
            "application/json;charset=UTF-8",
            "application/x-www-form-urlencoded"
        )

    private fun getProduceContentTypes(): Set<String> = setOf("application/json;charset=UTF-8")

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder().title("REST API Document.")
            .description("work in progress").termsOfServiceUrl("localhost").version("1.0").build()
    }

    @ApiModel
    class PageModel {
        @ApiModelProperty(value = "페이지 번호(0..N)", example = "0")
        val page: Int = 0

        @ApiModelProperty(value = "페이지 크기", allowableValues = "range[0, 100]", example = "0")
        val size: Int = 0

        @ApiModelProperty(value = "정렬(사용법: 컬럼명,ASC|DESC)")
        val sort: List<String> = listOf()
    }
}
