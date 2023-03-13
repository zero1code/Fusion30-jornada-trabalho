package br.com.fusiondms.core.model.exceptions

class ErrorSalvarRegistroPonto(message: String?, val code: Int = 1050) : Exception(
    "$message <br>Code: <b>$code</b>"
)

class ErrorGetColaborador(message: String?, val code: Int = 1051) : Exception(
    "$message <br>Code: <b>$code</b>"
)

class ErrorUpdateStatusRegistroPonto(message: String?, val code: Int = 1052) : Exception(
    "$message <br>Code: <b>$code</b>"
)

class ErrorApiEnvioRegistroPonto(message: String?, val code: Int = 1053) : Exception(
    "$message <br>Code: <b>$code</b>"
)