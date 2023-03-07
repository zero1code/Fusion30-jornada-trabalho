package br.com.fusiondms.core.model.exceptions

class ErrorLogin(message: String?) : Exception(message)

class ErrorApiLogin(message: String?, val code: Int = 1000) : Exception(
    "$message <br>Code: <b>$code</b>"
)

class ErrorSalvarColaboradorLocalLogin(message: String?, val code: Int = 1002) : Exception(
    "$message<br>Code: <b>$code</b>",
)

class ErrorApiCadastroColaborador(message: String?, val code: Int = 1003) : Exception(
    "$message <br>Code: <b>$code</b>"
)

class ErrorCadastroColaborador(message: String?, val code: Int = 1004) : Exception(
    "$message<br>Code: <b>$code</b>",
)