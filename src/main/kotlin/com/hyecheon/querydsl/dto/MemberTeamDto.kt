package com.hyecheon.querydsl.dto

import com.querydsl.core.annotations.*

data class MemberTeamDto
@QueryProjection constructor(
		val memberId: Long?,
		val username: String?,
		val age: Int?,
		val teamId: Long?,
		val teamName: String?
)