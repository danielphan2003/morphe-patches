package app.danielphan2003.patches.wattpad.reader

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.danielphan2003.patches.wattpad.shared.Constants.EXTENSION
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private const val READER_INSETS = "Lapp/danielphan2003/patches/wattpad/ReaderInsets;"

val keepReaderSizeSteadyPatch = bytecodePatch(
    name = "Keep reader size steady",
    description = "Keeps the reader the same size when the system bars appear or hide, " +
        "so paging mode does not lay out the whole part again when the reading menu opens or the app is resumed.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)
    extendWith(EXTENSION)

    execute {
        /*
         * The reader's root view fits the system windows, so it takes the visible system bars as
         * padding. The bars change when the reading menu opens or closes, and when the app comes
         * back from the background, and paging mode answers any height change by discarding every
         * laid out page and paginating the part again: a loading page, then the same page.
         * apply() runs once the reader's views exist, so give the root view insets that stay the
         * same while reading there: the status bar when "Display status bar" is on, and never the
         * navigation bar, which then draws over the reader's own bottom margin.
         */
        ReaderWindowConfigurationApplyFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lwp/wattpad/reader/utils/ReaderWindowConfiguration;->window:Landroid/view/Window;
                iget-object v1, p0, Lwp/wattpad/reader/utils/ReaderWindowConfiguration;->readingPreferences:Lwp/wattpad/reader/utils/ReadingPreferences;
                invoke-static { v0, v1 }, $READER_INSETS->keepReaderHeightSteady(Landroid/view/Window;Ljava/lang/Object;)V
            """,
        )
    }
}
