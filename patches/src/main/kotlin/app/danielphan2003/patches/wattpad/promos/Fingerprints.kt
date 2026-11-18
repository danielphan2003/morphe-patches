package app.danielphan2003.patches.wattpad.promos

import app.morphe.patcher.Fingerprint

internal object HomePremiumButtonFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/home/viewmodel/HomeScreenViewModel;",
    name = "getPremiumButton",
    returnType = "Lwp/clientplatform/cpcore/ViewResult;",
    parameters = emptyList(),
)

internal object ReaderBottomBarPremiumButtonsFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/reader/ui/views/ReaderBottomBar;",
    name = "setupPremiumButtons",
    returnType = "V",
    parameters = listOf("Z", "Z"),
)

internal object ResolveStartupPromptFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/home/usecase/ResolveSubscriptionPromptUseCase;",
    name = "access\$resolveStartUpPromptsLegacy",
    returnType = "Lwp/wattpad/subscription/SubscriptionPaywalls\$Config;",
    parameters = listOf("Lwp/wattpad/home/usecase/ResolveSubscriptionPromptUseCase;"),
)

internal object SubscriptionPaywallLauncherFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/subscription/SubscriptionPaywallLauncher;",
    name = "launch",
    returnType = "V",
    parameters = listOf(
        "Landroidx/fragment/app/FragmentActivity;",
        "Lwp/wattpad/subscription/SubscriptionPaywalls\$Config;",
    ),
)

internal object StoryDetailsBannerConfigFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/discover/storyinfo/viewmodel/StoryDetailsViewModel;",
    name = "getSubscriptionBannerConfig",
    returnType = "Landroidx/compose/runtime/snapshots/SnapshotStateMap;",
    parameters = emptyList(),
)
