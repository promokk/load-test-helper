package com.example.load_test_helper.test

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp

import java.time.LocalDateTime

@Entity
@Table(name="test", schema="load_test_helper")
class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="id",nullable=false)
    Integer id

    @CreationTimestamp
    @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss")
    @Column(name="createdAt",nullable=false)
    LocalDateTime createdAt

    @Column(name="stand",nullable=false)
    String stand

    @Column(name="duration",nullable=false)
    Integer duration

    @ElementCollection
    @CollectionTable(name = "test_server", schema="load_test_helper")
    @Column(name="server")
    List<String> server

    @ElementCollection
    @CollectionTable(name = "test_profile", schema="load_test_helper")
    @Column(name="profile")
    List<String> profile

    Test() {}

    Test(String stand, Integer duration, List<String> server, List<String> profile) {
        this.stand = stand
        this.duration = duration
        this.server = server
        this.profile = profile
    }
}
