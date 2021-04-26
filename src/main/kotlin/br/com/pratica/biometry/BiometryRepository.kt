package br.com.pratica.biometry

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface BiometryRepository : JpaRepository<Biometry, UUID>
