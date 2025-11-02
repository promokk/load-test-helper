package com.example.load_test_helper.server

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="server", schema="public")
class Server {
    @Id
    @Column(name="name",nullable=false)
    String name
    @Column(name="free",nullable=false)
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
