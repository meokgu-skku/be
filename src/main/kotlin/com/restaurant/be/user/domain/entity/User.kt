package com.restaurant.be.user.domain.entity

import com.restaurant.be.common.converter.SeparatorConverter
import com.restaurant.be.common.password.PasswordService
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
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(unique = true)
    var email: String = "",

    @Column(unique = true)
    var nickname: String = "",

    var password: String = "",

    @Column(columnDefinition = "boolean default false")
    var withdrawal: Boolean = false,

    @Convert(converter = SeparatorConverter::class)
    var roles: List<String> = listOf()
) {
    fun updatePassword(password: String) {
        this.password = password.run(PasswordService::hashPassword)
    }
}
