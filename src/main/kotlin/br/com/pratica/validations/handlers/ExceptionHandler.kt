package br.com.pratica.validations.handlers

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

interface ExceptionHandler<E: Exception> {

    fun handle(e: E): StatusWithDetails

    fun supports(e: Exception): Boolean

    data class StatusWithDetails(private val status: Status, private val metadata: Metadata = Metadata()) {
        constructor(se: StatusRuntimeException): this(se.status, se.trailers?: Metadata())
        constructor(sp: com.google.rpc.Status): this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException{
            return status.asRuntimeException(metadata)
        }
    }
}