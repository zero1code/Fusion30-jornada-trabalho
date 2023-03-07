package br.com.fusiondms.core.model.jornadatrabalho

data class Colaborador(
    val idEmpresa: Long,
    val matricula: Long,
    val nome: String = "",
    val funcao: String,
    val image64: String? = null,
    val imagePath: String? = null
) {
}