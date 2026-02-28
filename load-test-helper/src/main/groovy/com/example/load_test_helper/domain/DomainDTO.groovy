package com.example.load_test_helper.domain

import jakarta.validation.constraints.NotBlank

class DomainDTO {
    @NotBlank
    String name
    @NotBlank
    String url
}
