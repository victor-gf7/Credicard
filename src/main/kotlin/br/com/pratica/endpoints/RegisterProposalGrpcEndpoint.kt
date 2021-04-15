package br.com.pratica.endpoints

import br.com.pratica.ProposalRequest
import br.com.pratica.ProposalResponse
import br.com.pratica.RegisterProposalGrpcServiceGrpc
import br.com.pratica.exceptions.ProposalAlreadyExistsException
import br.com.pratica.integration.AnalysisRequest
import br.com.pratica.integration.FinancialClient
import br.com.pratica.proposal.Address
import br.com.pratica.proposal.Proposal
import br.com.pratica.proposal.ProposalRepository
import br.com.pratica.proposal.ProposalStatus
import br.com.pratica.utils.toGrpcTimestamp
import br.com.pratica.validations.annotations.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.transaction.SynchronousTransactionManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegisterProposalGrpcEndpoint(
    @Inject private val repository: ProposalRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>,
    @Inject val financialClient: FinancialClient,
) : RegisterProposalGrpcServiceGrpc.RegisterProposalGrpcServiceImplBase() {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun registerProposal(request: ProposalRequest, responseObserver: StreamObserver<ProposalResponse>) {
        logger.info("registering proposal...")
        logger.info("New Proposal: $request")

        /**
         * favoreça controle transacional programático
         */
        val proposal = transactionManager.executeWrite {

            if (repository.existsByDocument(request.document)) {
                throw ProposalAlreadyExistsException("Proposal already exists")
            }
            logger.info("Saving proposal...")
            repository.save(request.toModel()).also { proposal ->
                val statusBeforeAnalysis = submitForAnalysis(proposal)

                logger.info("Analysis has ended, the result was: $statusBeforeAnalysis")
                logger.info("Updating status of proposal ${proposal.id}")

                proposal.updateStatus(statusBeforeAnalysis)

                logger.info("Proposal saved")
            }
        }

        responseObserver.onNext(
            ProposalResponse.newBuilder()
                .setId(proposal.id.toString())
                .setCreatedAt(proposal.createdAt.toGrpcTimestamp())
                .build()
        )
        responseObserver.onCompleted()
    }

    private fun submitForAnalysis(proposal: Proposal): ProposalStatus {


        return try {
            logger.info("submitting proposal ${proposal.id} for external analysis")
            financialClient.submitForAnalysis(
                AnalysisRequest(
                    document = proposal.document,
                    name = proposal.name,
                    proposalId = proposal.id.toString()
                )
            ).toModel()
        } catch (e: HttpClientResponseException) {
            logger.info("error for external analysis")
            ProposalStatus.NOT_ELIGIBLE
        }
    }
}

private fun ProposalRequest.toModel(): Proposal {
    return Proposal(
        document = this.document,
        name = this.name,
        email = this.email,
        address = Address(
            street = this.address.street,
            neighborhood = this.address.neighborhood,
            number = this.address.number,
            city = this.address.city,
            cep = this.address.cep,
            complement = this.address.complement
        ),
        salary = BigDecimal(this.salary)
    )
}
