package com.hyecheon.querydsl.entity

import javax.persistence.*

@Entity
data class Member(
        @Id
        @GeneratedValue
        val id: Long? = null,
        val username: String? = null,
        val age: Int = 0,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "team_id")
        var team: Team? = null) {
    init {
        team?.let { changeTeam(it) }
    }

    fun changeTeam(team: Team) {
        this.team = team
        team.members.add(this)
    }

    override fun toString(): String {
        return "Member(id=$id, username='$username', age=$age)"
    }
}