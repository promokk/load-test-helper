package com.example.load_test_helper.server

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@JsonPropertyOrder(["name", "free"])
@Table(name="server", schema="load_test_helper")
class Server {
    @Id
    @Column(name="name", nullable=false)
    String name

    @Column(name="free", nullable=false)
    Boolean free

    Server() {}

    Server(String name) {
        this.name = name
        this.free = true
    }

    Server(String name, Boolean free) {
        this.name = name
        this.free = free
    }
}
