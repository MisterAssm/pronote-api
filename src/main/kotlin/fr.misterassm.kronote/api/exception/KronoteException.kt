package fr.misterassm.kronote.api.exception

class KronoteException(
    number: Int,
    text: String,
) : Exception("$number : $text")
