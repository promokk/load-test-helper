package com.example.load_test_helper.test

import jakarta.validation.constraints.*

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

    def validateServer() {
        server instanceof List && server?.every { it instanceof String }
    }
}
