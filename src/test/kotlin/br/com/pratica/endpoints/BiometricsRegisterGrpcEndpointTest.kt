package br.com.pratica.endpoints

import br.com.pratica.BiometricsRegisterGrpcServiceGrpc
import br.com.pratica.BiometricsRequest
import br.com.pratica.biometry.BiometryRepository
import br.com.pratica.card.Card
import br.com.pratica.card.CardRepository
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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class BiometricsRegisterGrpcEndpointTest(
    private val biometryRepository: BiometryRepository,
    private val proposalRepository: ProposalRepository,
    private val cardRepository: CardRepository,
    private val grpcClient: BiometricsRegisterGrpcServiceGrpc.BiometricsRegisterGrpcServiceBlockingStub
) {

    @BeforeEach
    fun setup() {
        biometryRepository.deleteAll()
        cardRepository.deleteAll()
        proposalRepository.deleteAll()
    }

    @Test
    @DisplayName("Must register a biometry")
    fun test01() {
        proposalRepository.save(
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
            ).also {
                it.status = ProposalStatus.ELIGIBLE_WITH_ATTACHED_CARD
            }
        ).let { proposal ->
            cardRepository.save(
                Card(
                    cardNumber = "3909-6789-4406-3384",
                    owner = "João",
                    issuedAt = LocalDateTime.now(),
                    limitAccount = BigDecimal("9923.00"),
                    proposalId = proposal.id.toString()
                )
            )
        }

        val response = grpcClient.registerBiometrics(
            BiometricsRequest.newBuilder()
                .setCardNumber("3909-6789-4406-3384")
                .setFingerprint("dGVzdGU=")
                .build()
        )

        with(response) {
            assertNotNull(id)
            assertNotNull(createdAt)
            assertTrue(biometryRepository.existsById(UUID.fromString(id)))
        }
    }

    @Test
    @DisplayName("Must not register a biometry with a non-existent card")
    fun test02(){
        assertThrows<StatusRuntimeException> {
            grpcClient.registerBiometrics(
                BiometricsRequest.newBuilder()
                    .setCardNumber("3909-6789-4406-3384")
                    .setFingerprint("dGVzdGU=")
                    .build()
            )
        }.also {
            assertEquals(Status.NOT_FOUND.code, it.status.code)
            assertEquals("No card found", it.status.description)
        }
    }

    @Test
    @DisplayName("Must not register biometry when invalid data")
    fun test03() {
        proposalRepository.save(
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
            ).also {
                it.status = ProposalStatus.ELIGIBLE_WITH_ATTACHED_CARD
            }
        ).let { proposal ->
            cardRepository.save(
                Card(
                    cardNumber = "3909-6789-4406-3384",
                    owner = "João",
                    issuedAt = LocalDateTime.now(),
                    limitAccount = BigDecimal("9923.00"),
                    proposalId = proposal.id.toString()
                )
            )
        }

        assertThrows<StatusRuntimeException> {
            grpcClient.registerBiometrics(
                BiometricsRequest.newBuilder()
                    .setCardNumber("3909-6789-4406-3384")
                    .setFingerprint("aaaaaaa")
                    .build()
            )
        }.also {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals("request with invalid parameters", it.status.description)
            assertThat(it.violations(), containsInAnyOrder(
                Pair("fingerprint", "It is not a valid Base64"),
            ))

        }
    }


    /**
     * the ClientFactory name must be unique for each class
     */
    @Factory
    class BiometricsRegisterClientFactory {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): BiometricsRegisterGrpcServiceGrpc.BiometricsRegisterGrpcServiceBlockingStub {
            return BiometricsRegisterGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}