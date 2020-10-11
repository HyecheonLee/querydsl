package com.hyecheon.querydsl

import com.hyecheon.querydsl.dto.MemberDto
import com.hyecheon.querydsl.dto.UserDto
import com.hyecheon.querydsl.entity.Member
import com.hyecheon.querydsl.entity.QMember
import com.hyecheon.querydsl.entity.QMember.member
import com.hyecheon.querydsl.entity.QTeam.team
import com.hyecheon.querydsl.entity.Team
import com.querydsl.core.Tuple
import com.querydsl.core.types.ExpressionUtils
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceUnit

@SpringBootTest
@Transactional
class QuerydslBasicTest {
    @Autowired
    lateinit var em: EntityManager
    lateinit var queryFactory: JPAQueryFactory

    @BeforeEach
    internal fun before() {

        queryFactory = JPAQueryFactory(em)

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

    }

    @Test
    internal fun startJPQL() {
        //member1을 차자라
        val findMember = em.createQuery("select m from Member m where m.username =: username", Member::class.java)
                .setParameter("username", "member1")
                .singleResult

        assertThat(findMember.username).isEqualTo("member1")
    }

    @Test
    internal fun startQuerydsl() {
        val findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne()
        assertThat(findMember!!.username).isEqualTo("member1")
    }

    @Test
    internal fun search() {
        val findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne()

        assertThat(findMember!!.username).isEqualTo("member1")
    }

    @Test
    internal fun resultFetch() {
        val fetch = queryFactory.selectFrom(member)
                .fetch()

        val fetchOne = queryFactory.selectFrom(member)
                .fetchOne()

    }

    /**
     * 회원 정렬 순서
     * */
    @Test
    internal fun sort() {
        em.persist(Member(age = 100))
        em.persist(Member(username = "member5", age = 100))
        em.persist(Member(username = "member6", age = 100))

        val result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(
                        member.age.desc(),
                        member.username.asc().nullsLast()
                )
                .fetch()
        val member5 = result[0]
        val member6 = result[1]
        val memberNull = result[2]

        assertThat(member5.username).isEqualTo("member5")
        assertThat(member6.username).isEqualTo("member6")
        assertThat(memberNull.username).isNull()

    }

    @Test
    internal fun paging1() {
        val result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch()
        assertThat(result.size).isEqualTo(2)
    }

    @Test
    internal fun paging2() {
        val result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults()

        assertThat(result.total).isEqualTo(4)
        assertThat(result.limit).isEqualTo(2)
        assertThat(result.offset).isEqualTo(1)
        assertThat(result.results.size).isEqualTo(2)
    }

    @Test
    internal fun aggregation() {
        val result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch()

        val tuple = result[0]
        assertThat(tuple.get(member.count())).isEqualTo(4)
        assertThat(tuple.get(member.age.sum())).isEqualTo(100)
        assertThat(tuple.get(member.age.avg())).isEqualTo(25.0)
        assertThat(tuple.get(member.age.max())).isEqualTo(40)
        assertThat(tuple.get(member.age.min())).isEqualTo(10)

    }

    @Test
    internal fun group() {
        val result = queryFactory.select(
                team.name,
                member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch()

        val teamA = result[0]
        val teamB = result[1]

        assertThat(teamA.get(team.name)).isEqualTo("teamA")
        assertThat(teamA.get(member.age.avg())).isEqualTo(15.0)
        assertThat(teamB.get(team.name)).isEqualTo("teamB")
        assertThat(teamB.get(member.age.avg())).isEqualTo(35.0)
    }

    @Test
    internal fun join() {
        val result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch()

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2")
    }

    @Test
    internal fun thetaJoin() {
        em.persist(Member(username = "teamA"))
        em.persist(Member(username = "teamB"))

        val result = queryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch()

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB")

    }

    @Test
    internal fun joinOnFiltering() {
        val result = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team).on(team.name.eq("teamA"))
                .fetch()

        result.forEach { t: Tuple? ->
            println("tuple = $t")
        }
    }

    @Test
    internal fun joinOnNoRelation() {
        em.persist(Member(username = "teamA"))
        em.persist(Member(username = "teamB"))
        em.persist(Member(username = "teamC"))

        val result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch()

        result.forEach { t: Tuple? ->
            println("tuple = $t")
        }
    }

    @PersistenceUnit
    lateinit var emf: EntityManagerFactory

    @Test
    internal fun fetchJoinNo() {
        em.flush()
        em.clear()

        val result = queryFactory.select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne()

        val loaded = emf.persistenceUnitUtil.isLoaded(result!!.team)
        assertThat(loaded).`as`("페치 조인 미적용").isFalse()


    }

    @Test
    internal fun fetchJoin() {
        em.flush()
        em.clear()

        val result = queryFactory.select(member)
                .from(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne()

        val loaded = emf.persistenceUnitUtil.isLoaded(result!!.team)
        assertThat(loaded).`as`("페치 조인 적용").isTrue()


    }

    @Test
    internal fun subQuery() {
        val memberSub = QMember("member_sub")
        val result = queryFactory.selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch()
        assertThat(result).extracting("age")
                .containsExactly(40)
    }

    @Test
    internal fun subQueryGoe() {
        val memberSub = QMember("member_sub")
        val result = queryFactory.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch()
        assertThat(result).extracting("age")
                .containsExactly(30, 40)
    }

    @Test
    internal fun subQueryIn() {
        val memberSub = QMember("member_sub")
        val result = queryFactory.selectFrom(member)
                .where(member.age.`in`(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch()
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40)
    }

    @Test
    internal fun selectSubQuery() {
        val memberSub = QMember("member_sub")
        val result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch()
        result.forEach {
            println("tuple = $it")
        }
    }

    @Test
    internal fun basicCase() {
        val result = queryFactory.select(
                member.age
                        .`when`(10).then("열살")
                        .`when`(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch()
        result.forEach { println("s = $it") }
    }

    @Test
    internal fun complexCase() {
        val result = queryFactory
                .select(CaseBuilder()
                        .`when`(member.age.between(0, 20)).then("0~20살")
                        .`when`(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch()

        result.forEach { println("s = $it") }

    }

    @Test
    internal fun constant() {
        val result = queryFactory
                .select(member.username,
                        Expressions.constant("A"))
                .from(member)
                .from(member)
                .fetch()

        result.forEach { println("tuple = $it") }
    }

    @Test
    internal fun concat() {
        val result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch()

        result.forEach { println("s = $it") }
    }

    @Test
    internal fun simpleProjection() {
        val result = queryFactory.select(member.username)
                .from(member)
                .fetch()

        result.forEach { println("s=${it}") }
    }

    @Test
    internal fun tupleProjection() {
        val result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch()

        result.forEach {
            val username = it.get(member.username)
            val age = it.get(member.age)
            println("username=$username")
            println("age=$age")
        }
    }

    @Test
    internal fun findDtoByJPQL() {
        val result = em.createQuery("select new com.hyecheon.querydsl.dto.MemberDto(m.username,m.age) from Member m", MemberDto::class.java).resultList

        result.forEach {
            println("memberDto = $it")
        }
    }

    @Test
    internal fun findDtoBySetter() {
        val result = queryFactory
                .select(Projections.bean(MemberDto::class.java,
                        member.username, member.age))
                .from(member)
                .fetch()

        result.forEach {
            println("memberDto = $it")
        }
    }

    @Test
    internal fun findDtoByField() {
        val result = queryFactory
                .select(Projections.fields(MemberDto::class.java,
                        member.username, member.age))
                .from(member)
                .fetch()

        result.forEach {
            println("memberDto = $it")
        }
    }

    @Test
    internal fun findDtoByConstructor() {
        val result = queryFactory
                .select(Projections.constructor(MemberDto::class.java,
                        member.username, member.age))
                .from(member)
                .fetch()

        result.forEach {
            println("memberDto = $it")
        }
    }

    @Test
    internal fun findUserDto() {
        val memberSub = QMember("memberSub")
        val result = queryFactory
                .select(Projections.fields(UserDto::class.java,
                        member.username.`as`("name"),
                        ExpressionUtils.`as`(
                                JPAExpressions
                                        .select(memberSub.age.max())
                                        .from(memberSub),
                                "age")))
                .from(member)
                .fetch()

        result.forEach {
            println("memberDto = $it")
        }
    }

}
