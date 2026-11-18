package app.danielphan2003.patches.kikiauto.voice

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

internal object AsrAuthProviderFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    filters = listOf(
        methodCall(smali = "Lai/zalo/kiki/core/app/authen/logic/AuthenticateUseCase;->getAuthenInfo()Lai/zalo/kiki/core/data/type/KResult;"),
        methodCall(smali = "Lai/zalo/kiki/core/data/type/KSuccessResult;->getData()Ljava/lang/Object;"),
        methodCall(smali = "Lk6/b;-><init>(Ljava/lang/String;I)V"),
    ),
)

internal object QueryAsrFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "I",
        "I",
        "[B",
        "Ljava/lang/String;",
        "Landroid/location/Location;",
        "Lgk/d;",
    ),
    filters = listOf(
        methodCall(smali = "Lgn/f;->l(Lgk/f;Lok/p;Lgk/d;)Ljava/lang/Object;"),
    ),
)

internal object QueryAsrCoroutineFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(smali = "Lok/a;->invoke()Ljava/lang/Object;"),
        string("file."),
        string("MD5"),
        methodCall(smali = "Lokhttp3/ResponseBody;->string()Ljava/lang/String;"),
        string("No authentication"),
    ),
)

internal object AsrResultHandlerFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        string("vad_network_request_timeout"),
        string("network_error: "),
        string("network_return: "),
        string("vad finish (Unknow)"),
    ),
)

internal object ServerIssueVoiceCallbackFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    filters = listOf(
        literal(0x7f1100c1),
        methodCall(smali = "Lv2/c;->v(Lv2/c;I)V"),
    ),
)

internal object ServerIssueToastFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        literal(0x7f1100c1),
        methodCall(smali = "Lai/zalo/kiki/core/data/sharedutils/UtilsKt;->showToast(Landroid/content/Context;Ljava/lang/String;)V"),
    ),
)

internal object ServerIssueAssistantStateFingerprint : Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        literal(0x7f1100c1),
        methodCall(smali = "Lai/zalo/kiki/auto/specific/lifecycle_aware/AssistantStateView;->h(Ljava/lang/CharSequence;)V"),
    ),
)

internal object NotifyErrorFromOutsideFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("I", "Lgk/d;"),
    filters = listOf(
        methodCall(
            definingClass = "Lai/zalo/kiki/core/app/voice_tts/error_notify/ErrorNotifyUseCase;",
            name = "notifyVoice",
            parameters = listOf(
                "I",
                "Lai/zalo/kiki/core/app/directive_handler/contract/execute_services/PlayerService\$PlayerStateCallback;",
                "Lai/zalo/kiki/core/app/logging/actionlogv2/ActionLogV2;",
                "Lgk/d;",
            ),
            returnType = "Ljava/lang/Object;",
        ),
    ),
)

internal object AssistantStateViewOnErrorFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "I"),
    filters = listOf(
        string("real_time_error_event"),
        methodCall(smali = "Lai/zalo/kiki/auto/utils/z;->d(Ljava/lang/String;Lok/p;)V"),
    ),
)

internal object ErrorTextMappingFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
    filters = listOf(
        literal(0x7f1100b7),
        literal(0x7f1100c2),
    ),
)

internal object ActivateAssistantErrorFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(smali = "Lai/zalo/kiki/auto/ui/CarMainActivity;->Q()Lai/zalo/kiki/core/app/view_contract/AssistantContract\$Presenter;"),
        methodCall(
            definingClass = "Lai/zalo/kiki/core/app/view_contract/AssistantContract\$Presenter;",
            name = "notifyErrorFromOutside",
            parameters = listOf("I", "Lgk/d;"),
            returnType = "Ljava/lang/Object;",
        ),
    ),
)

internal object AssistantPresenterHandleErrorFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    filters = listOf(
        methodCall(smali = "Lai/zalo/kiki/core/data/type/KErrorResult;->getThrowable()Ljava/lang/Throwable;"),
        methodCall(smali = "Lai/zalo/kiki/core/data/type/KErrorResult;->getErrorCode()I"),
        methodCall(
            definingClass = "Lai/zalo/kiki/core/app/voice_tts/error_notify/ErrorNotifyUseCase;",
            name = "notifyVoice",
            parameters = listOf(
                "I",
                "Lai/zalo/kiki/core/app/directive_handler/contract/execute_services/PlayerService\$PlayerStateCallback;",
                "Lai/zalo/kiki/core/app/logging/actionlogv2/ActionLogV2;",
                "Lgk/d;",
            ),
            returnType = "Ljava/lang/Object;",
        ),
    ),
)

internal object KErrorResultConstructorFingerprint : Fingerprint(
    name = "<init>",
    returnType = "V",
    parameters = listOf("Ljava/lang/Throwable;", "I"),
    filters = listOf(
        string("throwable"),
        methodCall(smali = "Lpk/i;->f(Ljava/lang/Object;Ljava/lang/String;)V"),
    ),
)

internal object RawNetworkAudioResourceMapFingerprint : Fingerprint(
    filters = listOf(
        literal(0x7f100081),
        literal(0x7f10010f),
        literal(0x7f100082),
        literal(0x7f100110),
    ),
)
