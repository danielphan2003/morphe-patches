package app.danielphan2003.patches.kikiauto.youtube

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal object ViewIntentForPackageFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Landroid/content/Intent;",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
    filters = listOf(
        string("packageName"),
        string("android.intent.action.VIEW"),
        methodCall(smali = "Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z"),
        methodCall(smali = "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;"),
        methodCall(smali = "Landroid/content/Intent;->setData(Landroid/net/Uri;)Landroid/content/Intent;"),
        methodCall(smali = "Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;"),
        literal(268435456),
    ),
)
