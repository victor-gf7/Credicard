package br.com.pratica.card

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface CardRepository: JpaRepository<Card, UUID>{

    fun findByCardNumber(cardNumber: String): Optional<Card>
}