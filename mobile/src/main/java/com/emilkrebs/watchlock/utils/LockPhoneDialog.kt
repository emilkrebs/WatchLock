package com.emilkrebs.watchlock.utils

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Context.DEVICE_POLICY_SERVICE
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.emilkrebs.watchlock.services.WatchCommunicationService


fun lockPhoneDialog(context: Context, reason: String = "") {
    if (WatchCommunicationService(context).isAdminActive()) {
        lockPhone(context)
    }
}


fun lockPhone(context: Context) {
    (context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager).lockNow()
}

class LockDialog : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContent {
            MainDialog()
        }
    }

    @Composable
    @Preview
    fun MainDialog() {
        var openDialog by remember { mutableStateOf(true) }

        if (openDialog) {
            AlertDialog(onDismissRequest = {
                openDialog = false
                lockPhone(this)
            }, title = { Text("This phone is about to be locked") }, text = {
                Text("This phone is about to be locked")
            }, confirmButton = {
                Button(onClick = {
                    authenticateBiometric(context = this,
                        fragmentActivity = this,
                        title = "Biometric Authentication",
                        subtitle = "This phone is about to be locked",
                        onSuccess = { },
                        onFailure = { lockPhone(context = this) })
                }) {
                    Text("Stay Unlocked")
                }
            }, dismissButton = {
                OutlinedButton(onClick = {
                    openDialog = false
                    lockPhone(this)
                }) {
                    Text("Dismiss")
                }
            })
        }

    }
}