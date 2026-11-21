package app.danielphan2003.patches.wattpad.privacy

import app.danielphan2003.patches.wattpad.shared.Constants.COMPATIBILITY_WATTPAD
import app.danielphan2003.patches.wattpad.shared.Constants.EXTENSION
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import org.w3c.dom.Element

private const val NETWORK_ALLOWLIST = "Lapp/danielphan2003/patches/wattpad/NetworkAllowlist;"

/** Content providers that start ad networks and analytics SDKs as soon as the app process starts. */
private val SDK_INIT_PROVIDERS = setOf(
    "com.applovin.sdk.AppLovinInitProvider",
    "com.magnite.sdk.adsbase.MagniteInitProvider",
    "io.bidmachine.BidMachineInitProvider",
    "com.vungle.ads.VungleProvider",
    "com.facebook.ads.AudienceNetworkContentProvider",
    "com.ironsource.lifecycle.IronsourceLifecycleProvider",
    "com.ironsource.lifecycle.LevelPlayActivityLifecycleProvider",
    "com.mbridge.msdk.config.component.status.MBComponentLifecycleProvider",
    "com.datadog.android.rum.DdRumContentProvider",
)

/** androidx.startup initializers of ad and in-app messaging SDKs. */
private val SDK_STARTUP_INITIALIZERS = setOf(
    "com.unity3d.services.core.configuration.AdsSdkInitializer",
    "com.appcues.monitor.AppcuesInitializer",
)

/** Manifest switches read by Firebase and Google Analytics. */
private val COLLECTION_FLAGS = mapOf(
    "firebase_crashlytics_collection_enabled" to "false",
    "firebase_analytics_collection_deactivated" to "true",
    "firebase_analytics_collection_enabled" to "false",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_default_allow_ad_personalization_signals" to "false",
    "firebase_sessions_enabled" to "false",
)

private val disableSdkStartupPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as Element

            // Morphe's XML documents are not namespace aware, so use the prefixed attribute names.
            fun Element.androidName() = getAttribute("android:name")

            val providers = application.getElementsByTagName("provider")
            for (i in providers.length - 1 downTo 0) {
                val provider = providers.item(i) as Element
                if (provider.androidName() in SDK_INIT_PROVIDERS) {
                    provider.parentNode.removeChild(provider)
                }
            }

            val metaData = application.getElementsByTagName("meta-data")
            val existing = mutableMapOf<String, Element>()
            for (i in metaData.length - 1 downTo 0) {
                val entry = metaData.item(i) as Element
                val name = entry.androidName()
                if (name in SDK_STARTUP_INITIALIZERS) {
                    entry.parentNode.removeChild(entry)
                } else if (entry.parentNode == application) {
                    existing[name] = entry
                }
            }

            COLLECTION_FLAGS.forEach { (name, value) ->
                val entry = existing[name] ?: document.createElement("meta-data").also {
                    it.setAttribute("android:name", name)
                    application.appendChild(it)
                }
                entry.setAttribute("android:value", value)
            }
        }
    }
}

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry and ad SDKs",
    description = "Stops the ad networks and analytics, attribution, crash reporting and session tracking SDKs " +
        "from starting, and blocks every connection that reading, comments and signing in do not need. " +
        "Also lets Wattpad run with GrapheneOS's dynamic code loading via memory blocked.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WATTPAD)
    dependsOn(disableSdkStartupPatch)
    extendWith(EXTENSION)

    execute {
        /*
         * comScore's native library makes memory executable when it gets the app context. With
         * GrapheneOS's dynamic code loading via memory blocked, that throws inside native code and
         * aborts the app on launch, so never start comScore.
         */
        ComScoreStartFingerprint.method.addInstructions(0, "return-void")

        /*
         * Audience Network loads its own dex from memory. It already falls back when that fails,
         * so fail right away instead of asking the system to load code from memory.
         */
        AudienceNetworkClassLoaderFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Ljava/lang/UnsupportedOperationException;
                invoke-direct { v0 }, Ljava/lang/UnsupportedOperationException;-><init>()V
                throw v0
            """,
        )

        /*
         * Ad mediation and its networks, the offerwall, attribution, real user monitoring, session
         * replay, remote experiments, and Facebook app events, which FacebookPrivacyManager turns
         * back on at launch. Facebook login does not need any of these.
         */
        listOf(
            ExternalAdManagerLaunchFingerprint,
            MaxMediationInitializeFingerprint,
            TapjoyConfigureFingerprint,
            AppsFlyerLaunchFingerprint,
            AppsFlyerSubscriptionLaunchFingerprint,
            DatadogInitializeFingerprint,
            SessionReplayKeepRecordingFingerprint,
            OptimizelyInitializeFingerprint,
            FacebookPrivacyLaunchFingerprint,
            FacebookActivateAppFingerprint,
            // Facebook's SDK fetches app settings and feature gatekeepers whenever it starts.
            FacebookLoadAppSettingsFingerprint,
            FacebookCheckFeatureFingerprint,
            FacebookQueryGateKeepersFingerprint,
            // Wattpad also logs its own events to Facebook; drop them, and never upload stored ones.
            FacebookEventQueueAddFingerprint,
            FacebookEventQueueFlushFingerprint,
            FacebookEventQueueFlushAndWaitFingerprint,
            AmplitudeSessionReplaySetSessionIdFingerprint,
            AmplitudeSessionReplayStartFingerprint,
            AmplitudeSessionReplayStopFingerprint,
            AmplitudeSessionReplayUnmaskFingerprint,
        ).forEach { it.method.addInstructions(0, "return-void") }

        /*
         * Firebase's data transport uploads Play Billing library logs, Firebase Sessions, messaging
         * and Crashlytics events. Report every upload as failed so none leave the device.
         */
        DataTransportSendFingerprint.method.addInstructions(
            0,
            """
                invoke-static { }, Lcom/google/android/datatransport/runtime/backends/BackendResponse;->fatalError()Lcom/google/android/datatransport/runtime/backends/BackendResponse;
                move-result-object v0
                return-object v0
            """,
        )

        /*
         * Facebook's SDK also fetches its app settings straight away when a saved setting says the
         * advertiser ID may be collected. A failed fetch returns null, which every caller handles.
         */
        FacebookQueryAppSettingsFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """,
        )

        // The gatekeeper fetch also runs from a background executor; answer it with no gatekeepers.
        FacebookGateKeepersQueryResponseFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lorg/json/JSONObject;
                invoke-direct { v0 }, Lorg/json/JSONObject;-><init>()V
                return-object v0
            """,
        )

        // Remote feature flags: report a fetch that changed nothing, so the built-in defaults stay.
        RemoteConfigFetchAndActivateFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                invoke-static { v0 }, Lcom/google/android/gms/tasks/Tasks;->forResult(Ljava/lang/Object;)Lcom/google/android/gms/tasks/Task;
                move-result-object v0
                return-object v0
            """,
        )

        /*
         * Stock Wattpad saves "log app events" and "collect advertiser ID" as on in Facebook's own
         * settings, which outlive the manifest switches; report both as off.
         */
        listOf(
            FacebookAutoLogAppEventsFingerprint,
            FacebookAdvertiserIdCollectionFingerprint,
        ).forEach {
            it.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
        }

        /*
         * Several components get the session replay controller injected, and building its wrapper
         * creates Amplitude's session replay SDK, which fetches its remote config right away.
         * Return from the wrapper's constructor before the SDK is created; its methods are no-ops.
         */
        AmplitudeSessionReplayConstructorFingerprint.method.apply {
            val instructions = implementation!!.instructions.toList()
            val featureFlagIndex = instructions.indexOfFirst {
                it.opcode == Opcode.IPUT_OBJECT &&
                    ((it as ReferenceInstruction).reference as FieldReference).name == "featureFlag"
            }
            require(featureFlagIndex >= 0) { "Could not find the session replay feature flag" }
            addInstruction(featureFlagIndex + 1, "return-void")
        }

        /*
         * Wattpad's own tracking, and the SDKs that use the app's OkHttp, all go through clients
         * built by OkHttpClient.Builder.build(). Add an interceptor that only lets through the hosts
         * reading, comments and signing in need.
         */
        OkHttpBuilderBuildFingerprint.method.addInstructions(
            0,
            "invoke-static { p0 }, $NETWORK_ALLOWLIST->install(Lokhttp3/OkHttpClient\$Builder;)V",
        )
    }
}
