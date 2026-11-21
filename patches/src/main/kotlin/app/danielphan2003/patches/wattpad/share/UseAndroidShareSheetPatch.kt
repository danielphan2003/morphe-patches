package app.danielphan2003.patches.wattpad.share

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.danielphan2003.patches.wattpad.shared.Constants.EXTENSION
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

private const val NATIVE_SHARE = "Lapp/danielphan2003/patches/wattpad/NativeShare;"
private const val SHAREABLE = "Lwp/wattpad/share/interfaces/Shareable;"
private const val SHARE_ACTION = "Lwp/wattpad/share/enums/ShareAction;"
private const val SHARE_MEDIUM = "Lwp/wattpad/share/enums/ShareMedium;"
private const val SHARE_CAMPAIGN = "Lwp/wattpad/share/enums/ShareCampaign;"

@Suppress("unused")
val useAndroidShareSheetPatch = bytecodePatch(
    name = "Use Android share sheet",
    description = "Opens Android's share sheet in place of Wattpad's own share grid, and leaves out the tracking parameters and username Wattpad adds to shared links.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)
    extendWith(EXTENSION)

    execute {
        /*
         * Every share button builds a ShareDialog and calls show() on it, and the dialog lists
         * Wattpad's own grid of apps. Give ShareDialog its own show() that sends the message
         * Wattpad uses for "another app" to Android's share sheet, and never shows the dialog.
         * Building that message can make a network request, so the extension builds it off the
         * main thread.
         */
        val shareDialog = ShareDialogOnCreateFingerprint.classDef
        val show = ImmutableMethod(
            shareDialog.type,
            "show",
            emptyList(),
            "V",
            AccessFlags.PUBLIC.value,
            null,
            null,
            MutableMethodImplementation(6),
        ).toMutable()

        show.addInstructions(
            0,
            """
                iget-object v0, p0, ${shareDialog.type}->shareable:$SHAREABLE
                iget-object v1, p0, ${shareDialog.type}->action:$SHARE_ACTION
                sget-object v2, $SHARE_MEDIUM->OTHER_APP:$SHARE_MEDIUM
                iget-object v3, p0, ${shareDialog.type}->campaign:$SHARE_CAMPAIGN
                invoke-static { p0, v0, v1, v2, v3 }, $NATIVE_SHARE->share(Landroid/app/Dialog;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                return-void
            """,
        )
        shareDialog.methods.add(show)
    }
}
