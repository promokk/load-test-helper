package com.example.load_test_helper.scenario

import com.example.load_test_helper.group.GroupDTO
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.validation.annotation.Validated

@Validated
class ScenarioDTO {
    @NotBlank
    String name
    String stand
    @NotBlank
    String domain
    @NotNull
    @Min(1)
    Integer duration
    @NotNull
    Boolean draft
    @Valid
    @NotEmpty
    List<GroupDTO> groups = []
}
