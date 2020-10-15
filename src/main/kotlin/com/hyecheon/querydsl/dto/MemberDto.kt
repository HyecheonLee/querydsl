package com.hyecheon.querydsl.dto

import com.querydsl.core.annotations.*


data class MemberDto @QueryProjection constructor(
		val username: String? = null,
		val age: Int? = null
)