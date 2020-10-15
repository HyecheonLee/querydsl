package com.hyecheon.querydsl.controller

import com.hyecheon.querydsl.dto.*
import com.hyecheon.querydsl.repository.*
import org.springframework.web.bind.annotation.*

@RestController
class MemberController(
		private val memberJpaRepository: MemberJpaRepository) {

	@GetMapping("/v1/members")
	fun searchMemberV1(condition: MemberSearchCondition): MutableList<MemberTeamDto>? {
		return memberJpaRepository.search(condition)
	}
}