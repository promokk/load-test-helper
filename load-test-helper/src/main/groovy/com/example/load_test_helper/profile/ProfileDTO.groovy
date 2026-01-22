package com.example.load_test_helper.profile

import jakarta.validation.constraints.*

class ProfileDTO {
    @NotBlank
    String name
    @NotNull
    Double throughput
    @NotNull
    @Min(1)
    Integer threads
    @NotNull
    @Min(1)
    Integer rampUp
}
