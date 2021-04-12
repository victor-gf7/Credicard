package br.com.pratica.proposal

import br.com.pratica.validations.annotations.CPForCNPJ
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PositiveOrZero

@Entity
class Proposal(
    @field:CPForCNPJ
    @field:NotBlank
    @Column(nullable = false, unique = true)
    val document: String,

    @field:Email
    @field:NotBlank
    @Column(nullable = false)
    val email: String,

    @field:Valid
    @Embedded
    @Column(nullable = false)
    val address: Address,

    @field:NotNull
    @field:PositiveOrZero
    @Column(nullable = false)
    val salary: BigDecimal
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
}