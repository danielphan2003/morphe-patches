package app.danielphan2003.patches.wattpad.ads

import app.morphe.patcher.Fingerprint

internal object AdEligibilityFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/adsx/models/AdEligibility;",
    name = "isEligible",
    returnType = "Z",
    parameters = emptyList(),
)

internal object ReadingPreferencesFragmentFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/ui/activities/settings/ReadingPreferencesActivity\$ReadingPreferencesFragmentInternal;",
    name = "onCreatePreferences",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
    returnType = "V",
)
