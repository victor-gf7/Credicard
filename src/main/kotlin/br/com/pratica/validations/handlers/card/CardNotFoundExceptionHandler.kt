package br.com.pratica.validations.handlers.card

import br.com.pratica.exceptions.CardNotFoundException
import br.com.pratica.validations.handlers.ExceptionHandler
import io.grpc.Status

class CardNotFoundExceptionHandler: ExceptionHandler<CardNotFoundException> {
    override fun handle(e: CardNotFoundException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )     }

    override fun supports(e: Exception): Boolean {
        return e is CardNotFoundException
    }
}