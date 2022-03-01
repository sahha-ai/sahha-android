package sdk.sahha.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class SahhaActivityRecognitionPermission : ComponentActivity() {
  @RequiresApi(Build.VERSION_CODES.Q)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestPermission()
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  fun requestPermission() {
    requestPermissions(
      arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
      1000
    )
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    for (i in grantResults.indices) {
      if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        finish()
      }
    }

    for (i in grantResults.indices) {
      if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
        if (requestCode == 1000) {
          Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
          finish()
        }
      }
    }
  }
}
