package app.danielphan2003.patches.wattpad.offline

import app.morphe.patcher.Fingerprint

internal object OfflineStoryLimitFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/offline/OfflineStoryLimit;",
    name = "getNumOfflineStories",
    returnType = "I",
    parameters = emptyList(),
)
