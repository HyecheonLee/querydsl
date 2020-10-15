package com.hyecheon.querydsl.controller

import com.hyecheon.querydsl.entity.*
import org.springframework.context.annotation.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.*
import javax.annotation.*
import javax.persistence.*

@Profile("local")
@Component
class InitMember(private val initMemberService: InitMemberService) {

	@PostConstruct
	fun init() {
		initMemberService.init()
	}

	@Component
	class InitMemberService {
		@PersistenceContext
		lateinit var em: EntityManager

		@Transactional
		fun init() {
			val teamA = Team(name = "teamA")
			val teamB = Team(name = "teamB")
			em.persist(teamA)
			em.persist(teamB)

			for (i in 1..100) {
				val selectedTeam = if (i % 2 == 0) teamA else teamB
				em.persist(Member(username = "member${i}", age = i, team = selectedTeam))
			}
		}
	}
}