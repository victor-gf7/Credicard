package br.com.pratica.card

import br.com.pratica.integration.CardClient
import br.com.pratica.integration.NewCardResponse
import br.com.pratica.proposal.Proposal
import br.com.pratica.proposal.ProposalRepository
import br.com.pratica.proposal.ProposalStatus
import io.micronaut.scheduling.annotation.Scheduled
import io.micronaut.transaction.SynchronousTransactionManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssociateCard(
    @Inject private val cardClient: CardClient,
    @Inject private val proposalRepository: ProposalRepository,
    @Inject private val cardRepository: CardRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelay = "50s")
    fun execute() {
        logger.info("Executing Scheduled...")
        var pending: Boolean = true

        while (pending) {
            pending = transactionManager.executeWrite {
                logger.info("Initializing transactional context. Searching for eligible proposals")
                val eligibleProposals =
                    proposalRepository.findTop5ByStatusOrderByCreatedAtAscAndForUpdate(ProposalStatus.ELIGIBLE)
                if (eligibleProposals.isEmpty()) {
                    logger.info("No proposal found")
                    return@executeWrite false
                }

                eligibleProposals.forEach { proposal ->
                    getFindCardByProposalIdResponse(proposal).also { response ->
                        if (response != null){
                            val card: Card = response.toModel()
                            logger.info("Saving card $card")
                            cardRepository.save(card)

                            proposal.attachTo(card)
                            proposalRepository.save(proposal)
                        }

                    }
                }
                return@executeWrite true
            }
        }
        logger.info("process has ended with successful.")
    }

    private fun getFindCardByProposalIdResponse(proposal: Proposal): NewCardResponse? {
        return try {
            logger.info("submitting for card consultation through the external system")
            val response: NewCardResponse = cardClient.submitForCheck(idProposta = proposal.id.toString())
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}