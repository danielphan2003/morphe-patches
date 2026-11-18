package app.danielphan2003.patches.wattpad.theme

import app.morphe.patcher.Fingerprint

internal object CustomThemeEnabledFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/dev/DevelopmentManager;",
    name = "isCustomThemeEnabled",
    returnType = "Z",
    parameters = emptyList(),
)
