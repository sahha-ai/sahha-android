package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

internal class PhoneScreenOn : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    Toast.makeText(context, "Screen on detected", Toast.LENGTH_LONG).show()
  }
}
