package com.example.load_test_helper.domain


import com.example.load_test_helper.stand.Stand
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table


@Entity
@JsonPropertyOrder(["stand", "name", "url"])
@Table(name="domain", schema="load_test_helper")
class Domain {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="id",nullable=false)
    Integer id

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "stand_id")
    Stand stand

    @Column(name="name", nullable=false)
    String name

    @Column(name="url", nullable=false)
    String url
}
