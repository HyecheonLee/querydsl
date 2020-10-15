package com.hyecheon.querydsl.repository

import com.hyecheon.querydsl.dto.*
import com.hyecheon.querydsl.entity.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.transaction.annotation.*
import javax.persistence.*

@SpringBootTest
@Transactional
internal class MemberJpaRepositoryTest {
	@Autowired
	lateinit var em: EntityManager

	@Autowired
	lateinit var memberJpaRepository: MemberJpaRepository

	@Test
	internal fun basicTest() {
		val member = Member(username = "member1", age = 10)
		memberJpaRepository.save(member)
		val findMember = memberJpaRepository.findById(member.id!!).get()
		assertThat(findMember).isEqualTo(member)

		val result1 = memberJpaRepository.findAllQuerydsl()
		assertThat(result1).containsExactly(member)

		val result2 = memberJpaRepository.findByUsernameQuerydsl("member1")
		assertThat(result2).containsExactly(member)
	}

	@Test
	internal fun searchTest() {
		val teamA = Team(name = "teamA")
		val teamB = Team(name = "teamB")

		em.persist(teamA)
		em.persist(teamB)

		val member1 = Member(username = "member1", age = 10, team = teamA)
		val member2 = Member(username = "member2", age = 20, team = teamA)

		val member3 = Member(username = "member3", age = 30, team = teamB)
		val member4 = Member(username = "member4", age = 40, team = teamB)

		em.persist(member1)
		em.persist(member2)
		em.persist(member3)
		em.persist(member4)

		val condition = MemberSearchCondition(null, "teamB", 35, 40)

		val result = memberJpaRepository.search(condition)

		assertThat(result).extracting("username").containsExactly("member4")
	}
}