package com.restaurant.be.restaurant.presentation.domain.entity

import com.restaurant.be.restaurant.presentation.dto.common.MenuDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "menus")
class Menu(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    var id: Long,

    @Column(name = "restaurant_id", nullable = false)
    var restaurantId: Long,

    @Column(name = "menu_name", nullable = false)
    var menuName: String,

    @Column(name = "price")
    var price: String,

    @Column(name = "description")
    var description: String,

    @Column(name = "is_representative")
    var isRepresentative: String,

    @Column(name = "image_url")
    var imageUrl: String

) {
    fun toDto(): MenuDto {
        return MenuDto(
            name = this.menuName,
            price = this.price.replace(",", "").toInt(),
            description = this.description,
            isRepresentative = (this.isRepresentative == "대표"),
            imageUrl = this.imageUrl
        )
    }
}
