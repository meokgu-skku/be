package com.restaurant.be.restaurant.repository

import com.jillesvangurp.ktsearch.SearchClient
import com.jillesvangurp.ktsearch.parseHits
import com.jillesvangurp.ktsearch.search
import com.jillesvangurp.searchdsls.querydsl.ESQuery
import com.jillesvangurp.searchdsls.querydsl.SearchDSL
import com.jillesvangurp.searchdsls.querydsl.SortOrder
import com.jillesvangurp.searchdsls.querydsl.bool
import com.jillesvangurp.searchdsls.querydsl.exists
import com.jillesvangurp.searchdsls.querydsl.match
import com.jillesvangurp.searchdsls.querydsl.nested
import com.jillesvangurp.searchdsls.querydsl.range
import com.jillesvangurp.searchdsls.querydsl.sort
import com.jillesvangurp.searchdsls.querydsl.terms
import com.restaurant.be.restaurant.presentation.controller.dto.GetRestaurantsRequest
import com.restaurant.be.restaurant.presentation.controller.dto.Sort
import com.restaurant.be.restaurant.repository.dto.RestaurantEsDocument
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class RestaurantEsRepository(
    private val client: SearchClient
) {

    private val searchIndex = "restaurant"

    fun searchRestaurants(
        request: GetRestaurantsRequest,
        pageable: Pageable,
        restaurantIds: List<Long>?,
        like: Boolean?
    ): Pair<List<RestaurantEsDocument>, List<Double>?> {
        val dsl = SearchDSL()
        val termQueries: MutableList<ESQuery> = mutableListOf()

        if (restaurantIds != null) {
            if (like == true) {
                termQueries.add(
                    dsl.terms("id", *restaurantIds.map { it.toString() }.toTypedArray())
                )
            } else {
                termQueries.add(
                    dsl.bool {
                        mustNot(
                            dsl.terms("id", *restaurantIds.map { it.toString() }.toTypedArray())
                        )
                    }
                )
            }
        }

        if (!request.categories.isNullOrEmpty()) {
            termQueries.add(
                dsl.terms("category", *request.categories.toTypedArray())
            )
        }
        if (request.discountForSkku == true) {
            termQueries.add(
                dsl.exists("discount_content")
            )
        } else if (request.discountForSkku == false) {
            termQueries.add(
                dsl.bool {
                    mustNot(
                        dsl.exists("discount_content")
                    )
                }
            )
        }
        if (request.ratingAvg != null) {
            termQueries.add(
                dsl.range("rating_avg") {
                    gte = request.ratingAvg
                }
            )
        }
        if (request.reviewCount != null) {
            termQueries.add(
                dsl.range("review_count") {
                    gte = request.reviewCount
                }
            )
        }
        if (request.naverRatingAvg != null) {
            termQueries.add(
                dsl.range("naver_rating_avg") {
                    gte = request.naverRatingAvg
                }
            )
        }
        if (request.naverReviewCount != null) {
            termQueries.add(
                dsl.range("naver_review_count") {
                    gte = request.naverReviewCount
                }
            )
        }
        if (request.priceMax != null) {
            termQueries.add(
                dsl.nested {
                    path = "menus"
                    query = dsl.bool {
                        filter(
                            dsl.range("menus.price") {
                                lte = request.priceMax
                            }
                        )
                    }
                }
            )
        }
        if (request.priceMin != null) {
            termQueries.add(
                dsl.nested {
                    path = "menus"
                    query = dsl.bool {
                        filter(
                            dsl.range("menus.price") {
                                gte = request.priceMin
                            }
                        )
                    }
                }
            )
        }

        val result = runBlocking {
            val res = client.search(
                target = searchIndex,
                block = {
                    if (request.cursor != null) {
                        searchAfter = request.cursor
                    }
                    query = bool {
                        filter(
                            termQueries
                        )
                        if (!request.query.isNullOrEmpty()) {
                            should(
                                match("name", request.query) {
                                    boost = 0.1
                                },
                                match("category", request.query) {
                                    boost = 0.03
                                },
                                match("original_category", request.query) {
                                    boost = 0.03
                                },
                                nested {
                                    path = "menus"
                                    query = bool {
                                        should(
                                            match("menus.menu_name", request.query) {
                                                boost = 0.01
                                            },
                                            match("menus.description", request.query) {
                                                boost = 0.01
                                            }
                                        )
                                    }
                                }
                            )
                            minimumShouldMatch(1)
                        }
                        sort {
                            when (request.customSort) {
                                Sort.BASIC -> add("_score", SortOrder.DESC)
                                Sort.CLOSELY_DESC -> add("_geo_distance", SortOrder.ASC) {
                                    this["location"] = mapOf(
                                        "lat" to request.latitude,
                                        "lon" to request.longitude
                                    )
                                    this["unit"] = "m"
                                    this["mode"] = "min"
                                    this["distance_type"] = "arc"
                                }
                                Sort.RATING_DESC -> add("rating_avg", SortOrder.DESC)
                                Sort.REVIEW_COUNT_DESC -> add("review_count", SortOrder.DESC)
                                Sort.LIKE_COUNT_DESC -> add("like_count", SortOrder.DESC)
                                Sort.ID_ASC -> null
                            }

                            add("id", SortOrder.ASC)
                        }
                    }
                },
                size = pageable.pageSize,
                from = if (request.cursor != null) null else pageable.offset.toInt(),
                trackTotalHits = true
            )

            val nextCursor: List<Double>? = res.hits?.hits?.lastOrNull()?.sort?.mapNotNull {
                    jsonElement ->
                jsonElement.jsonPrimitive.contentOrNull?.toDouble()
            }

            Pair(res.parseHits<RestaurantEsDocument>(), nextCursor)
        }

        return result
    }
}
