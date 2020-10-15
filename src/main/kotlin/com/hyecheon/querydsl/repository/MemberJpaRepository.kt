package com.hyecheon.querydsl.repository

import com.hyecheon.querydsl.dto.*
import com.hyecheon.querydsl.entity.*
import com.hyecheon.querydsl.entity.QMember.*
import com.hyecheon.querydsl.entity.QTeam.*
import com.querydsl.core.*
import com.querydsl.core.types.*
import com.querydsl.core.types.dsl.*
import com.querydsl.jpa.impl.*
import org.springframework.stereotype.*
import org.springframework.util.*
import java.util.*
import javax.persistence.*

@Repository
class MemberJpaRepository(
		private val em: EntityManager) {
	private val queryFactory: JPAQueryFactory = JPAQueryFactory(em)

	fun save(member: Member) {
		em.persist(member)
	}

	fun findById(id: Long): Optional<Member> {
		val findMember = em.find(Member::class.java, id)
		return Optional.ofNullable(findMember)
	}

	fun findAll(): MutableList<Member>? {
		return em.createQuery("select m from Member m", Member::class.java).resultList
	}

	fun findAllQuerydsl(): MutableList<Member>? {
		return queryFactory.selectFrom(member)
				.fetch();
	}


	fun findByUsername(username: String): MutableList<Member>? {
		return em.createQuery("select m from Member m where m.username = :username", Member::class.java)
				.setParameter("username", username)
				.resultList
	}

	fun findByUsernameQuerydsl(username: String): MutableList<Member>? {
		return queryFactory
				.selectFrom(member)
				.where(member.username.eq(username))
				.fetch()
	}

	fun searchByBuilder(condition: MemberSearchCondition): MutableList<MemberTeamDto>? {
		val builder = BooleanBuilder()
		if (StringUtils.hasText(condition.username)) {
			builder.and(member.username.eq(condition.username))
		}
		if (StringUtils.hasText(condition.teamName)) {
			builder.and(team.name.eq(condition.teamName))
		}
		if (condition.ageGoe != null) {
			builder.and(member.age.goe(condition.ageGoe))
		}
		if (condition.ageLoe != null) {
			builder.and(member.age.loe(condition.ageLoe))
		}

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
				.where(builder)
				.fetch()
	}

	fun search(condition: MemberSearchCondition): MutableList<MemberTeamDto>? {
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

	fun searchMember(condition: MemberSearchCondition): MutableList<Member>? {
		return queryFactory
				.selectFrom(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.username),
						teamNameEq(condition.teamName),
						ageGoe(condition.ageGoe),
						ageLoe(condition.ageLoe)
				)
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