package com.fakhry.pomodojo.shared.domain.states

sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()

    data class Error(val message: String, val code: Int) : DomainResult<Nothing>()
}
