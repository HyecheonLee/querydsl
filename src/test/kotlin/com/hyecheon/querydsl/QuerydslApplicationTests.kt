package com.hyecheon.querydsl

import com.hyecheon.querydsl.entity.Hello
import com.hyecheon.querydsl.entity.QHello
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

    @Autowired
    lateinit var em: EntityManager

    @Test
    fun contextLoads() {
        val hello = Hello()
        em.persist(hello)

        val query = JPAQueryFactory(em)
        val qHello = QHello.hello
        val result = query.selectFrom(qHello)
                .fetchOne()

        assertThat(result).isEqualTo(hello)
        assertThat(result!!.id).isEqualTo(hello.id)

    }
}
