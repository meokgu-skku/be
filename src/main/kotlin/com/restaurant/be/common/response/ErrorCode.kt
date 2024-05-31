package com.restaurant.be.common.response

enum class ErrorCode(
    val errorMsg: String
) {
    // 500

    // 400
    COMMON_NULL_PARAMETER("빠뜨린 값이 없는지 확인 해주세요."),

    COMMON_INVALID_PARAMETER("요청한 값이 올바르지 않습니다.")

    ;
}
