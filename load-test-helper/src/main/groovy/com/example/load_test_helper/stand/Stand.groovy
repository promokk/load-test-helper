package com.example.load_test_helper.stand

import com.example.load_test_helper.domain.Domain
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
@JsonPropertyOrder(["name", "domains"])
@Table(name="stand", schema="load_test_helper")
class Stand {
    @Id
    @Column(name = "name", nullable = false)
    String name

    @OneToMany(mappedBy = "stand", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name = "domains")
    List<Domain> domains = []

    Stand() {}

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    Stand(@JsonProperty("name") String name,
          @JsonProperty("domains") List<Domain> domains) {
        this.name = name
        this.domains = domains
        this.domains.forEach { it.stand = this }
    }
}
