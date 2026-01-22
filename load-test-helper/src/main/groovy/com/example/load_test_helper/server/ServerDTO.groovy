package com.example.load_test_helper.server

import jakarta.validation.constraints.*

class ServerDTO {
    @NotBlank
    String name
    Boolean free = true
}
