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
@JsonPropertyOrder(["name", "stand", "url", "duration", "draft", "group"])
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

    @Column(name="draft", nullable=false)
    Boolean draft

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name="group")
    List<Group> group = []

    Scenario() {}

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    Scenario(@JsonProperty("name") String name,
            @JsonProperty("stand") String stand,
            @JsonProperty("stand") String url,
            @JsonProperty("stand") String duration,
            @JsonProperty("draft") Boolean draft,
            @JsonProperty("group") List<Group> group) {
        this.name = name
        this.stand = stand
        this.stand = url
        this.stand = duration
        this.draft = draft
        this.group = group
        this.group.forEach { it.scenario = this }
    }
}
