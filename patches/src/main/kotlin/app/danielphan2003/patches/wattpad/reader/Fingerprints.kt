package app.danielphan2003.patches.wattpad.reader

import app.morphe.patcher.Fingerprint

internal object EnqueuePageLoadFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/reader/readingmodes/paging/ReaderPageModeFragment;",
    name = "enqueuePageLoadIfNecessary",
    returnType = "V",
    parameters = emptyList(),
)

internal object ExitImmersiveModeFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/reader/utils/ReaderWindowConfiguration;",
    name = "exitImmersiveMode",
    returnType = "V",
    parameters = emptyList(),
)
