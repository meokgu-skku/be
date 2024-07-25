package com.restaurant.be.user.domain.entity

import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
class Certification {
    @Id
    var phoneNumber: Long? = null

    @Column(length = 6)
    var certificationNumber: String? = null

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now()

    var expiredAt: LocalDateTime = createdAt.plusMinutes(5)
}
