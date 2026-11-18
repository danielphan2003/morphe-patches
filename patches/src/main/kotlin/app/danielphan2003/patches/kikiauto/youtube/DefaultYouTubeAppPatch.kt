package app.danielphan2003.patches.kikiauto.youtube

import app.danielphan2003.patches.kikiauto.shared.Constants.COMPATIBILITY_KIKI_AUTO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch

val defaultYouTubeAppPatch = bytecodePatch(
    name = "Open YouTube in default app",
    description = "Opens YouTube links with Android's default app instead of forcing the stock YouTube package.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_KIKI_AUTO)

    execute {
        ViewIntentForPackageFingerprint.method.addInstructionsWithLabels(
            0,
            """
                const-string v0, "com.google.android.youtube"
                invoke-static { p0, v0 }, Lpk/i;->a(Ljava/lang/Object;Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :morphe_keep_package
                const-string p0, ""
                :morphe_keep_package
                nop
            """,
        )
    }
}
