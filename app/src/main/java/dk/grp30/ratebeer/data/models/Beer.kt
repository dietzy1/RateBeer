package dk.grp30.ratebeer.data.models

data class Beer(
    val id: String,
    val name: String,
    val brewery: String,
    val style: String,
    val abv: Double,
    val rating: Double,
    val imageUrl: String
)