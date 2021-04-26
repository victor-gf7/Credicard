package br.com.pratica.exceptions

import java.lang.RuntimeException

class CardNotFoundException(message: String?): RuntimeException(message)
