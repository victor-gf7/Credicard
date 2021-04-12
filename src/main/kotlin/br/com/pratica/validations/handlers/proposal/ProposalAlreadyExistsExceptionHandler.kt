package br.com.pratica.validations.handlers.proposal

import br.com.pratica.exceptions.ProposalAlreadyExistsException
import br.com.pratica.validations.handlers.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ProposalAlreadyExistsExceptionHandler: ExceptionHandler<ProposalAlreadyExistsException> {
    override fun handle(e: ProposalAlreadyExistsException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ProposalAlreadyExistsException
    }
}