package com.emilkrebs.watchlock.utils

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
fun authenticateBiometric(context: Context, title: String = "", subtitle: String = "", description: String = "",  onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
    val executor = ContextCompat.getMainExecutor(context)

    val biometricPrompt = BiometricPrompt.Builder(context)
        .setTitle(title)
        .setSubtitle(subtitle)
        .setDescription(description)
        .setNegativeButton("Cancel", executor) { _, _ ->
            onFailure()
        }
        .setConfirmationRequired(true)
        .build()

    val cancellationSignal = CancellationSignal()
    cancellationSignal.setOnCancelListener(onFailure)

    biometricPrompt.authenticate(cancellationSignal, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onFailure()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFailure()
        }
    })
}