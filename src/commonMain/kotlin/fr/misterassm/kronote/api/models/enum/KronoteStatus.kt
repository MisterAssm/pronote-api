package fr.misterassm.kronote.api.models.enum

enum class KronoteStatus(val isInitialing: Boolean) {

    INITIALIZING(true),
    INITIALIZED(true),
    LOGGING_IN(true),
    CONNECTED(false),
    DISCONNECTED(false),
    FAILED_TO_LOGIN(false);

    fun isConnected() = equals(CONNECTED)

}