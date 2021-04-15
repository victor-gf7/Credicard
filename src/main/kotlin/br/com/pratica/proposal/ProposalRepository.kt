package br.com.pratica.proposal

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.QueryHint
import io.micronaut.data.annotation.Repository
import io.micronaut.data.annotation.repeatable.QueryHints
import io.micronaut.data.jpa.repository.JpaRepository
import org.hibernate.LockOptions
import java.util.*

@Repository
interface ProposalRepository: JpaRepository<Proposal, UUID> {
    fun existsByDocument(document: String?): Boolean


    @QueryHints(
        (QueryHint(name = "javax.persistence.lock.timeout", value = (LockOptions.SKIP_LOCKED.toString() + ""))) // ...skip locked
    )
    @Query(value = "SELECT * FROM Proposal p WHERE p.status ='ELIGIBLE' ORDER BY p.created_at ASC LIMIT 5 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    fun findTop5ByStatusOrderByCreatedAtAscAndForUpdate(status: ProposalStatus): List<Proposal> //Pessimistic locking is supported through the use of find*ForUpdate methods
}