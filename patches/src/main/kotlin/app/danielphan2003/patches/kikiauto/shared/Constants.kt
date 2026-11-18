package app.danielphan2003.patches.kikiauto.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY_KIKI_AUTO = Compatibility(
        name = "Kiki Auto",
        packageName = "ai.zalo.kiki.car",
        appIconColor = 0x1A73E8,
        targets = listOf(
            AppTarget(
                version = "26.04.01.02",
            ),
        ),
    )
}
