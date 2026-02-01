package com.example.load_test_helper.group

import jakarta.validation.constraints.*

class GroupDTO {
    @NotBlank
    String profile
    @NotBlank
    String server
    String url
    @Min(1)
    Integer duration
}
