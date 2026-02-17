package com.example.load_test_helper.test

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

class TestDTO {
    @NotBlank
    String stand
    @NotNull
    @Min(1)
    Integer duration
    @NotEmpty
    List<String> server
    @NotBlank
    String profile
}
