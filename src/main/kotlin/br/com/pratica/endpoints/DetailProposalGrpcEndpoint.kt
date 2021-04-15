package br.com.pratica.endpoints

import br.com.pratica.DetailProposalGrpcServiceGrpc
import br.com.pratica.DetailProposalRequest
import br.com.pratica.DetailProposalResponse
import br.com.pratica.exceptions.ProposalNotFoundException
import br.com.pratica.proposal.ProposalRepository
import br.com.pratica.utils.toGrpcTimestamp
import br.com.pratica.validations.annotations.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DetailProposalGrpcEndpoint(
    @Inject private val repository: ProposalRepository
): DetailProposalGrpcServiceGrpc.DetailProposalGrpcServiceImplBase() {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun detailProposal(
        request: DetailProposalRequest,
        responseObserver: StreamObserver<DetailProposalResponse>
    ) {
        logger.info("searching for proposal...")
        val proposal = repository.findById(UUID.fromString(request.id))
        if (proposal.isEmpty){
            throw ProposalNotFoundException("No proposal found")
        }
        logger.info("Found proposal: ${proposal.get()}")

        responseObserver.onNext(
            DetailProposalResponse.newBuilder()
                .setDocument(proposal.get().document)
                .setName(proposal.get().name)
                .setEmail(proposal.get().email)
                .setSalary(proposal.get().salary.toString())
                .setStatus(DetailProposalResponse.ProposalStatusGrpc.valueOf(proposal.get().status.name))
                .setCreatedAt(proposal.get().createdAt.toGrpcTimestamp())
                .build()
        )
        responseObserver.onCompleted()
    }
}