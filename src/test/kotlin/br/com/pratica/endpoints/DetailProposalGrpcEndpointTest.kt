package br.com.pratica.endpoints

import br.com.pratica.DetailProposalGrpcServiceGrpc
import br.com.pratica.DetailProposalRequest
import br.com.pratica.DetailProposalResponse
import br.com.pratica.proposal.Address
import br.com.pratica.proposal.Proposal
import br.com.pratica.proposal.ProposalRepository
import br.com.pratica.utils.toGrpcTimestamp
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*
import javax.transaction.Transactional

@MicronautTest(transactional = false)
internal class DetailProposalGrpcEndpointTest(
    private val repository: ProposalRepository,
    private val grpcClient: DetailProposalGrpcServiceGrpc.DetailProposalGrpcServiceBlockingStub
) {

    private val PROPOSAL_ID: UUID = UUID.randomUUID()

    @Test
    @DisplayName("Must return the details of a proposal")
    fun test01() {
        val proposal = repository.save(
            Proposal(
                document = "809.784.250-57",
                name = "João Victor",
                email = "teste@teste.com",
                address = Address(
                    street = "Rua 1",
                    neighborhood = "Bairro Novo",
                    number = "003",
                    city = "Almenara",
                    cep = "11.111-111",
                    complement = "Ap 1"
                ),
                salary = BigDecimal("5000.0")
            )
        )

        val response = grpcClient.detailProposal(
            DetailProposalRequest.newBuilder()
                .setId(proposal.id.toString())
                .build()
        )

        with(response) {
            assertAll({
                assertEquals(proposal.document, document)
                assertEquals(proposal.name, name)
                assertEquals(proposal.email, email)
                assertEquals(proposal.salary.setScale(2).toString(), salary)
                assertEquals(DetailProposalResponse.ProposalStatusGrpc.valueOf(proposal.status.name), status)
                assertEquals(proposal.createdAt.toGrpcTimestamp().seconds, createdAt.seconds)
            })
        }
    }


    @Test
    @DisplayName("Must not detail proposal without registration in the system")
    fun test02() {

        assertThrows<StatusRuntimeException> {
            grpcClient.detailProposal(
                DetailProposalRequest.newBuilder()
                    .setId(PROPOSAL_ID.toString())
                    .build()
            )
        }.also {
            assertEquals(Status.NOT_FOUND.code, it.status.code)
            assertEquals("No proposal found", it.status.description)
        }


    }

    /**
     * The ClientFactory name must be single for each class
     */
    @Factory
    class DetailProposalClientFactory {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): DetailProposalGrpcServiceGrpc.DetailProposalGrpcServiceBlockingStub {
            return DetailProposalGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}