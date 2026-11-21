package app.danielphan2003.patches.wattpad.promos

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

val hidePremiumPromosPatch = bytecodePatch(
    name = "Hide premium promos",
    description = "Hides premium upsell buttons and banners, including the \"No ads\" button at the end of a part, and prevents the subscription paywall from being shown.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)

    execute {
        // Home screen "Try Premium" header button.
        HomePremiumButtonFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lwp/clientplatform/cpcore/ViewResult${'$'}Uninitialized;->INSTANCE:Lwp/clientplatform/cpcore/ViewResult${'$'}Uninitialized;
                return-object v0
            """,
        )

        // Reader bottom bar "No ads" and "Premium+" buttons.
        ReaderBottomBarPremiumButtonsFingerprint.method.addInstructions(
            0,
            """
                const/4 p1, 0x0
                const/4 p2, 0x0
            """,
        )

        // End of part footer "No ads" and "Premium+" buttons, shown whenever a click handler is set.
        PartEndFooterPremiumCtaFingerprint.method.addInstructions(0, "const/4 p1, 0x0")
        PartEndFooterPremiumPlusCtaFingerprint.method.addInstructions(0, "const/4 p1, 0x0")

        // Automatic startup / new user subscription paywall prompt.
        ResolveStartupPromptFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """,
        )

        // Any remaining subscription paywall launch (buttons, banners, ad free prompts).
        SubscriptionPaywallLauncherFingerprint.method.addInstructions(
            0,
            "return-void",
        )

        // Premium banners on story details.
        StoryDetailsBannerConfigFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Landroidx/compose/runtime/snapshots/SnapshotStateMap;
                invoke-direct { v0 }, Landroidx/compose/runtime/snapshots/SnapshotStateMap;-><init>()V
                return-object v0
            """,
        )
    }
}
