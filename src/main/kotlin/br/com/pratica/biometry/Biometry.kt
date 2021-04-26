package br.com.pratica.biometry

import br.com.pratica.card.Card
import br.com.pratica.validations.annotations.ValidBase64
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Entity
class Biometry(
    @field:NotBlank
    @field:ValidBase64
    @Column(nullable = false)
    val fingerprint: String,

    @ManyToOne @Valid
    val card: Card
) {
    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
}