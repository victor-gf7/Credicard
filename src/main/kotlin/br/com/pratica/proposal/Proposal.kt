package br.com.pratica.proposal

import br.com.pratica.card.Card
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

    @field:NotBlank
    @Column(nullable = false)
    val name: String,

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
    var id: UUID? = null

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProposalStatus = ProposalStatus.NOT_ELIGIBLE

    fun updateStatus(statusBeforeAnalysis: ProposalStatus): Proposal {
        this.status = statusBeforeAnalysis
        return this
    }

    fun attachTo(card: Card) {
        if(card == null){
            throw IllegalArgumentException("Impossible to attach this proposal to a card: card can not to be null")
        }

        card.proposalId = this.id.toString()
        this.updateStatus(ProposalStatus.ELIGIBLE_WITH_ATTACHED_CARD)
    }

}