package app.danielphan2003.patches.wattpad.theme

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

val unlockThemeColoursPatch = bytecodePatch(
    name = "Unlock theme colours",
    description = "Unlocks all theme colours in the Customize theme screen without a premium subscription.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)

    execute {
        /*
         * DevelopmentManager.isCustomThemeEnabled() is the developer switch for custom themes.
         * SubscriptionThemeActivity uses it both to skip locking the theme colour views and to
         * allow selecting a colour without a premium check.
         */
        CustomThemeEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
