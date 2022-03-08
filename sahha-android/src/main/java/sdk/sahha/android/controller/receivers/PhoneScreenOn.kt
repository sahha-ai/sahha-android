package sdk.sahha.android.controller.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class PhoneScreenOn : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    Toast.makeText(context, "Screen on detected", Toast.LENGTH_LONG).show()
  }
}
