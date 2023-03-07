package br.com.fusiondms.core.model.jornadatrabalho

data class RegistroPonto(
    val id: Int = 0,
    val matricula: Long,
    val dataRegistro: Long,
    val registroEfetuado: Boolean
)
