package br.com.pratica.endpoints

import br.com.pratica.ProposalRequest
import br.com.pratica.RegisterProposalGrpcServiceGrpc
import br.com.pratica.proposal.Address
import br.com.pratica.proposal.Proposal
import br.com.pratica.proposal.ProposalRepository
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.util.*

@MicronautTest(transactional = false)
internal class RegisterProposalGrpcEndpointTest(
    private val repository: ProposalRepository,
    private val grpcClient: RegisterProposalGrpcServiceGrpc.RegisterProposalGrpcServiceBlockingStub,
) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("Must register a proposal")
    fun test01() {
        val response = grpcClient.registerProposal(
            ProposalRequest.newBuilder()
                .setDocument("809.784.250-57")
                .setEmail("teste@teste.com")
                .setAddress(
                    ProposalRequest.Address.newBuilder()
                        .setStreet("Rua 1")
                        .setNeighborhood("Bairro Novo")
                        .setNumber("003")
                        .setCity("Almenara")
                        .setCep("11.111-111")
                        .setComplement("Ap 1")
                        .build()
                )
                .setSalary("5000.0")
                .build()
        )

        with(response) {
            assertNotNull(id)
            assertNotNull(createdAt)
            assertTrue(repository.existsById(UUID.fromString(id)))
        }
    }

    @Test
    @DisplayName("Must not register a new proposal when there is a proposal for the same document")
    fun test02() {
        repository.save(
            Proposal(
                document = "809.784.250-57",
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
        ).let { proposal ->
            assertThrows<StatusRuntimeException>{
                grpcClient.registerProposal(
                    ProposalRequest.newBuilder()
                        .setDocument(proposal.document)
                        .setEmail("rafael.ponte@zup.com.br")
                        .setAddress(
                            ProposalRequest.Address.newBuilder()
                                .setStreet("Rua 2")
                                .setNeighborhood("Bairro teste")
                                .setNumber("100")
                                .setCity("São Paulo")
                                .setCep("58.111-111")
                                .setComplement("Ap 101")
                                .build()
                        )
                        .setSalary("15000.0")
                        .build()
                )
            }.also {
                assertEquals(Status.ALREADY_EXISTS.code, it.status.code)
                assertEquals("Proposal already exists", it.status.description)
            }
        }
    }

    @Test
    @DisplayName("Must not register proposal when invalid data")
    fun test03(){
        assertThrows<StatusRuntimeException> {
            grpcClient.registerProposal(
                ProposalRequest.newBuilder()
                    .setSalary("-0.1")
                    .build()
            )
        }.also {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("request with invalid parameters", it.status.description)
            assertThat(it.violations(), containsInAnyOrder(
                Pair("document", "não deve estar em branco"),
                Pair("document", "Value reported for the document is invalid"),
                Pair("email", "não deve estar em branco"),
                Pair("street", "não deve estar em branco"),
                Pair("neighborhood", "não deve estar em branco"),
                Pair("number", "não deve estar em branco"),
                Pair("city", "não deve estar em branco"),
                Pair("cep", "não deve estar em branco"),
                Pair("salary", "deve ser maior ou igual a 0"),
            ))
        }
    }

    @Factory
    class ClientsFactory {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegisterProposalGrpcServiceGrpc.RegisterProposalGrpcServiceBlockingStub {
            return RegisterProposalGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun StatusRuntimeException.violations(): List<Pair<String, String>> {
        val details = StatusProto.fromThrowable(this)
            ?.detailsList?.get(0)!!
            .unpack(BadRequest::class.java)

        return details.fieldViolationsList
            .map { it.field to it.description }
    }
}
