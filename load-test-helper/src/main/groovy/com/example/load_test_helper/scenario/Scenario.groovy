package com.example.load_test_helper.scenario

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.CascadeType

import com.example.load_test_helper.group.Group


@Entity
@Table(name="scenario", schema="load_test_helper")
class Scenario {
    @Id
    @Column(name="name",nullable=false)
    String name

    @Column(name="stand",nullable=false)
    String stand

    @Column(name="draft",nullable=false)
    Boolean draft

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name="group",nullable=false)
    List<Group> group

    Scenario() {}

    Scenario(String name, String stand, List<Group> group) {
        this.name = name
        this.stand = stand
        this.group = group
        this.draft = true
    }

    Scenario(String name, String stand, Boolean draft, List<Group> group) {
        this.name = name
        this.stand = stand
        this.group = group
        this.draft = draft
    }
}
