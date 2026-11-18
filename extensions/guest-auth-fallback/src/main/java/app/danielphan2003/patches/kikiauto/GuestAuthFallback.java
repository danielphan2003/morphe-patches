package app.danielphan2003.patches.kikiauto;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Builds a local guest session for Kiki Auto, so it can be used without a Zalo login.
 * Kiki's classes are obfuscated and not on this module's classpath, so they are reached by reflection.
 */
@SuppressWarnings("unused")
public final class GuestAuthFallback {
    private static final String TAG = "GuestAuthFallback";
    private static final String GUEST_USER_ID = "guest";
    private static final String GUEST_TYPE = "guest";
    private static final String GUEST_AUTH_TOKEN = "local_guest_token";
    private static final String GUEST_DEVICE_TOKEN = "local_guest_device";

    private GuestAuthFallback() {
    }

    /**
     * Called by the "Skip Zalo login" patch.
     *
     * @return a {@code KSuccessResult} wrapping the guest device auth, or null if it could not be built.
     */
    public static Object getGuestAuthenInfo() {
        try {
            return buildGuestDeviceAuthResult();
        } catch (Throwable throwable) {
            Log.e(TAG, "getGuestAuthenInfo failed", throwable);
            return null;
        }
    }

    /**
     * Saves the guest session into Kiki's key value store.
     *
     * @param keyValueDb Kiki's key value store, with saveBoolValue and saveStrValue methods.
     */
    public static void persistGuestAuth(Object keyValueDb) {
        if (keyValueDb == null) {
            Log.w(TAG, "keyValueDb is null, cannot persist guest auth");
            return;
        }

        try {
            Object deviceAuth = buildGuestDeviceAuth();
            Log.d(TAG, "Built guest device auth for persistence");

            invokeMethod(
                    keyValueDb,
                    "saveBoolValue",
                    new Class<?>[]{String.class, boolean.class},
                    new Object[]{"anonymous_user_key", Boolean.TRUE}
            );
            Log.d(TAG, "Saved anonymous_user_key");

            String serialized = serializeDeviceAuth(deviceAuth);
            Log.d(TAG, "Serialized auth: " + serialized);

            invokeMethod(
                    keyValueDb,
                    "saveStrValue",
                    new Class<?>[]{String.class, String.class},
                    new Object[]{"authenticated_key", serialized}
            );
            Log.d(TAG, "Saved authenticated_key");
        } catch (Throwable throwable) {
            Log.e(TAG, "persistGuestAuth failed", throwable);
        }
    }

    private static Object buildGuestDeviceAuthResult() throws Exception {
        Object deviceAuth = buildGuestDeviceAuth();
        Class<?> successResultClass = Class.forName("ai.zalo.kiki.core.data.type.KSuccessResult");
        Constructor<?> constructor = successResultClass.getDeclaredConstructor(Object.class);
        return constructor.newInstance(deviceAuth);
    }

    /** Builds n3.d(n3.c(userId, token, false, true), deviceToken, 0, type). */
    private static Object buildGuestDeviceAuth() throws Exception {
        Class<?> userAuthClass = Class.forName("n3.c");
        Constructor<?> userAuthConstructor = userAuthClass.getDeclaredConstructor(
                String.class, String.class, boolean.class, boolean.class
        );
        userAuthConstructor.setAccessible(true);
        Object userAuth = userAuthConstructor.newInstance(
                GUEST_USER_ID, GUEST_AUTH_TOKEN, Boolean.valueOf(false), Boolean.valueOf(true)
        );

        Class<?> deviceAuthClass = Class.forName("n3.d");
        Constructor<?> deviceAuthConstructor = deviceAuthClass.getDeclaredConstructor(
                userAuthClass, String.class, int.class, String.class
        );
        deviceAuthConstructor.setAccessible(true);
        return deviceAuthConstructor.newInstance(
                userAuth, GUEST_DEVICE_TOKEN, Integer.valueOf(0), GUEST_TYPE
        );
    }

    /** Joins the session fields with "|", the format Kiki stores its session in. */
    private static String serializeDeviceAuth(Object deviceAuth) throws Exception {
        Object userAuth = getFieldValue(deviceAuth, "f18253a");
        String userId = String.valueOf(getFieldValue(userAuth, "f18249a"));
        String token = String.valueOf(getFieldValue(userAuth, "f18250b"));
        String deviceToken = String.valueOf(getFieldValue(deviceAuth, "f18254b"));
        String flags = String.valueOf(getFieldValue(deviceAuth, "f18255c"));
        String type = String.valueOf(getFieldValue(deviceAuth, "f18256d"));
        return userId + "|" + token + "|" + deviceToken + "|" + flags + "|" + type;
    }

    private static Object getFieldValue(Object target, String name) throws Exception {
        if (target == null) {
            return null;
        }
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Object invokeMethod(Object target, String name, Class<?>[] parameterTypes, Object[] args)
            throws Exception {
        Method method = findMethod(target.getClass(), name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Look in the superclass.
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Method findMethod(Class<?> type, String name, Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                // Fall back to non-public methods declared here.
            }
            try {
                return current.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                // Look in the superclass.
            }
        }
        throw new NoSuchMethodException(name + " in " + type.getName());
    }
}
