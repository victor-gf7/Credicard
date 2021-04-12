package br.com.pratica.endpoints

import br.com.pratica.ProposalRequest
import br.com.pratica.ProposalResponse
import br.com.pratica.RegisterProposalGrpcServiceGrpc
import br.com.pratica.exceptions.ProposalAlreadyExistsException
import br.com.pratica.proposal.*
import br.com.pratica.validations.annotations.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import io.micronaut.transaction.SynchronousTransactionManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.Connection
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegisterProposalGrpcEndpoint(
    @Inject private val repository: ProposalRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>,
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
            repository.save(request.toModel())
        }

        responseObserver.onNext(
            ProposalResponse.newBuilder()
                .setId(proposal.id.toString())
                .setCreatedAt(proposal.createdAt.toGrpcTimestamp())
                .build()
        )
        responseObserver.onCompleted()
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

private fun LocalDateTime.toGrpcTimestamp(): Timestamp {
    val instant = this.atZone(ZoneId.of("UTC")).toInstant()
    return Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()
}
