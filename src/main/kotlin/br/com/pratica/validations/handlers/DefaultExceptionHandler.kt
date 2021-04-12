package br.com.pratica.validations.handlers

import br.com.pratica.exceptions.DefaultException
import io.grpc.Status

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.UNKNOWN
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is DefaultException
    }

}
