package br.com.pratica.integration

import br.com.pratica.card.Card
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.math.BigDecimal
import java.time.LocalDateTime

@Client("\${external.accountcards.url}")
interface CardClient {

    @Get("/api/cartoes")
    fun submitForCheck(@QueryValue(defaultValue = "") idProposta: String) : NewCardResponse
}

@Introspected
data class NewCardResponse(
    @field:JsonProperty("id")
    val cardNumber: String,
    @field:JsonProperty("titular")
    val owner: String,
    @field:JsonProperty("emitidoEm")
    val issuedAt: LocalDateTime,
    @field:JsonProperty("limite")
    val limit: BigDecimal,
    @field:JsonProperty("idProposta")
    val proposalId: String
) {

    fun toModel(): Card {
        return  Card(
            cardNumber = this.cardNumber,
            owner =  this.owner,
            issuedAt = this.issuedAt,
            limitAccount = this.limit,
            proposalId = this.proposalId
        )
    }
}
