package com.restaurant.be.hello.repository

import com.restaurant.be.hello.domain.entity.Hello
import org.springframework.data.jpa.repository.JpaRepository

interface HelloRepository : JpaRepository<Hello, Long>, HelloRepositoryCustom
