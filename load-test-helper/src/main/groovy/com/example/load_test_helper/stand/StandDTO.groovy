package com.example.load_test_helper.stand


import com.example.load_test_helper.domain.DomainDTO
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

class StandDTO {
    @NotBlank
    String name
    @Valid
    @NotEmpty
    List<DomainDTO> domains = []
}
