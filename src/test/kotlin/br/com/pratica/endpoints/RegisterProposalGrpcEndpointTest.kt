package br.com.pratica.endpoints

import br.com.pratica.ProposalRequest
import br.com.pratica.RegisterProposalGrpcServiceGrpc
import br.com.pratica.integration.AnalysisRequest
import br.com.pratica.integration.AnalysisResponse
import br.com.pratica.integration.FinancialClient
import br.com.pratica.proposal.Address
import br.com.pratica.proposal.Proposal
import br.com.pratica.proposal.ProposalRepository
import br.com.pratica.proposal.ProposalStatus
import br.com.pratica.shared.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegisterProposalGrpcEndpointTest(
    private val repository: ProposalRepository,
    private val grpcClient: RegisterProposalGrpcServiceGrpc.RegisterProposalGrpcServiceBlockingStub,
) {

    @Inject
    lateinit var financialClient: FinancialClient

    private val PROPOSAL_ID: UUID = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("Must register a eligible proposal")
    fun test01() {

        `when`(
            financialClient.submitForAnalysis(
                AnalysisRequest(
                    document = "809.784.250-57",
                    name = "João Victor",
                    proposalId = PROPOSAL_ID.toString()
                )
            )
        )
            .thenReturn(
                    AnalysisResponse(
                        proposalId = PROPOSAL_ID.toString(),
                        analysisResult = "SEM_RESTRICAO"
                    )
            )


        val response = grpcClient.registerProposal(
            ProposalRequest.newBuilder()
                .setDocument("809.784.250-57")
                .setName("João Victor")
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
            assertEquals(ProposalStatus.ELIGIBLE, repository.findById(UUID.fromString(id)).get().status)
        }
    }

    @Test
    @DisplayName("Must register a not eligible proposal")
    fun test02() {

        `when`(
            financialClient.submitForAnalysis(
                AnalysisRequest(
                    document = "371.978.390-17",
                    name = "João Victor",
                    proposalId = PROPOSAL_ID.toString()
                )
            )
        )
            .thenReturn(
                AnalysisResponse(
                    proposalId = PROPOSAL_ID.toString(),
                    analysisResult = "COM_RESTRICAO"
                )
            )


        val response = grpcClient.registerProposal(
            ProposalRequest.newBuilder()
                .setDocument("371.978.390-17")
                .setName("João Victor")
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
            assertEquals(ProposalStatus.NOT_ELIGIBLE, repository.findById(UUID.fromString(id)).get().status)
        }
    }

    @Test
    @DisplayName("Must not register a new proposal when there is a proposal for the same document")
    fun test03() {
        repository.save(
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
        ).let { proposal ->
            assertThrows<StatusRuntimeException> {
                grpcClient.registerProposal(
                    ProposalRequest.newBuilder()
                        .setDocument(proposal.document)
                        .setName("Teste 2")
                        .setEmail("teste2@zup.com.br")
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
    fun test04() {
        assertThrows<StatusRuntimeException> {
            grpcClient.registerProposal(
                ProposalRequest.newBuilder()
                    .setSalary("-0.1")
                    .build()
            )
        }.also {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("request with invalid parameters", it.status.description)
            assertThat(
                it.violations(), containsInAnyOrder(
                    Pair("name", "não deve estar em branco"),
                    Pair("document", "não deve estar em branco"),
                    Pair("document", "Value reported for the document is invalid"),
                    Pair("email", "não deve estar em branco"),
                    Pair("street", "não deve estar em branco"),
                    Pair("neighborhood", "não deve estar em branco"),
                    Pair("number", "não deve estar em branco"),
                    Pair("city", "não deve estar em branco"),
                    Pair("cep", "não deve estar em branco"),
                    Pair("salary", "deve ser maior ou igual a 0"),
                )
            )
        }
    }


    /**
     * declarative beans for injection
     */

    @MockBean(FinancialClient::class)
    fun mockFinancialClient(): FinancialClient {
        return mock(FinancialClient::class.java)
    }

    /**
     * The ClientFactory name must be single for each class
     */
    @Factory
    class RegisterProposalClientFactory {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegisterProposalGrpcServiceGrpc.RegisterProposalGrpcServiceBlockingStub {
            return RegisterProposalGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}

