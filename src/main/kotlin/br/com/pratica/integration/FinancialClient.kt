package br.com.pratica.integration

import br.com.pratica.proposal.ProposalStatus
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${external.financialanalysis.url}")
interface FinancialClient {

    @Post("/api/solicitacao")
    fun submitForAnalysis(@Body request: AnalysisRequest): AnalysisResponse
}

@Introspected
data class AnalysisRequest(
    @field:JsonProperty("documento")
    val document: String,
    @field:JsonProperty("nome")
    val name: String,
    @field:JsonProperty("idProposta")
    val proposalId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnalysisRequest

        if (document != other.document) return false

        return true
    }

    override fun hashCode(): Int {
        return document.hashCode()
    }
}

@Introspected
data class AnalysisResponse(
    @field:JsonProperty("idProposta")
    val proposalId: String,
    @field:JsonProperty("resultadoSolicitacao")
    val analysisResult: String
) {
    fun toModel(): ProposalStatus {
        if ("SEM_RESTRICAO" == analysisResult){
            return ProposalStatus.ELIGIBLE
        }

        return ProposalStatus.NOT_ELIGIBLE
    }
}
