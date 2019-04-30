package xyz.ismailnurudeen.numberfacts

data class Fact(
        val text: String,
        val number: String,
        val found: Boolean = true,
        val type: String
)