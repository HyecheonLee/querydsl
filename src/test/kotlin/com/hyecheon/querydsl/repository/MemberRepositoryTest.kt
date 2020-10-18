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
class MemberRepositoryTest {

	@Autowired
	lateinit var memberRepository: MemberRepository

	@Autowired
	lateinit var em: EntityManager

	@Test
	internal fun basicTest() {
		val member = Member(username = "member1", age = 10)
		memberRepository.save(member)
		val findMember = memberRepository.findById(member.id!!).get()
		assertThat(findMember).isEqualTo(member)

		val result1 = memberRepository.findAll()
		assertThat(result1).containsExactly(member)

		val result2 = memberRepository.findByUsername("member1")
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

		val result = memberRepository.search(condition)

		assertThat(result).extracting("username").containsExactly("member4")
	}

	@Test
	internal fun querydslPredicateExecutorTest() {
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

		val result = memberRepository
				.findAll(
						QMember.member.age.between(10, 40)
								.and(QMember.member.username.eq("member1")))
		result.forEach {
			println("member1=$it")
		}
	}
}