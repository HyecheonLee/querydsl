package com.hyecheon.querydsl.entity

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
internal class MemberTest {
    @Autowired
    lateinit var em: EntityManager

    @Test
    internal fun testEntity() {
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

        //초기화
        em.flush()
        em.clear()

        val members = em.createQuery("select  m from Member m", Member::class.java)
                .resultList

        members.forEach { member: Member? ->
            println("member = $member")
            println("-> member.team = ${member?.team}")
        }
    }
}