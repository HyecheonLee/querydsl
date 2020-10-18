package com.hyecheon.querydsl.controller

import com.hyecheon.querydsl.dto.*
import com.hyecheon.querydsl.repository.*
import org.springframework.data.domain.*
import org.springframework.web.bind.annotation.*

@RestController
class MemberController(
		private val memberJpaRepository: MemberJpaRepository,
		private val memberRepository: MemberRepository) {

	@GetMapping("/v1/members")
	fun searchMemberV1(condition: MemberSearchCondition): MutableList<MemberTeamDto>? {
		return memberJpaRepository.search(condition)
	}

	@GetMapping("/v2/members")
	fun searchMemberV2(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
		return memberRepository.searchPageSimple(condition, pageable)
	}

	@GetMapping("/v3/members")
	fun searchMemberV3(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
		return memberRepository.searchPageComplex(condition, pageable)
	}
}