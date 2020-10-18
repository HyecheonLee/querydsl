package com.hyecheon.querydsl.repository

import com.hyecheon.querydsl.dto.*
import org.springframework.data.domain.*

interface MemberRepositoryCustom {
	fun search(condition: MemberSearchCondition): List<MemberTeamDto>
	fun searchPageSimple(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto>
	fun searchPageComplex(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto>
}