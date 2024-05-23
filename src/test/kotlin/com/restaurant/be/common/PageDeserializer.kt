package com.restaurant.be.common

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class PageDeserializer<T>(private val clazz: Class<T>) : JsonDeserializer<Page<T>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Page<T> {
        val mapper = (p.codec as ObjectMapper)
        val node: JsonNode = mapper.readTree(p)
        val content: List<T> = mapper.convertValue(
            node.get("content"),
            mapper.typeFactory.constructCollectionType(List::class.java, clazz)
        )
        val pageable = PageRequest.of(
            node.get("pageable")?.get("pageNumber")?.asInt() ?: 0,
            node.get("pageable")?.get("pageSize")?.asInt() ?: 0
        )
        return PageImpl(content, pageable, node.get("totalElements")?.asLong() ?: 0L)
    }
}
