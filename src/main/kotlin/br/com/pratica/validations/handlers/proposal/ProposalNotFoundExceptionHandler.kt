package br.com.pratica.validations.handlers.proposal

import br.com.pratica.exceptions.ProposalNotFoundException
import br.com.pratica.validations.handlers.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ProposalNotFoundExceptionHandler: ExceptionHandler<ProposalNotFoundException> {
    override fun handle(e: ProposalNotFoundException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )    }

    override fun supports(e: Exception): Boolean {
        return e is ProposalNotFoundException
    }

}