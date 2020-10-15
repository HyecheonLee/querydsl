package com.hyecheon.querydsl.repository

import org.assertj.core.api.Assertions.*
import com.hyecheon.querydsl.entity.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.transaction.annotation.*

@SpringBootTest
@Transactional
class MemberRepositoryTest {

	@Autowired
	lateinit var memberRepository: MemberRepository

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
}