package app.danielphan2003.patches.kikiauto.auth

import app.danielphan2003.patches.kikiauto.shared.Constants.COMPATIBILITY_KIKI_AUTO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS =
    "Lapp/danielphan2003/patches/kikiauto/GuestAuthFallback;"

val skipZaloLoginPatch = bytecodePatch(
    name = "Skip Zalo login",
    description = "Uses Kiki Auto's guest authentication fallback instead of requiring Zalo login before app use.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_KIKI_AUTO)
    extendWith("extensions/guest-auth-fallback.mpe")

    execute {
        GetAuthenInfoFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->getGuestAuthenInfo()Ljava/lang/Object;
                move-result-object v0
                if-eqz v0, :morphe_no_guest_auth
                check-cast v0, Lai/zalo/kiki/core/data/type/KResult;
                return-object v0
                :morphe_no_guest_auth
                nop
            """,
        )

        CheckNeedLoginCoroutineFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->getGuestAuthenInfo()Ljava/lang/Object;
                move-result-object v0
                if-eqz v0, :morphe_original_check_need_login
                new-instance v0, Lai/zalo/kiki/core/data/type/KSuccessResult;
                sget-object v1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                invoke-direct { v0, v1 }, Lai/zalo/kiki/core/data/type/KSuccessResult;-><init>(Ljava/lang/Object;)V
                return-object v0
                :morphe_original_check_need_login
                nop
            """,
        )

        AuthenticateServiceCheckNeedLoginFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->getGuestAuthenInfo()Ljava/lang/Object;
                move-result-object v0
                if-eqz v0, :morphe_original_auth_service_check_need_login
                new-instance v0, Lai/zalo/kiki/core/data/type/KSuccessResult;
                sget-object v1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                invoke-direct { v0, v1 }, Lai/zalo/kiki/core/data/type/KSuccessResult;-><init>(Ljava/lang/Object;)V
                return-object v0
                :morphe_original_auth_service_check_need_login
                nop
            """,
        )

        mutableClassDefBy(classDefBy("Lj3/a;")).methods.first {
            it.name == "analyzeText" &&
                it.returnType == "Ljava/lang/Object;" &&
                it.parameterTypes.map { type -> type.toString() } == listOf(
                    "Ljava/lang/String;",
                    "Lai/zalo/kiki/core/app/authen/logic/AuthenticateUseCase;",
                    "Ljava/lang/String;",
                    "Ljava/lang/String;",
                    "Lai/zalo/kiki/core/app/view_contract/NLPStateListener;",
                    "Lgk/d;",
                )
        }.apply {
            val registerDeviceIndex = findMethodCallIndex(
                definingClass = "Lai/zalo/kiki/core/app/authen/logic/AuthenticateUseCase;",
                name = "registerDevice",
                returnType = "Ljava/lang/Object;",
            )
            replaceInstruction(
                registerDeviceIndex,
                "invoke-interface { v11, v3, v15 }, Lai/zalo/kiki/core/app/authen/logic/AuthenticateUseCase;->registerAnonymousUser(ILgk/d;)Ljava/lang/Object;",
            )
            addInstructionsWithLabels(
                registerDeviceIndex,
                """
                    const/4 v3, 0x1
                """,
            )

            val errorCodeConstIndex = implementation!!.instructions.withIndex()
                .take(registerDeviceIndex)
                .last { (_, instruction) ->
                    instruction.opcode == Opcode.CONST_16 &&
                        (instruction as? NarrowLiteralInstruction)?.narrowLiteral == -12
                }
                .index

            addInstructionsWithLabels(
                errorCodeConstIndex + 1,
                """
                    if-eq v2, v3, :morphe_nlp_retry_auth_error
                    const/16 v3, -0xa
                    :morphe_nlp_retry_auth_error
                    nop
                """,
            )
        }
    }
}

private fun app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.findMethodCallIndex(
    definingClass: String,
    name: String,
    returnType: String,
): Int = implementation!!.instructions.withIndex()
    .first { (_, instruction) ->
        val reference = instruction.methodReference()
        reference?.definingClass == definingClass &&
            reference.name == name &&
            reference.returnType == returnType
    }
    .index

private fun Instruction.methodReference(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference
