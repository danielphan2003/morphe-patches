package app.danielphan2003.patches.wattpad.offline

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

val unlimitedOfflineStoriesPatch = bytecodePatch(
    name = "Unlimited offline stories",
    description = "Removes the limit on the number of stories that can be saved for offline reading.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)

    execute {
        /*
         * OfflineStoryLimit.getNumOfflineStories() returns the maximum number of
         * stories that can be saved offline. It returns Integer.MAX_VALUE for
         * premium users and the (small) default limit for free users.
         * Returning Integer.MAX_VALUE unconditionally removes the limit.
         */
        OfflineStoryLimitFingerprint.method.addInstructions(
            0,
            """
                const v0, 0x7fffffff
                return v0
            """,
        )
    }
}
