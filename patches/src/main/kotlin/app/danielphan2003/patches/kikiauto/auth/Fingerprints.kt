package app.danielphan2003.patches.kikiauto.auth

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

internal object GetAuthenInfoFingerprint : Fingerprint(
    returnType = "Lai/zalo/kiki/core/data/type/KResult;",
    parameters = emptyList(),
    filters = listOf(
        string("authenticated_key"),
        string("No authen info exists"),
    ),
)

internal object CheckNeedLoginCoroutineFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lgk/d;"),
    filters = listOf(
        methodCall(
            smali = "Lo3/a;->getAuthenInfo()Lai/zalo/kiki/core/data/type/KResult;",
        ),
        methodCall(
            smali = "Lai/zalo/kiki/core/app/authen/service/AuthenticateService;->checkNeedLogin(Ln3/d;Lgk/d;)Ljava/lang/Object;",
        ),
    ),
)

internal object AuthenticateServiceCheckNeedLoginFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ln3/d;", "Lgk/d;"),
    filters = listOf(
        methodCall(
            smali = "Lai/zalo/kiki/core/app/authen/service/AuthenticateAPI;->checkNeedLogin(Ljava/lang/String;Ljava/lang/String;Lgk/d;)Ljava/lang/Object;",
        ),
        string("code"),
        literal(-10),
    ),
)

internal object NlpAnalyzeTextAnonymousRetryFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Ljava/lang/String;",
        "Lai/zalo/kiki/core/app/authen/logic/AuthenticateUseCase;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Lai/zalo/kiki/core/app/view_contract/NLPStateListener;",
        "Lgk/d;",
    ),
    filters = listOf(
        methodCall(
            smali = "Lai/zalo/kiki/core/app/authen/logic/AuthenticateUseCase;->registerDevice(Lgk/d;)Ljava/lang/Object;",
        ),
        literal(-12),
    ),
)
