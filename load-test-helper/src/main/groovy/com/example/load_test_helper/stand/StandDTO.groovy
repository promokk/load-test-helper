package com.example.load_test_helper.stand

import jakarta.validation.constraints.NotBlank

class StandDTO {
    @NotBlank
    String name
    @NotBlank
    String url
}
