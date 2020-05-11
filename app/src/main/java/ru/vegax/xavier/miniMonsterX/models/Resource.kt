package ru.vegax.xavier.miniMonsterX.models

data class Resource<out T>(val status: Status, val data: T?, val error: Throwable?) {

    companion object Companion {
        fun <K> success(data: K) = Resource(Status.SUCCESS, data, null)

        fun <K> error(data: K?, error: Throwable) = Resource(Status.ERROR, data, error)

        fun <K> loading(data: K?) = Resource(Status.LOADING, data, null)
    }

    fun <T> setData(data: T?) = Resource(status, data, error)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Resource<*>) return false

        if (status != other.status) return false

        if (status == Status.SUCCESS) {
            return data?.equals(other.data) ?: (other.data == null)
        }

        return error?.equals(other.error) ?: (other.error == null)
    }

}