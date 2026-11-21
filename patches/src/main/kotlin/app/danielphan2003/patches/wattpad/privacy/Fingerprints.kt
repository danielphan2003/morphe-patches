package app.danielphan2003.patches.wattpad.privacy

import app.morphe.patcher.Fingerprint

internal object ComScoreStartFingerprint : Fingerprint(
    definingClass = "Lcom/comscore/Analytics;",
    name = "start",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

internal object AudienceNetworkClassLoaderFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/ads/internal/dynamicloading/DynamicLoaderFactory;",
    name = "makeAdsSdkClassLoader",
    returnType = "Ljava/lang/ClassLoader;",
    parameters = listOf("Landroid/content/Context;"),
)

internal object ExternalAdManagerLaunchFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/ads/ExternalAdManager;",
    name = "onAppLaunch",
    returnType = "V",
    parameters = listOf("Landroid/app/Application;"),
)

internal object AppsFlyerLaunchFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/analytics/appsflyer/AppsFlyer;",
    name = "onAppLaunch",
    returnType = "V",
    parameters = emptyList(),
)

internal object AppsFlyerSubscriptionLaunchFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/subscription/AppsFlyerSubscriptionEventManager;",
    name = "onAppLaunch",
    returnType = "V",
    parameters = emptyList(),
)

internal object DatadogInitializeFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/datadog/DatadogProvider;",
    name = "initialize",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

internal object FacebookActivateAppFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/appevents/AppEventsLoggerImpl\$Companion;",
    name = "activateApp",
    returnType = "V",
    parameters = listOf("Landroid/app/Application;", "Ljava/lang/String;"),
)

internal object OkHttpBuilderBuildFingerprint : Fingerprint(
    definingClass = "Lokhttp3/OkHttpClient\$Builder;",
    name = "build",
    returnType = "Lokhttp3/OkHttpClient;",
    parameters = emptyList(),
)

internal object MaxMediationInitializeFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/adsx/MaxAdMediationInitializer;",
    name = "initialize",
    returnType = "V",
    parameters = listOf("Z", "L"),
)

internal object TapjoyConfigureFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/offerwall/TapjoyInitializer;",
    name = "configure",
    returnType = "V",
    parameters = listOf("Lorg/json/JSONObject;"),
)

internal object OptimizelyInitializeFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/optimizely/Optimizely;",
    name = "initializeSdkIfNecessary",
    returnType = "V",
    parameters = emptyList(),
)

internal object FacebookPrivacyLaunchFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/analytics/facebook/FacebookPrivacyManager;",
    name = "onAppLaunch",
    returnType = "V",
    parameters = emptyList(),
)

internal object SessionReplayKeepRecordingFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/sessionreplay/SessionReplayLifecycleObserver;",
    name = "keepRecording",
    returnType = "V",
    parameters = listOf("Ljava/lang/Object;"),
)

internal object FacebookLoadAppSettingsFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/internal/FetchedAppSettingsManager;",
    name = "loadAppSettingsAsync",
    returnType = "V",
    parameters = emptyList(),
)

internal object FacebookCheckFeatureFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/internal/FeatureManager;",
    name = "checkFeature",
    returnType = "V",
    parameters = listOf("Lcom/facebook/internal/FeatureManager\$Callback;", "Lcom/facebook/internal/FeatureManager\$Feature;"),
)

internal object AmplitudeSessionReplayConstructorFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/sessionreplay/AmplitudeSessionReplay;",
    name = "<init>",
    returnType = "V",
)

internal object AmplitudeSessionReplaySetSessionIdFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/sessionreplay/AmplitudeSessionReplay;",
    name = "setSessionId",
    returnType = "V",
    parameters = listOf("J"),
)

internal object AmplitudeSessionReplayStartFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/sessionreplay/AmplitudeSessionReplay;",
    name = "startRecording",
    returnType = "V",
    parameters = emptyList(),
)

internal object AmplitudeSessionReplayStopFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/sessionreplay/AmplitudeSessionReplay;",
    name = "stopRecording",
    returnType = "V",
    parameters = emptyList(),
)

internal object AmplitudeSessionReplayUnmaskFingerprint : Fingerprint(
    definingClass = "Lwp/wattpad/sessionreplay/AmplitudeSessionReplay;",
    name = "unmaskView",
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
)

internal object FacebookAutoLogAppEventsFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/UserSettingsManager;",
    name = "getAutoLogAppEventsEnabled",
    returnType = "Z",
    parameters = emptyList(),
)

internal object FacebookAdvertiserIdCollectionFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/UserSettingsManager;",
    name = "getAdvertiserIDCollectionEnabled",
    returnType = "Z",
    parameters = emptyList(),
)

internal object FacebookQueryGateKeepersFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/internal/FetchedAppGateKeepersManager;",
    name = "queryAppGateKeepers",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
)

internal object DataTransportSendFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/datatransport/cct/CctTransportBackend;",
    name = "send",
    returnType = "Lcom/google/android/datatransport/runtime/backends/BackendResponse;",
)

internal object RemoteConfigFetchAndActivateFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;",
    name = "fetchAndActivate",
    returnType = "Lcom/google/android/gms/tasks/Task;",
    parameters = emptyList(),
)

internal object FacebookQueryAppSettingsFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/internal/FetchedAppSettingsManager;",
    name = "queryAppSettings",
    returnType = "Lcom/facebook/internal/FetchedAppSettings;",
    parameters = listOf("Ljava/lang/String;", "Z"),
)

internal object FacebookEventQueueAddFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/appevents/AppEventQueue;",
    name = "add",
    returnType = "V",
    parameters = listOf("Lcom/facebook/appevents/AccessTokenAppIdPair;", "Lcom/facebook/appevents/AppEvent;"),
)

internal object FacebookEventQueueFlushFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/appevents/AppEventQueue;",
    name = "flush",
    returnType = "V",
    parameters = listOf("Lcom/facebook/appevents/FlushReason;"),
)

internal object FacebookEventQueueFlushAndWaitFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/appevents/AppEventQueue;",
    name = "flushAndWait",
    returnType = "V",
    parameters = listOf("Lcom/facebook/appevents/FlushReason;"),
)

internal object FacebookGateKeepersQueryResponseFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/internal/FetchedAppGateKeepersManager;",
    name = "getAppGateKeepersQueryResponse",
    returnType = "Lorg/json/JSONObject;",
    parameters = emptyList(),
)
