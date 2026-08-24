package com.example.load_test_helper.group

import com.example.load_test_helper.scenario.Scenario
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
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
@JsonPropertyOrder(["id", "scenario", "profile", "server", "masterRun", "domain", "duration", "certificate", "testParam", "serverParam"])
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

    @Column(name="server")
    String server

    @Column(name="masterRun")
    Boolean masterRun

    @Column(name="domain")
    String domain

    @Column(name="duration")
    Integer duration

    @Column(name="certificate")
    String certificate

    @Column(name="testParam")
    String testParam

    @Column(name="serverParam")
    String serverParam

    Group() {}

    Group(String profile, String server, Boolean masterRun, String domain, Integer duration, String certificate, String testParam, String serverParam) {
        this.profile = profile
        this.server = server
        this.masterRun = masterRun
        this.domain = domain
        this.duration = duration
        this.certificate = certificate
        this.testParam = testParam
        this.serverParam = serverParam
    }
}
