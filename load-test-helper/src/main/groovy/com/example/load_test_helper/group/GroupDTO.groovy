package com.example.load_test_helper.group

import jakarta.validation.constraints.*

class GroupDTO {
    @NotBlank
    String profile
    @NotBlank
    String server
    @NotBlank
    String url
}
