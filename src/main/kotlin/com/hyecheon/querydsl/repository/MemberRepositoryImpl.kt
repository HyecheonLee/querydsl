package com.hyecheon.querydsl.repository

import com.hyecheon.querydsl.dto.*
import com.hyecheon.querydsl.entity.QMember.*
import com.hyecheon.querydsl.entity.QTeam.*
import com.querydsl.core.types.*
import com.querydsl.core.types.dsl.*
import com.querydsl.jpa.impl.*
import org.springframework.data.domain.*
import org.springframework.data.repository.support.*
import org.springframework.util.*
import javax.persistence.*

class MemberRepositoryImpl(em: EntityManager) : MemberRepositoryCustom {
	private val queryFactory = JPAQueryFactory(em)

	override fun search(condition: MemberSearchCondition): List<MemberTeamDto> {
		return queryFactory
				.select(QMemberTeamDto(
						member.id.`as`("memberId"),
						member.username,
						member.age,
						team.id.`as`("teamId"),
						team.name.`as`("teamName")
				))
				.from(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.username),
						teamNameEq(condition.teamName),
						ageGoe(condition.ageGoe),
						ageLoe(condition.ageLoe)
				)
				.fetch()
	}

	override fun searchPageSimple(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
		val results = queryFactory
				.select(QMemberTeamDto(
						member.id.`as`("memberId"),
						member.username,
						member.age,
						team.id.`as`("teamId"),
						team.name.`as`("teamName")
				))
				.from(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.username),
						teamNameEq(condition.teamName),
						ageGoe(condition.ageGoe),
						ageLoe(condition.ageLoe)
				)
				.offset(pageable.offset)
				.limit(pageable.pageSize.toLong())
				.fetchResults()
		val content = results.results
		val total = results.total
		return PageImpl(content, pageable, total)
	}

	override fun searchPageComplex(condition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
		val content = getContent(condition, pageable)
		val countQuery = queryFactory.select(member)
				.from(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.username),
						teamNameEq(condition.teamName),
						ageGoe(condition.ageGoe),
						ageLoe(condition.ageLoe)
				)

//		return PageImpl(content, pageable, total)
		return PageableExecutionUtils.getPage(content, pageable) {
			countQuery.fetchCount()
		}
	}

	private fun getContent(condition: MemberSearchCondition, pageable: Pageable): List<MemberTeamDto> {
		return queryFactory
				.select(QMemberTeamDto(
						member.id.`as`("memberId"),
						member.username,
						member.age,
						team.id.`as`("teamId"),
						team.name.`as`("teamName")
				))
				.from(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.username),
						teamNameEq(condition.teamName),
						ageGoe(condition.ageGoe),
						ageLoe(condition.ageLoe)
				)
				.offset(pageable.offset)
				.limit(pageable.pageSize.toLong())
				.fetch()
	}

	private fun ageLoe(ageLoe: Int?): Predicate? {
		return if (ageLoe != null) member.age.loe(ageLoe) else null
	}

	private fun ageGoe(ageGoe: Int?): BooleanExpression? {
		return if (ageGoe != null) member.age.goe(ageGoe) else null
	}

	private fun teamNameEq(teamName: String?): BooleanExpression? {
		return if (StringUtils.hasText(teamName)) team.name.eq(teamName) else null
	}

	private fun usernameEq(username: String?): BooleanExpression? {
		return if (StringUtils.hasText(username)) member.username.eq(username) else null
	}
}