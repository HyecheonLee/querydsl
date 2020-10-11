package com.hyecheon.querydsl.entity

import javax.persistence.*

@Entity
data class Team(
        @Id
        @GeneratedValue
        val id: Long? = null,
        val name: String,
        @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
        val members: MutableList<Member> = mutableListOf()) {
    override fun toString(): String {
        return "Team(id=$id, name='$name')"
    }
}