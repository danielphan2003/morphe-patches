package app.danielphan2003.patches.wattpad.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    /** Java helpers used by the Wattpad patches, built from extensions/wattpad. */
    const val EXTENSION = "extensions/wattpad.mpe"

    val COMPATIBILITY_WATTPAD = Compatibility(
        name = "Wattpad",
        packageName = "wp.wattpad",
        description = "Read and write stories from the Wattpad community.",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFF500A,
        signatures = setOf(
            "012de45837d20561255a02bbc22334c1f681d01b366fe31bef418baf535903ef",
        ),
        targets = listOf(
            AppTarget(version = "11.40.5", versionCode = 3823),
        ),
    )
}
