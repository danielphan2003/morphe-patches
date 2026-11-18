package app.danielphan2003.patches.wattpad.reader

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22t
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/** How many laid out pages to keep ready after the one on screen. */
private const val PAGES_AHEAD = 3

val preloadReaderPagesPatch = bytecodePatch(
    name = "Preload reader pages",
    description = "Lays out the next pages in paging mode before they are reached, " +
        "so turning a page does not show a loading page.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)

    execute {
        /*
         * Paging mode lays out one page at a time, and only asks for the next page
         * when the last visible page is the last laid out one:
         *
         *     if (findLastVisibleItemPosition() == getItemCount() - 1) enqueue(After(lastPage))
         *
         * Each request re-fetches the part, lays it out in a hidden view and, when online,
         * waits on a polls request, so a page turned to before that finishes shows a
         * loading page. Rewrite the condition as
         *
         *     if (findLastVisibleItemPosition() + PAGES_AHEAD >= getItemCount() - 1)
         *
         * The fragment already calls this method after every model build, so the read
         * ahead continues by itself, and the pagination queue ignores a request it has
         * already seen, so no page is requested twice. The last visible position is not
         * read again after the comparison, so its register can be reused.
         */
        EnqueuePageLoadFingerprint.method.apply {
            val instructions = implementation!!.instructions.toList()

            val lastVisibleIndex = instructions.indexOfFirst {
                it.methodReference()?.name == "findLastVisibleItemPosition"
            }
            require(lastVisibleIndex >= 0) { "Could not find findLastVisibleItemPosition" }
            val lastVisibleRegister = instructions.moveResultRegisterAfter(lastVisibleIndex)

            val itemCountIndex = instructions.indexAfter(lastVisibleIndex) {
                it.methodReference()?.name == "getItemCount"
            }
            val lastIndexRegister = instructions.moveResultRegisterAfter(itemCountIndex)

            // add-int/lit8 vCount, vCount, -0x1 turns the item count into the last index.
            val lastIndexIndex = instructions.indexAfter(itemCountIndex) {
                it.opcode == Opcode.ADD_INT_LIT8 && (it as TwoRegisterInstruction).registerA == lastIndexRegister
            }

            val branchIndex = instructions.indexAfter(lastIndexIndex) { it.opcode == Opcode.IF_EQ }
            val branch = instructions[branchIndex] as BuilderInstruction22t
            require(branch.registerA == lastVisibleRegister && branch.registerB == lastIndexRegister) {
                "Unexpected comparison v${branch.registerA}, v${branch.registerB}"
            }

            // Replace the branch first: the insertion below shifts the indexes after it.
            replaceInstruction(
                branchIndex,
                BuilderInstruction22t(Opcode.IF_GE, lastVisibleRegister, lastIndexRegister, branch.target),
            )
            addInstruction(
                lastIndexIndex + 1,
                "add-int/lit8 v$lastVisibleRegister, v$lastVisibleRegister, $PAGES_AHEAD",
            )
        }
    }
}

private fun List<Instruction>.indexAfter(index: Int, predicate: (Instruction) -> Boolean) =
    withIndex().drop(index + 1).first { (_, instruction) -> predicate(instruction) }.index

private fun List<Instruction>.moveResultRegisterAfter(invokeIndex: Int): Int {
    val moveResult = this[invokeIndex + 1]
    require(moveResult.opcode == Opcode.MOVE_RESULT) {
        "Expected move-result after index $invokeIndex, found ${moveResult.opcode}"
    }
    return (moveResult as OneRegisterInstruction).registerA
}

private fun Instruction.methodReference(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference
