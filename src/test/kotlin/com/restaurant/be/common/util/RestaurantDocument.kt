package com.restaurant.be.common.util

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import javax.persistence.Id

@Document(indexName = "restaurant")
data class RestaurantDocument(
    @Id
    @Field(type = FieldType.Long, name = "id")
    val id: Long,

    @Field(type = FieldType.Text, name = "name")
    val name: String,

    @Field(type = FieldType.Text, name = "original_category")
    val originalCategory: String,

    @Field(type = FieldType.Text, name = "address")
    val address: String,

    @Field(type = FieldType.Long, name = "naver_review_count")
    val naverReviewCount: Long,

    @Field(type = FieldType.Double, name = "naver_rating_avg")
    val naverRatingAvg: Double,

    @Field(type = FieldType.Long, name = "review_count")
    val reviewCount: Long,

    @Field(type = FieldType.Double, name = "rating_avg")
    val ratingAvg: Double,

    @Field(type = FieldType.Long, name = "like_count")
    val likeCount: Long,

    @Field(type = FieldType.Text, name = "number")
    val number: String,

    @Field(type = FieldType.Text, name = "image_url")
    val imageUrl: String,

    @Field(type = FieldType.Text, name = "category")
    val category: String,

    @Field(type = FieldType.Text, name = "discount_content")
    val discountContent: String?,

    @Field(type = FieldType.Nested, name = "menus")
    val menus: List<MenuDocument>
)

data class MenuDocument(
    @Field(type = FieldType.Long, name = "restaurant_id")
    val restaurantId: Long,

    @Field(type = FieldType.Text, name = "menu_name")
    val menuName: String,

    @Field(type = FieldType.Integer, name = "price")
    val price: Int,

    @Field(type = FieldType.Text, name = "description")
    val description: String,

    @Field(type = FieldType.Text, name = "is_representative")
    val isRepresentative: String,

    @Field(type = FieldType.Text, name = "image_url")
    val imageUrl: String
)
