package com.restaurant.be.restaurant.presentation.repository

import com.restaurant.be.restaurant.presentation.domain.entity.Restaurants
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantsRepository : JpaRepository<Restaurants, Int> {

    // 이름으로 레스토랑을 찾는 메소드
    fun findByName(name: String): List<Restaurants>?

    // 리뷰 수가 특정 값을 넘는 레스토랑을 찾는 메소드
    fun findByReviewCountGreaterThan(reviewCount: Int): List<Restaurants>

    fun findById(id: Long): Restaurants?

    fun findByCustomCategoryContaining(customCategory: String): List<Restaurants>?
}

// And: findByLastnameAndFirstname
// Or: findByLastnameOrFirstname
// Is, Equals: findByFirstname, findByFirstnameIs, findByFirstnameEquals
// Between: findByStartDateBetween
// LessThan, GreaterThan, LessThanEqual, GreaterThanEqual: findByAgeLessThan, findByAgeGreaterThan
// After, Before: findByStartDateAfter, findByStartDateBefore
// IsNull, IsNotNull, NotNull, Null: findByAge(Is)Null
// Like, NotLike: findByFirstnameLike
// StartingWith, EndingWith, Containing: findByFirstnameStartingWith
// OrderBy (asc, desc를 추가하여 정렬 순서 지정): findByAgeOrderByLastnameDesc
// Not: findByNameNot
// In, NotIn: findByAgeIn(Collection ages), findByAgeNotIn(Collection age)
// True, False: findByActiveTrue(), findByNameFalse()

// fun incrementLikeCountById(restaurantId: Long) {
//    val restaurant = findById(restaurantId)
//    if (restaurant != null) {
//        restaurant.plusLikeCount()
//        this.save(restaurant)
//    }
// }
//
// fun decrementLikeCountById(restaurantId: Long) {
//    val restaurant = findById(restaurantId)
//    if (restaurant != null) {
//        restaurant.minusLikeCount()
//        this.save(restaurant)
//    }
// }
//
// fun incrementReviewCountById(restaurantId: Long) {
//    val restaurant: Restaurants? = findById(restaurantId)
//    if (restaurant != null) {
//        restaurant.plusReviewCount()
//        this.save(restaurant)
//    }
// }
// fun decrementReviewCountById(restaurantId: Long) {
//    val restaurant: Restaurants? = findById(restaurantId)
//    if (restaurant != null) {
//        restaurant.minusReviewCount()
//        this.save(restaurant)
//
//    }
// }
