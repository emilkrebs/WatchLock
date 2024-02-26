package com.emilkrebs.watchlock.utils

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

private lateinit var executor: Executor
private lateinit var biometricPrompt: BiometricPrompt
private lateinit var promptInfo: BiometricPrompt.PromptInfo

fun authenticateBiometric(
    context: Context,
    fragmentActivity: FragmentActivity,
    title: String = "",
    subtitle: String = "",
    onSuccess: () -> Unit = {},
    onFailure: () -> Unit = {}
) {
    if (!isAvailable(context)) {
        Toast.makeText(
            context, "Biometric authentication is not available on this device.", Toast.LENGTH_SHORT
        ).show()
        onSuccess()
        return
    }
    executor = ContextCompat.getMainExecutor(context)
    promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle(title)
        .setSubtitle(subtitle)
        .setConfirmationRequired(false)
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL).build()

    val executor = ContextCompat.getMainExecutor(context)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
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
    }
    biometricPrompt =  BiometricPrompt(fragmentActivity, executor, callback)
    biometricPrompt.authenticate(promptInfo)
}


fun isAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }
}