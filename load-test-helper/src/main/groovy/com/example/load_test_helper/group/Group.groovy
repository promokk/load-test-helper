package com.example.load_test_helper.group

import com.example.load_test_helper.scenario.Scenario
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.FetchType
import jakarta.persistence.Table


@Entity
@Table(name="group", schema="load_test_helper")
class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="id",nullable=false)
    Integer id

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "scenario_id")
    Scenario scenario

    @Column(name="profile", nullable=false)
    String profile

    @Column(name="server", nullable=false)
    String server

    @Column(name="url", nullable=false)
    String url

    Group() {}

    Group(String profile, String server, String url) {
        this.profile = profile
        this.server = server
        this.url = url
    }
}
