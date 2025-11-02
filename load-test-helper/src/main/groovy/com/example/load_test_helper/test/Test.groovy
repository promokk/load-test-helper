package com.example.load_test_helper.test

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp

import java.time.LocalDateTime

@Entity
@Table(name="test", schema="public")
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
    @Column(name="server",nullable=false)
    String server
    @Column(name="profile",nullable=false)
    ArrayList profile

    Test() {}

    Test(String stand, String server, ArrayList profile) {
        this.stand = stand
        this.server = server
        this.profile = profile
    }
}
