package fr.misterassm.kronote.api.exception

class KronoteException(
    private val number: Int,
    private val text: String,
) : Exception("$number : $text")
