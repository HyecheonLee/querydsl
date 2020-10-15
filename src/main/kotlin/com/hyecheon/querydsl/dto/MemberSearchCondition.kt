package com.hyecheon.querydsl.dto

data class MemberSearchCondition(
		val username: String?,
		val teamName: String?,
		val ageGoe: Int?,
		val ageLoe: Int?
)