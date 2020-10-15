package com.hyecheon.querydsl.repository

import com.hyecheon.querydsl.entity.*
import org.springframework.data.jpa.repository.*

interface MemberRepository : JpaRepository<Member, Long> {
	fun findByUsername(username: String): List<Member>
}