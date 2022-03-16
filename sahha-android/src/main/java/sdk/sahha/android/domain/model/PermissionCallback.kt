package sdk.sahha.android.domain.model

import sdk.sahha.android.domain.model.enums.PermissionStatus

class PermissionCallback {
    var unit = { permissionStatus: Enum<PermissionStatus> -> }
}