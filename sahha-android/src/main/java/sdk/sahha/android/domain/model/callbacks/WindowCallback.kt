package sdk.sahha.android.domain.model.callbacks

import android.content.Context
import android.os.Build
import android.view.*
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.source.SahhaSensorStatus

internal class WindowCallback(
    private val context: Context,
    private var localCallback: Window.Callback,
    private val activityCallback: ActivityCallback
) : Window.Callback {

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return localCallback.dispatchKeyEvent(event)
    }

    override fun dispatchKeyShortcutEvent(event: KeyEvent?): Boolean {
        return localCallback.dispatchKeyShortcutEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return localCallback.dispatchTouchEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent?): Boolean {
        return localCallback.dispatchTrackballEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        return localCallback.dispatchGenericMotionEvent(event)
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        return localCallback.dispatchPopulateAccessibilityEvent(event)
    }

    override fun onCreatePanelView(featureId: Int): View? {
        return localCallback.onCreatePanelView(featureId)
    }

    override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
        return localCallback.onCreatePanelMenu(featureId, menu)
    }

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
        return localCallback.onPreparePanel(featureId, view, menu)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        return localCallback.onMenuOpened(featureId, menu)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        return localCallback.onMenuItemSelected(featureId, item)
    }

    override fun onWindowAttributesChanged(attrs: WindowManager.LayoutParams?) {
        return localCallback.onWindowAttributesChanged(attrs)
    }

    override fun onContentChanged() {
        return localCallback.onContentChanged()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            activityCallback.setSettingOnResume?.let {
                val activityStatus = checkActivityRecognitionPermission()
                Sahha.di.permissionHandler.sensorStatus = activityStatus
                it(activityStatus)
            }
        }

        return localCallback.onWindowFocusChanged(hasFocus)
    }

    override fun onAttachedToWindow() {
        return localCallback.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        return localCallback.onDetachedFromWindow()
    }

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        return localCallback.onPanelClosed(featureId, menu)
    }

    override fun onSearchRequested(): Boolean {
        return localCallback.onSearchRequested()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onSearchRequested(searchEvent: SearchEvent?): Boolean {
        return localCallback.onSearchRequested(searchEvent)
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback?): ActionMode? {
        return localCallback.onWindowStartingActionMode(callback)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onWindowStartingActionMode(
        callback: ActionMode.Callback?,
        type: Int
    ): ActionMode? {
        return localCallback.onWindowStartingActionMode(callback, type)
    }

    override fun onActionModeStarted(mode: ActionMode?) {
        return localCallback.onActionModeStarted(mode)
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        return localCallback.onActionModeFinished(mode)
    }

    private fun checkActivityRecognitionPermission(): Enum<SahhaSensorStatus> {
        return SahhaPermissions.activityRecognitionGranted(context)
    }
}