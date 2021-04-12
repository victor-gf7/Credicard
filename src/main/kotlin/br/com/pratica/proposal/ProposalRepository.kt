package br.com.pratica.proposal

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ProposalRepository: JpaRepository<Proposal, UUID> {
    fun existsByDocument(document: String?): Boolean

}
