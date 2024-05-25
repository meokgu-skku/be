package com.restaurant.be.user.domain.entity

import com.restaurant.be.common.converter.SeparatorConverter
import com.restaurant.be.common.password.PasswordService
import com.restaurant.be.user.presentation.dto.UpdateUserRequest
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var email: String = "",

    @Column(unique = true)
    var nickname: String = "",

    @Column
    var password: String = "",

    @Column(columnDefinition = "boolean default false")
    var withdrawal: Boolean = false,

    @Convert(converter = SeparatorConverter::class)
    var roles: List<String> = listOf(),

    @Column
    var profileImageUrl: String
) {
    fun updatePassword(password: String) {
        this.password = password.run(PasswordService::hashPassword)
    }

    fun updateUser(request: UpdateUserRequest) {
        this.nickname = request.nickname
        this.profileImageUrl = request.profileImageUrl
    }

    fun delete() {
        this.withdrawal = true
        this.email = ""
    }
}
