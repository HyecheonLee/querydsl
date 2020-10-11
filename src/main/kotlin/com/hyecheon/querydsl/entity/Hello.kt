package com.hyecheon.querydsl.entity

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Hello(
        @Id
        @GeneratedValue
        var id: Long? = null)