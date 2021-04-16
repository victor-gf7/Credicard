package br.com.pratica.card

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PositiveOrZero

@Entity
class Card(
    @field:NotBlank
    @Column(nullable = false, unique = true)
    val cardNumber: String,

    @field:NotBlank
    @Column(nullable = false)
    val owner: String,

    @Column(nullable = false)
    val issuedAt: LocalDateTime,

    @field:NotNull
    @field:PositiveOrZero
    @Column(nullable = false)
    val limitAccount: BigDecimal,

    @field:NotBlank
    @Column(nullable = false)
    var proposalId: String
) {

    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
}