package com.example.load_test_helper.scenario

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.CascadeType

import com.example.load_test_helper.group.Group


@Entity
@JsonPropertyOrder(["name", "stand", "url", "duration", "customParam", "draft", "groups"])
@Table(name="scenario", schema="load_test_helper")
class Scenario {
    @Id
    @Column(name="name", nullable=false)
    String name

    @Column(name="stand", nullable=false)
    String stand

    @Column(name="url", nullable=false)
    String url

    @Column(name="duration", nullable=false)
    Integer duration

    @Column(name="customParam")
    String customParam

    @Column(name="draft", nullable=false)
    Boolean draft

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name="groups")
    List<Group> groups = []

    Scenario() {}

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    Scenario(@JsonProperty("name") String name,
             @JsonProperty("stand") String stand,
             @JsonProperty("stand") String url,
             @JsonProperty("duration") Integer duration,
             @JsonProperty("duration") String customParam,
             @JsonProperty("draft") Boolean draft,
             @JsonProperty("groups") List<Group> groups) {
        this.name = name
        this.stand = stand
        this.url = url
        this.duration = duration
        this.customParam = customParam
        this.draft = draft
        this.groups = groups
        this.groups.forEach { it.scenario = this }
    }
}
