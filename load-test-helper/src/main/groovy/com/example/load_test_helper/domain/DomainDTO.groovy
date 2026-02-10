package com.example.load_test_helper.domain

import com.example.load_test_helper.stand.StandDTO
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

class DomainDTO {
    @NotBlank
    String name
    @Valid
    @NotEmpty
    List<StandDTO> stands = []
}
