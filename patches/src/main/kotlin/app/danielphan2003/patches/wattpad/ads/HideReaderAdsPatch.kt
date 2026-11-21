package app.danielphan2003.patches.wattpad.ads

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

val hideReaderAdsPatch = bytecodePatch(
    name = "Hide reader ads",
    description = "Hides banner and interstitial ads displayed while reading stories and on story details, the empty sticky banner slot over the bottom of the page, the ad rows between comments, and the ad skips promo row.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)

    execute {
        /*
         * AdEligibility is the ad eligibility of a story for a given placement
         * (reader sticky banner, between parts, story details, interstitials).
         * Forcing isEligible() to false makes the app skip every ad placement
         * that is gated by the ad eligibility response.
         */
        AdEligibilityFingerprint.method.replaceInstruction(
            0,
            "const/4 v0, 0x0",
        )

        hideAdSkipsPreference()
        hideStickyAdSlot()

        // Comments list ad rows: an "Advertisement" label over an empty banner slot. Returning before
        // the composable starts its group draws nothing and keeps the composition balanced.
        CommentsInlineAdFingerprint.method.addInstructions(0, "return-void")
    }
}

/**
 * The reader shows the Compose sticky banner slot when it opens, painted in the page colour,
 * and only hides it when it is told to hide the ad. Without ads that never happens, so the
 * empty slot stays on top of the bottom of every page and covers the last line of text.
 * The slot is painted in updateBannerAdBackgroundColorScheme(), so hide it there instead.
 */
context(_: BytecodePatchContext)
private fun hideStickyAdSlot() {
    UpdateBannerAdBackgroundColorSchemeFingerprint.method.apply {
        val instructions = implementation!!.instructions

        val slotIndex = instructions.indexOfFirst { instruction ->
            instruction.opcode == Opcode.IGET_OBJECT &&
                ((instruction as ReferenceInstruction).reference as FieldReference).name == "readerStickyAdComposeView"
        }
        require(slotIndex >= 0) { "Could not find the sticky ad slot" }

        val setColourIndex = instructions.withIndex()
            .drop(slotIndex + 1)
            .first { (_, instruction) -> instruction.methodReference()?.name == "setBackgroundColor" }
            .index
        val setColour = instructions[setColourIndex] as FiveRegisterInstruction
        val slotRegister = setColour.registerC
        val colourRegister = setColour.registerD

        // The colour is not used after this call, so its register can hold View.GONE.
        replaceInstruction(
            setColourIndex,
            "invoke-virtual { v$slotRegister, v$colourRegister }, Landroid/view/View;->setVisibility(I)V",
        )
        addInstruction(setColourIndex, "const/16 v$colourRegister, 0x8")
    }
}

/**
 * The "Ad Skips Preference" row is only useful when ads are shown, so keep it hidden
 * by forcing its feature flag to false while the reading preferences are created.
 */
context(_: BytecodePatchContext)
private fun hideAdSkipsPreference() {
    ReadingPreferencesFragmentFingerprint.method.apply {
        val instructions = implementation!!.instructions

        val adSkipsStringIndex = instructions.indexOfFirst { instruction ->
            val reference = (instruction as? ReferenceInstruction)?.reference
            reference is StringReference && reference.string == "ad_skips"
        }
        require(adSkipsStringIndex >= 0) { "Could not find the ad_skips preference" }

        val booleanValueIndex = instructions.withIndex()
            .drop(adSkipsStringIndex + 1)
            .first { (_, instruction) ->
                val reference = instruction.methodReference()
                reference?.definingClass == "Ljava/lang/Boolean;" && reference.name == "booleanValue"
            }
            .index

        val moveResultIndex = instructions.withIndex()
            .drop(booleanValueIndex + 1)
            .first { (_, instruction) -> instruction.opcode == Opcode.MOVE_RESULT }
            .index

        val register = (instructions[moveResultIndex] as OneRegisterInstruction).registerA
        addInstructions(moveResultIndex + 1, "const/4 v$register, 0x0")
    }
}

private fun Instruction.methodReference(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference
