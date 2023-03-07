package br.com.fusiondms.core.model.exceptions

class ErrorJornadaTrabalho(message: String?) : Exception(message)

class ErrorSalvarRegistroPonto(message: String?, val code: Int = 1050) : Exception(
    "$message <br>Code: <b>$code</b>"
)

class ErrorGetColaborador(message: String?, val code: Int = 1051) : Exception(
    "$message <br>Code: <b>$code</b>"
)