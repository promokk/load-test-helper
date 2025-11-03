package com.example.load_test_helper.profile

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="profile", schema="load_test_helper")
class Profile {
    @Id
    @Column(name="name",nullable=false)
    String name

    @Column(name="throughput",nullable=false)
    Double throughput

    @Column(name="threads",nullable=false)
    Integer threads

    @Column(name="ramp_up",nullable=false)
    Integer rampUp

    Profile() {}

    Profile(String name, Double throughput, Integer threads, Integer rampUp) {
        this.name = name
        this.throughput = throughput
        this.threads = threads
        this.rampUp = rampUp
    }
}
