package com.example.load_test_helper.scenario

import com.example.load_test_helper.group.GroupDTO
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.validation.annotation.Validated

@Validated
class ScenarioDTO {
    @NotBlank
    String name
    @NotBlank
    String stand
    @NotBlank
    String url
    @NotNull
    @Min(1)
    Integer duration
    @NotNull
    Boolean draft
    @Valid
    @NotEmpty
    List<GroupDTO> group = []
}
