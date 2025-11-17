package com.fakhry.pomodojo.commons.domain.state

sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()

    data class Error(val message: String, val code: Int) : DomainResult<Nothing>()
}
