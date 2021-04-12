package br.com.pratica.proposal

import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class Address(
    @field:NotBlank val street: String,
    @field:NotBlank val neighborhood: String,
    @field:NotBlank val number: String,
    @field:NotBlank val city: String,
    @field:NotBlank val cep: String,
    val complement: String
)
