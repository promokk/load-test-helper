package com.example.load_test_helper.group

import jakarta.validation.constraints.*

class GroupDTO {
    @NotBlank
    String profile
    @NotBlank
    String server
    String domain
    @Min(1)
    Integer duration
    String customParam
}
