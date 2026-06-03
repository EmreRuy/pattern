package com.example.pattern.domain.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Staff-Level: Standardized Result wrapper for Data Layer operations.
 * Encapsulates Loading, Success, and Error states to decouple UI from exception handling.
 */
sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class Error(val exception: Throwable) : DataResult<Nothing>
    data object Loading : DataResult<Nothing>
}

/**
 * Extension to convert a standard Flow into a Result-wrapped Flow.
 */
fun <T> Flow<T>.asResult(): Flow<DataResult<T>> {
    return this
        .map<T, DataResult<T>> { DataResult.Success(it) }
        .onStart { emit(DataResult.Loading) }
        .catch { emit(DataResult.Error(it)) }
}

/**
 * Combines two Result-wrapped flows into a single Result-wrapped flow.
 */
fun <T1, T2, R> combineResults(
    f1: Flow<DataResult<T1>>,
    f2: Flow<DataResult<T2>>,
    transform: suspend (T1, T2) -> R
): Flow<DataResult<R>> = combine(f1, f2) { r1, r2 ->
    when {
        r1 is DataResult.Error -> DataResult.Error(r1.exception)
        r2 is DataResult.Error -> DataResult.Error(r2.exception)
        r1 is DataResult.Loading || r2 is DataResult.Loading -> DataResult.Loading
        r1 is DataResult.Success && r2 is DataResult.Success -> {
            DataResult.Success(transform(r1.data, r2.data))
        }
        else -> DataResult.Loading
    }
}

/**
 * Combines three Result-wrapped flows into a single Result-wrapped flow.
 */
fun <T1, T2, T3, R> combineResults(
    f1: Flow<DataResult<T1>>,
    f2: Flow<DataResult<T2>>,
    f3: Flow<DataResult<T3>>,
    transform: suspend (T1, T2, T3) -> R
): Flow<DataResult<R>> = combine(f1, f2, f3) { r1, r2, r3 ->
    when {
        r1 is DataResult.Error -> DataResult.Error(r1.exception)
        r2 is DataResult.Error -> DataResult.Error(r2.exception)
        r3 is DataResult.Error -> DataResult.Error(r3.exception)
        r1 is DataResult.Loading || r2 is DataResult.Loading || r3 is DataResult.Loading -> DataResult.Loading
        r1 is DataResult.Success && r2 is DataResult.Success && r3 is DataResult.Success -> {
            DataResult.Success(transform(r1.data, r2.data, r3.data))
        }
        else -> DataResult.Loading
    }
}

/**
 * Maps a DataResult of type T to a DataResult of type R.
 */
fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> {
    return when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Error -> DataResult.Error(exception)
        is DataResult.Loading -> DataResult.Loading
    }
}

/**
 * Extension to map a Flow<DataResult<T>> to a Flow<DataResult<R>>.
 */
fun <T, R> Flow<DataResult<T>>.mapResult(transform: (T) -> R): Flow<DataResult<R>> {
    return this.map { it.map(transform) }
}
