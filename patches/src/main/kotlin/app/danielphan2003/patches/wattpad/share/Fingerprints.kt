package app.danielphan2003.patches.wattpad.share

import app.morphe.patcher.Fingerprint

internal object ShareDialogOnCreateFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/share/ui/ShareDialog;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
