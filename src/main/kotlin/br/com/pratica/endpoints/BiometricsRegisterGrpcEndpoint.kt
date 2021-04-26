package br.com.pratica.endpoints

import br.com.pratica.BiometricsRegisterGrpcServiceGrpc
import br.com.pratica.BiometricsRequest
import br.com.pratica.BiometricsResponse
import br.com.pratica.biometry.Biometry
import br.com.pratica.biometry.BiometryRepository
import br.com.pratica.card.Card
import br.com.pratica.card.CardRepository
import br.com.pratica.exceptions.CardNotFoundException
import br.com.pratica.utils.toGrpcTimestamp
import br.com.pratica.validations.annotations.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class BiometricsRegisterGrpcEndpoint(
    @Inject private val cardRepository: CardRepository,
    @Inject private val biometryRepository: BiometryRepository
) : BiometricsRegisterGrpcServiceGrpc.BiometricsRegisterGrpcServiceImplBase() {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun registerBiometrics(
        request: BiometricsRequest,
        responseObserver: StreamObserver<BiometricsResponse>
    ) {
        val biometry: Biometry = cardRepository.findByCardNumber(request.cardNumber).let { card ->
            if (!card.isPresent) {
                throw CardNotFoundException("No card found")
            }

            biometryRepository.save(request.toModel(card.get()))
        }

        responseObserver.onNext(
            BiometricsResponse.newBuilder()
                .setId(biometry.id.toString())
                .setCreatedAt(biometry.createdAt.toGrpcTimestamp())
                .build()
        )
        responseObserver.onCompleted()
    }
}

private fun BiometricsRequest.toModel(card: Card): Biometry {
    return Biometry(
        fingerprint = this.fingerprint,
        card = card
    )
}
