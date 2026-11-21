package app.danielphan2003.patches.wattpad;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/** Used by the "Disable telemetry and ad SDKs" patch. */
@SuppressWarnings("unused")
public final class NetworkAllowlist implements Interceptor {
    private static final NetworkAllowlist INSTANCE = new NetworkAllowlist();

    /** Wattpad hosts that only serve tracking or ads. */
    private static final String[] BLOCKED_WATTPAD_HOSTS = {
        "track.wattpad.com",
        "kevel.wattpad.com",
        "adm.wattpad.com",
    };

    /** Hosts outside wattpad.com that reading, comments or signing in need. */
    private static final String[] ALLOWED_HOSTS = {
        "w.tt",
        // Video headers and embeds inside stories.
        "youtube.com",
        "youtube-nocookie.com",
        "ytimg.com",
        "googlevideo.com",
        // Signing in with Google.
        "accounts.google.com",
        "oauth2.googleapis.com",
        "www.googleapis.com",
        // Google Play billing for an existing subscription.
        "play.google.com",
    };

    private NetworkAllowlist() {
    }

    /**
     * Called with every OkHttpClient.Builder right before it builds a client. The interceptor runs
     * as an application interceptor, before anything else touches the request, and as a network
     * interceptor, so redirects OkHttp follows on its own are checked too.
     */
    public static void install(OkHttpClient.Builder builder) {
        if (!contains(builder, "interceptors")) {
            builder.addInterceptor(INSTANCE);
        }
        if (!contains(builder, "networkInterceptors")) {
            builder.addNetworkInterceptor(INSTANCE);
        }
    }

    /**
     * Builders copied from a client already hold the interceptor. Wattpad's OkHttp has no
     * interceptors() methods left, so look at the fields that keep them.
     */
    private static boolean contains(OkHttpClient.Builder builder, String fieldName) {
        try {
            java.lang.reflect.Field field = OkHttpClient.Builder.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return ((java.util.List<?>) field.get(builder)).contains(INSTANCE);
        } catch (Throwable ignored) {
            // Adding it twice is harmless.
            return false;
        }
    }

    public static boolean isAllowed(String host) {
        if (host == null) {
            return false;
        }
        host = host.toLowerCase(Locale.ROOT);
        for (String blocked : BLOCKED_WATTPAD_HOSTS) {
            if (host.equals(blocked)) {
                return false;
            }
        }
        if (matches(host, "wattpad.com")) {
            return true;
        }
        for (String allowed : ALLOWED_HOSTS) {
            if (matches(host, allowed)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String host = chain.request().url().host();
        if (!isAllowed(host)) {
            throw new IOException("Blocked by Morphe: " + host);
        }
        return chain.proceed(chain.request());
    }
}
