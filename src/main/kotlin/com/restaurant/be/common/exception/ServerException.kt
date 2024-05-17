package com.restaurant.be.common.exception

sealed class ServerException(
    val code: Int,
    override val message: String
) : RuntimeException(message)

data class InvalidTokenException(
    override val message: String = "유효하지 않은 토큰입니다."
) : ServerException(401, message)

data class InvalidPasswordException(
    override val message: String = "패스워드가 일치 하지 않습니다."
) : ServerException(401, message)

data class NotFoundUserEmailException(
    override val message: String = "존재 하지 않는 유저 이메일 입니다."
) : ServerException(400, message)

data class WithdrawalUserException(
    override val message: String = "탈퇴한 유저 입니다."
) : ServerException(400, message)

data class DuplicateUserEmailException(
    override val message: String = "이미 존재 하는 이메일 입니다."
) : ServerException(400, message)

data class DuplicateUserNicknameException(
    override val message: String = "이미 존재 하는 닉네임 입니다."
) : ServerException(400, message)

data class SendEmailException(
    override val message: String = "이메일 전송에 실패 했습니다."
) : ServerException(500, message)

data class SkkuEmailException(
    override val message: String = "성균관대 이메일이 아닙니다."
) : ServerException(400, message)

data class InvalidEmailCodeException(
    override val message: String = "인증 코드가 일치 하지 않습니다."
) : ServerException(400, message)

data class InvalidUserResetPasswordStateException(
    override val message: String = "유저가 비밀번호 초기화 상태가 아닙니다."
) : ServerException(400, message)

data class NotEqualTokenException(
    override val message: String = "토큰이 일치 하지 않습니다."
) : ServerException(400, message)

data class NotFoundUserException(
    override val message: String = "존재 하지 않는 유저 입니다."
) : ServerException(400, message)

data class NotFoundReviewException(
    override val message: String = "존재하지 않은 리뷰 입니다."
) : ServerException(400, message)

data class UnAuthorizedUpdateException(
    override val message: String = "해당 게시글을 수정할 권한이 없습니다."
) : ServerException(401, message)

data class UnAuthorizedDeleteException(
    override val message: String = "해당 게시글을 삭제할 권한이 없습니다."
) : ServerException(401, message)
