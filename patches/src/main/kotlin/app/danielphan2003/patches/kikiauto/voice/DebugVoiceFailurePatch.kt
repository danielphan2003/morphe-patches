package app.danielphan2003.patches.kikiauto.voice

import app.danielphan2003.patches.kikiauto.shared.Constants.COMPATIBILITY_KIKI_AUTO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod

private const val LOG_TAG = "KikiPatchVoice"

val debugVoiceFailurePatch = bytecodePatch(
    name = "Debug Kiki voice failures",
    description = "Logs Kiki voice ASR/auth/network failure details to logcat for diagnosing guest-mode voice requests.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_KIKI_AUTO)

    execute {
        patchAuthProvider()
        patchQueryAsr()
        patchQueryAsrCoroutine()
        patchServerIssueUi()
        patchRawNetworkAudioMap()
        patchNotifyErrorFromOutside()
        patchAssistantStateViewOnError()
        patchAssistantPresenterHandleError()
        patchErrorTextMapping()
        patchActivateAssistantError()
        patchKErrorResultConstructor()
        patchAnonymousAuthService()
    }
}

context(_: BytecodePatchContext)
private fun patchAuthProvider() {
    AsrAuthProviderFingerprint.method.apply {
        val authResultIndex = findMoveResultAfterMethod(
            "Lai/zalo/kiki/core/app/authen/logic/AuthenticateUseCase;",
            "getAuthenInfo",
            "Lai/zalo/kiki/core/data/type/KResult;",
        )
        addInstructionsWithLabels(
            authResultIndex + 1,
            """
                new-instance v1, Ljava/lang/StringBuilder;
                const-string v2, "auth-provider: authInfoClass="
                invoke-direct { v1, v2 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                if-eqz v0, :morphe_auth_provider_info_null
                invoke-virtual { v0 }, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                move-result-object v2
                invoke-virtual { v2 }, Ljava/lang/Class;->getName()Ljava/lang/String;
                move-result-object v2
                goto :morphe_auth_provider_info_append
                :morphe_auth_provider_info_null
                const-string v2, "null"
                :morphe_auth_provider_info_append
                invoke-virtual { v1, v2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { v1 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v1
                const-string v2, "$LOG_TAG"
                invoke-static { v2, v1 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )

        val returnIndex = implementation!!.instructions.indexOfLast {
            it.opcode == Opcode.RETURN_OBJECT && it is OneRegisterInstruction && it.registerA == 1
        }
        addInstructionsWithLabels(
            returnIndex,
            """
                if-eqz v1, :morphe_auth_provider_missing
                iget-object v0, v1, Lk6/b;->a:Ljava/lang/String;
                if-eqz v0, :morphe_auth_provider_zero_length
                invoke-virtual { v0 }, Ljava/lang/String;->length()I
                move-result v0
                goto :morphe_auth_provider_have_length
                :morphe_auth_provider_zero_length
                const/4 v0, 0x0
                :morphe_auth_provider_have_length
                iget v2, v1, Lk6/b;->b:I
                new-instance v0, Ljava/lang/StringBuilder;
                const-string v2, "auth-provider: token-present"
                invoke-direct { v0, v2 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                const-string v2, " deviceType="
                invoke-virtual { v0, v2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                iget v2, v1, Lk6/b;->b:I
                invoke-virtual { v0, v2 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                const-string v2, " tokenLength="
                invoke-virtual { v0, v2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                iget-object v2, v1, Lk6/b;->a:Ljava/lang/String;
                if-eqz v2, :morphe_auth_provider_append_zero
                invoke-virtual { v2 }, Ljava/lang/String;->length()I
                move-result v2
                goto :morphe_auth_provider_append_length
                :morphe_auth_provider_append_zero
                const/4 v2, 0x0
                :morphe_auth_provider_append_length
                invoke-virtual { v0, v2 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v0
                const-string v2, "$LOG_TAG"
                invoke-static { v2, v0 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                goto :morphe_auth_provider_done
                :morphe_auth_provider_missing
                const-string v0, "$LOG_TAG"
                const-string v2, "auth-provider: missing-auth"
                invoke-static { v0, v2 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                :morphe_auth_provider_done
                nop
            """,
        )
    }
}

context(_: BytecodePatchContext)
private fun patchQueryAsr() {
    QueryAsrFingerprint.method.addInstructionsWithLabels(
        0,
        """
            new-instance v0, Ljava/lang/StringBuilder;
            const-string v1, "query-asr: sampleRate="
            invoke-direct { v0, v1 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
            invoke-virtual { v0, p1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
            const-string v1, " channel="
            invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            invoke-virtual { v0, p2 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
            const-string v1, " bytes="
            invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            array-length v1, p3
            invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
            const-string v1, " ext="
            invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            invoke-virtual { v0, p4 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            const-string v1, " hasLocation="
            invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            if-eqz p5, :morphe_query_asr_no_location
            const/4 v1, 0x1
            goto :morphe_query_asr_location_ready
            :morphe_query_asr_no_location
            const/4 v1, 0x0
            :morphe_query_asr_location_ready
            invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;
            invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
            move-result-object v0
            const-string v1, "$LOG_TAG"
            invoke-static { v1, v0 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
        """,
    )
}

context(_: BytecodePatchContext)
private fun patchQueryAsrCoroutine() {
    QueryAsrCoroutineFingerprint.method.apply {
        val authMoveResultIndex = findMoveResultAfterMethod("Lok/a;", "invoke", "Ljava/lang/Object;")
        val authCastIndex = implementation!!.instructions.withIndex()
            .drop(authMoveResultIndex + 1)
            .first { it.value.opcode == Opcode.CHECK_CAST }
            .index
        addInstructionsWithLabels(
            authCastIndex + 1,
            """
                if-eqz v2, :morphe_query_asr_auth_null
                new-instance v3, Ljava/lang/StringBuilder;
                const-string v4, "query-asr: auth-provider returned tokenLength="
                invoke-direct { v3, v4 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                iget-object v4, v2, Lk6/b;->a:Ljava/lang/String;
                if-eqz v4, :morphe_query_asr_auth_zero_length
                invoke-virtual { v4 }, Ljava/lang/String;->length()I
                move-result v4
                goto :morphe_query_asr_auth_have_length
                :morphe_query_asr_auth_zero_length
                const/4 v4, 0x0
                :morphe_query_asr_auth_have_length
                invoke-virtual { v3, v4 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                const-string v4, " deviceType="
                invoke-virtual { v3, v4 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                iget v4, v2, Lk6/b;->b:I
                invoke-virtual { v3, v4 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                invoke-virtual { v3 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v3
                const-string v4, "$LOG_TAG"
                invoke-static { v4, v3 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                goto :morphe_query_asr_auth_logged
                :morphe_query_asr_auth_null
                const-string v3, "$LOG_TAG"
                const-string v4, "query-asr: auth-provider returned null"
                invoke-static { v3, v4 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
                :morphe_query_asr_auth_logged
                nop
            """,
        )

        val performIndex = findStringIndex("ignored")
        addInstructionsWithLabels(
            performIndex,
            """
                const-string v0, "$LOG_TAG"
                const-string v14, "query-asr: performASR start"
                invoke-static { v0, v14 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )

        val responseMoveResultIndex = findMoveResultAfterMethod(
            "Lai/zalo/kiki/core/vad_offline/network/vad/KikiVADNetworkAPI;",
            "performASR",
            "Ljava/lang/Object;",
        )
        addInstructionsWithLabels(
            responseMoveResultIndex + 1,
            """
                const-string v3, "$LOG_TAG"
                const-string v4, "query-asr: performASR response-body"
                invoke-static { v3, v4 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )

        val noAuthStringIndex = findStringIndex("No authentication")
        addInstructionsWithLabels(
            noAuthStringIndex,
            """
                const-string v3, "$LOG_TAG"
                const-string v4, "query-asr: throwing No authentication"
                invoke-static { v3, v4 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )
    }
}

context(_: BytecodePatchContext)
private fun patchAsrResultHandler() {
    AsrResultHandlerFingerprint.method.apply {
        val timeoutIndex = findStringIndex("vad_network_request_timeout")
        addInstructionsWithLabels(
            timeoutIndex,
            """
                const-string v7, "$LOG_TAG"
                const-string v8, "asr-result: null -> REQUEST_TIMEOUT"
                invoke-static { v7, v8 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )

        val errorCastIndex = implementation!!.instructions.withIndex()
            .first { it.value.opcode == Opcode.CHECK_CAST && it.value.referenceString() == "Lai/zalo/kiki/core/data/type/KErrorResult;" }
            .index
        addInstructionsWithLabels(
            errorCastIndex + 1,
            """
                new-instance v2, Ljava/lang/StringBuilder;
                const-string v3, "asr-result: error code="
                invoke-direct { v2, v3 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { p1 }, Lai/zalo/kiki/core/data/type/KErrorResult;->getErrorCode()I
                move-result v3
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                const-string v3, " throwable="
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p1 }, Lai/zalo/kiki/core/data/type/KErrorResult;->getThrowable()Ljava/lang/Throwable;
                move-result-object v3
                invoke-virtual { v3 }, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                move-result-object v7
                invoke-virtual { v7 }, Ljava/lang/Class;->getName()Ljava/lang/String;
                move-result-object v7
                invoke-virtual { v2, v7 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                const-string v7, " message="
                invoke-virtual { v2, v7 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { v3 }, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;
                move-result-object v3
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { v2 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v2
                const-string v3, "$LOG_TAG"
                invoke-static { v3, v2 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )

        val successDataIndex = implementation!!.instructions.withIndex()
            .filter { it.value.methodReference()?.toSmali() == "Lai/zalo/kiki/core/data/type/KSuccessResult;->getData()Ljava/lang/Object;" }
            .drop(1)
            .first()
            .index
        val successMoveResultIndex = implementation!!.instructions.withIndex()
            .drop(successDataIndex + 1)
            .first { it.value.opcode == Opcode.MOVE_RESULT_OBJECT }
            .index
        val resultReg = (implementation!!.instructions[successMoveResultIndex] as OneRegisterInstruction).registerA
        val insertIdx = successMoveResultIndex + 1
        val free1 = 1.takeIf { it != resultReg } ?: 2
        val free2 = 2.takeIf { it != resultReg && it != free1 } ?: 3
        if (localRegisterCount() <= maxOf(free1, free2)) return@apply
        addInstructionsWithLabels(
            insertIdx,
            """
                new-instance v$free1, Ljava/lang/StringBuilder;
                const-string v$free2, "asr-result: success dataClass="
                invoke-direct { v$free1, v$free2 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { v$resultReg }, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                move-result-object v$free2
                invoke-virtual { v$free2 }, Ljava/lang/Class;->getName()Ljava/lang/String;
                move-result-object v$free2
                invoke-virtual { v$free1, v$free2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { v$free1 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v$free1
                const-string v$free2, "$LOG_TAG"
                invoke-static { v$free2, v$free1 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )

        val unknownIndex = findStringIndex("vad finish (Unknow)")
        addInstructionsWithLabels(
            unknownIndex,
            """
                const-string v1, "$LOG_TAG"
                const-string v3, "asr-result: unknown KResult type"
                invoke-static { v1, v3 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )
    }
}

context(_: BytecodePatchContext)
private fun patchServerIssueUi() {
    ServerIssueVoiceCallbackFingerprint.method.addInstructionsWithLabels(
        0,
        """
            const-string v0, "$LOG_TAG"
            const-string v1, "server-issue-ui: path=v2/e"
            invoke-static { v0, v1 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
        """,
    )

    ServerIssueToastFingerprint.method.addInstructionsWithLabels(
        ServerIssueToastFingerprint.method.findMethodCallIndex(
            "Lai/zalo/kiki/core/data/sharedutils/UtilsKt;",
            "showToast",
            "V",
        ),
        """
            const-string v1, "$LOG_TAG"
            const-string v2, "server-issue-ui: path=r1/a1 toast"
            invoke-static { v1, v2 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
        """,
    )

    ServerIssueAssistantStateFingerprint.method.addInstructionsWithLabels(
        ServerIssueAssistantStateFingerprint.method.findMethodCallIndex(
            "Lai/zalo/kiki/auto/specific/lifecycle_aware/AssistantStateView;",
            "h",
            "V",
        ),
        """
            const-string v3, "$LOG_TAG"
            const-string v4, "server-issue-ui: path=p0/b AssistantStateView.h"
            invoke-static { v3, v4 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
        """,
    )
}

context(_: BytecodePatchContext)
private fun patchRawNetworkAudioMap() {
    RawNetworkAudioResourceMapFingerprint.method.addInstructionsWithLabels(
        0,
        """
            const-string v0, "$LOG_TAG"
            const-string v1, "resource-map: slow-network raw resource map reached"
            invoke-static { v0, v1 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
        """,
    )
}

private fun MutableMethod.findMoveResultAfterMethod(
    definingClass: String,
    name: String,
    returnType: String,
): Int {
    val callIndex = findMethodCallIndex(definingClass, name, returnType)
    return implementation!!.instructions.withIndex()
        .drop(callIndex + 1)
        .first { it.value.opcode == Opcode.MOVE_RESULT || it.value.opcode == Opcode.MOVE_RESULT_OBJECT || it.value.opcode == Opcode.MOVE_RESULT_WIDE }
        .index
}

private fun MutableMethod.findMethodCallIndex(
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

private fun MutableMethod.findStringIndex(value: String): Int = implementation!!.instructions.withIndex()
    .first { (_, instruction) ->
        val reference = (instruction as? ReferenceInstruction)?.reference
        reference is StringReference && reference.string == value
    }
    .index

private fun Instruction.methodReference(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference

private fun Instruction.referenceString(): String? =
    ((this as? ReferenceInstruction)?.reference)?.toString()

private fun MethodReference.toSmali(): String =
    "$definingClass->$name(${parameterTypes.joinToString("")})$returnType"

context(_: BytecodePatchContext)
private fun patchNotifyErrorFromOutside() {
    NotifyErrorFromOutsideFingerprint.method.apply {
        if (localRegisterCount() < 2) return@apply
        addInstructionsWithLabels(
            0,
            """
                new-instance v0, Ljava/lang/StringBuilder;
                const-string v1, "notify-error: code="
                invoke-direct { v0, v1 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { v0, p1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v0
                const-string v1, "$LOG_TAG"
                invoke-static { v1, v0 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )
    }
}

context(_: BytecodePatchContext)
private fun patchAssistantStateViewOnError() {
    AssistantStateViewOnErrorFingerprint.method.apply {
        if (localRegisterCount() < 2) return@apply
        addInstructionsWithLabels(
            0,
            """
                new-instance v0, Ljava/lang/StringBuilder;
                const-string v1, "asst-state: code="
                invoke-direct { v0, v1 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { v0, p2 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                const-string v1, " text="
                invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { v0, p1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v0
                const-string v1, "$LOG_TAG"
                invoke-static { v1, v0 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )
    }
}

context(_: BytecodePatchContext)
private fun patchAssistantPresenterHandleError() {
    AssistantPresenterHandleErrorFingerprint.method.apply {
        if (localRegisterCount() < 4) return@apply
        addInstructionsWithLabels(
            0,
            """
                new-instance v0, Ljava/lang/StringBuilder;
                const-string v1, "presenter-error: code="
                invoke-direct { v0, v1 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { p1 }, Lai/zalo/kiki/core/data/type/KErrorResult;->getErrorCode()I
                move-result v1
                invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                const-string v1, " throwable="
                invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p1 }, Lai/zalo/kiki/core/data/type/KErrorResult;->getThrowable()Ljava/lang/Throwable;
                move-result-object v1
                if-eqz v1, :morphe_presenter_error_no_throwable
                invoke-virtual { v1 }, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                move-result-object v2
                invoke-virtual { v2 }, Ljava/lang/Class;->getName()Ljava/lang/String;
                move-result-object v2
                invoke-virtual { v0, v2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                const-string v2, " message="
                invoke-virtual { v0, v2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { v1 }, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;
                move-result-object v2
                invoke-virtual { v0, v2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                goto :morphe_presenter_error_done_throwable
                :morphe_presenter_error_no_throwable
                const-string v1, "null"
                invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                :morphe_presenter_error_done_throwable
                invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v0
                const-string v1, "$LOG_TAG"
                invoke-static { v1, v0 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )
    }
}

context(_: BytecodePatchContext)
private fun patchErrorTextMapping() {
    ErrorTextMappingFingerprint.method.addEntryLog("error-map: init")
}

context(_: BytecodePatchContext)
private fun patchActivateAssistantError() {
    ActivateAssistantErrorFingerprint.method.addEntryLog("activate-error: callback invoked")
}

context(_: BytecodePatchContext)
private fun patchKErrorResultConstructor() {
    KErrorResultConstructorFingerprint.method.apply {
        val returnIndex = implementation!!.instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
        addInstructionsWithLabels(
            returnIndex,
            """
                const/16 p0, -0xa
                if-ne p2, p0, :morphe_kerror_init_done
                new-instance p0, Ljava/lang/StringBuilder;
                const-string p2, "kerror-init: code=-10 throwable="
                invoke-direct { p0, p2 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                if-eqz p1, :morphe_kerror_init_null_throwable
                invoke-virtual { p1 }, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                move-result-object p2
                invoke-virtual { p2 }, Ljava/lang/Class;->getName()Ljava/lang/String;
                move-result-object p2
                invoke-virtual { p0, p2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                const-string p2, " message="
                invoke-virtual { p0, p2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p1 }, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;
                move-result-object p2
                invoke-virtual { p0, p2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                goto :morphe_kerror_init_message_ready
                :morphe_kerror_init_null_throwable
                const-string p2, "null"
                invoke-virtual { p0, p2 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                :morphe_kerror_init_message_ready
                invoke-virtual { p0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object p0
                new-instance p1, Ljava/lang/Throwable;
                invoke-direct { p1 }, Ljava/lang/Throwable;-><init>()V
                const-string p2, "$LOG_TAG"
                invoke-static { p2, p0, p1 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
                :morphe_kerror_init_done
                nop
            """,
        )
    }
}

context(patchContext: BytecodePatchContext)
private fun patchAnonymousAuthService() {
    val authenticateService = patchContext.mutableClassDefBy(patchContext.classDefBy("Lp3/a;"))

    authenticateService.methods.first {
        it.name == "canRegisterAnonymousUser" &&
            it.returnType == "Ljava/lang/Object;" &&
            it.parameterTypes.map { type -> type.toString() } == listOf("I", "Lgk/d;")
    }.apply {
        addInstructionsWithLabels(
            0,
            """
                new-instance v0, Ljava/lang/StringBuilder;
                const-string v1, "anon-status: loginFailCount="
                invoke-direct { v0, v1 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { v0, p1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v0
                const-string v1, "$LOG_TAG"
                invoke-static { v1, v0 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )

        val responseStringIndex = findMoveResultAfterMethod(
            "Lokhttp3/ResponseBody;",
            "string",
            "Ljava/lang/String;",
        )
        addInstructionsWithLabels(
            responseStringIndex + 1,
            """
                new-instance v0, Ljava/lang/StringBuilder;
                const-string v1, "anon-status: raw="
                invoke-direct { v0, v1 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { v0, p1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v0
                const-string v1, "$LOG_TAG"
                invoke-static { v1, v0 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )
    }

    authenticateService.methods.first {
        it.name == "registerAnonymous" &&
            it.returnType == "Ljava/lang/Object;" &&
            it.parameterTypes.map { type -> type.toString() } == listOf("I", "Lgk/d;")
    }.apply {
        addInstructionsWithLabels(
            0,
            """
                new-instance v1, Ljava/lang/StringBuilder;
                const-string v2, "anon-register: loginFailCount="
                invoke-direct { v1, v2 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { v1, p1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                invoke-virtual { v1 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v1
                const-string v2, "$LOG_TAG"
                invoke-static { v2, v1 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )

        val daoCastIndex = implementation!!.instructions.withIndex()
            .first {
                it.value.opcode == Opcode.CHECK_CAST &&
                    it.value.referenceString() == "Lai/zalo/kiki/core/app/dao/AuthenticateDAO;"
            }
            .index
        val errorCodeMoveResultIndex = implementation!!.instructions.withIndex()
            .drop(daoCastIndex + 1)
            .first { it.value.opcode == Opcode.MOVE_RESULT }
            .index
        addInstructionsWithLabels(
            errorCodeMoveResultIndex + 1,
            """
                new-instance v2, Ljava/lang/StringBuilder;
                const-string v3, "anon-register: dao errorCode="
                invoke-direct { v2, v3 }, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
                invoke-virtual { v2, p1 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                const-string v3, " limit="
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p2 }, Lai/zalo/kiki/core/app/dao/AuthenticateDAO;->getLimitDevice()I
                move-result v3
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                const-string v3, " needActivate="
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p2 }, Lai/zalo/kiki/core/app/dao/AuthenticateDAO;->getNeedActivate()Z
                move-result v3
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;
                const-string v3, " stillActivate="
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p2 }, Lai/zalo/kiki/core/app/dao/AuthenticateDAO;->getStillActivate()Z
                move-result v3
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;
                const-string v3, " isNewUser="
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p2 }, Lai/zalo/kiki/core/app/dao/AuthenticateDAO;->isNewUser()Z
                move-result v3
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;
                const-string v3, " userIdLength="
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p2 }, Lai/zalo/kiki/core/app/dao/AuthenticateDAO;->getUserId()Ljava/lang/String;
                move-result-object v3
                if-eqz v3, :morphe_anon_register_user_id_zero
                invoke-virtual { v3 }, Ljava/lang/String;->length()I
                move-result v3
                goto :morphe_anon_register_user_id_ready
                :morphe_anon_register_user_id_zero
                const/4 v3, 0x0
                :morphe_anon_register_user_id_ready
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                const-string v3, " tokenLength="
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual { p2 }, Lai/zalo/kiki/core/app/dao/AuthenticateDAO;->getToken()Ljava/lang/String;
                move-result-object v3
                if-eqz v3, :morphe_anon_register_token_zero
                invoke-virtual { v3 }, Ljava/lang/String;->length()I
                move-result v3
                goto :morphe_anon_register_token_ready
                :morphe_anon_register_token_zero
                const/4 v3, 0x0
                :morphe_anon_register_token_ready
                invoke-virtual { v2, v3 }, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
                invoke-virtual { v2 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v2
                const-string v3, "$LOG_TAG"
                invoke-static { v3, v2 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
            """,
        )
    }
}

private fun MutableMethod.addEntryLog(message: String) {
    if (localRegisterCount() < 2) return

    addInstructionsWithLabels(
        0,
        """
            const-string v0, "$LOG_TAG"
            const-string v1, "$message"
            invoke-static { v0, v1 }, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
        """,
    )
}

private fun MutableMethod.localRegisterCount(): Int {
    val parameterRegisters = parameterTypes.sumOf { type ->
        when (type.toString()) {
            "D", "J" -> 2
            else -> 1
        }
    } + if (accessFlags and STATIC_ACCESS_FLAG == 0) 1 else 0

    return implementation!!.registerCount - parameterRegisters
}

private const val STATIC_ACCESS_FLAG = 0x8
