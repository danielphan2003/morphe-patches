package app.danielphan2003.patches.wattpad.reader

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

val keepReaderSizeSteadyPatch = bytecodePatch(
    name = "Keep reader size steady",
    description = "Keeps the system bars as they are while reading when the reading menu opens, " +
        "so paging mode does not lay out the whole part again every time the menu opens or closes.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)

    execute {
        /*
         * Opening the reading menu calls exitImmersiveMode(), which shows every system bar,
         * and closing it (also on every page turn) calls enterImmersiveMode(), which hides
         * the navigation bar again, and the status bar too unless "Display status bar" is on.
         * The reader window fits inside the system bars, so its height changes each time,
         * and paging mode answers any height change by discarding every laid out page and
         * paginating the part again: a loading page, then the same page with a line more or
         * less. Its only callers open the reading menu, so make exitImmersiveMode() reapply
         * the immersive state instead. The bars can still be revealed by swiping from the
         * screen edge; those transient bars draw over the reader without resizing it.
         */
        ExitImmersiveModeFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Lwp/wattpad/reader/utils/ReaderWindowConfiguration;->enterImmersiveMode()V
                return-void
            """,
        )
    }
}
