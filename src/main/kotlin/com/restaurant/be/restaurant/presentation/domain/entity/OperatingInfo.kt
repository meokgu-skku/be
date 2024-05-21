package com.restaurant.be.restaurant.presentation.domain.entity

import com.restaurant.be.restaurant.presentation.dto.common.OperatingInfoDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "operating_infos")
class OperatingInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    var id: Long,

    @Column(name = "restaurant_id", nullable = false)
    var restaurantId: Long,

    @Column(name = "restaurant_name", nullable = false)
    var restaurantName: String,

    @Column(name = "day")
    var day: String,

    @Column(name = "info")
    var info: String

) {
    fun toDto(): OperatingInfoDto {
        val operatingTime = this.info.split(" _ ")
        return OperatingInfoDto(
            day = this.day,
            startTime = operatingTime.first(),
            endTime = operatingTime.last()
        )
    }
}
