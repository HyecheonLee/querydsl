package com.hyecheon.querydsl.repository

import com.hyecheon.querydsl.entity.*
import org.springframework.data.jpa.repository.*
import org.springframework.data.querydsl.*

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
	fun findByUsername(username: String): List<Member>
}