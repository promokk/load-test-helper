package com.example.load_test_helper.group

import jakarta.validation.constraints.*

class GroupDTO {
    @NotBlank
    String profile
    @NotBlank
    String server
    Boolean masterRun
    String domain
    @Min(1)
    Integer duration
    String certificate
    String testParam
    String serverParam
}
