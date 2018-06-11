package com.google.android.libraries.remixer.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AndroidUtils {

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static float sDensity = 1;
    public static int sNavigationBarHeight;
    public static int sStatusBarHeight;
    public static Application APP;

    public static void init(Context context) {
        APP = (Application) context.getApplicationContext();
        sDensity = context.getResources().getDisplayMetrics().density;

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            sStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                sNavigationBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(sDensity * value);
    }

    public static int dp2(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.floor(sDensity * value);
    }

    public static float dpf2(float value) {
        if (value == 0) {
            return 0;
        }
        return sDensity * value;
    }

    static String md5(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(message.getBytes());
            byte messageDigest[] = digest.digest();
            char[] hex = encodeHex(messageDigest, DIGITS_LOWER);
            return new String(hex);
        } catch (NoSuchAlgorithmException e) {
            //igone
        }
        return "";
    }

    private static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }
}
