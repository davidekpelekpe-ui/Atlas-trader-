package com.example.utils

import android.content.Context
import android.widget.Toast

object ErrorHandler {
    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.io.IOException -> "Network error. Please check your connection."
            is java.util.concurrent.TimeoutException -> "Request timed out. Please try again."
            else -> throwable.message ?: "An unknown error occurred."
        }
    }
}
