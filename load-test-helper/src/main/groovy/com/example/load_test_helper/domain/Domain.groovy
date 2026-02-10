package com.example.load_test_helper.domain

import com.example.load_test_helper.stand.Stand
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@JsonPropertyOrder(["name", "stands"])
@Table(name="domain", schema="load_test_helper")
class Domain {
    @Id
    @Column(name = "name", nullable = false)
    String name

    @OneToMany(mappedBy = "domain", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name = "stands")
    List<Stand> stands = []

    Domain() {}

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    Domain(@JsonProperty("name") String name,
           @JsonProperty("stands") List<Stand> stands) {
        this.name = name
        this.stands = stands
        this.stands.forEach { it.domain = this }
    }
}
