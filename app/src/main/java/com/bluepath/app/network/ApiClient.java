package com.bluepath.app.network;

import com.bluepath.app.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static volatile BluePathApi service;

    private ApiClient() {}

    public static boolean isConfigured() {
        return BuildConfig.BLUEPATH_API_BASE_URL != null
                && !BuildConfig.BLUEPATH_API_BASE_URL.trim().isEmpty();
    }

    public static BluePathApi service() {
        if (service == null) {
            synchronized (ApiClient.class) {
                if (service == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(normalizeBaseUrl(BuildConfig.BLUEPATH_API_BASE_URL))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    service = retrofit.create(BluePathApi.class);
                }
            }
        }
        return service;
    }

    private static String normalizeBaseUrl(String value) {
        String base = value == null ? "" : value.trim();
        if (base.isEmpty()) base = "http://127.0.0.1/";
        if (!base.endsWith("/")) base += "/";
        return base;
    }
}
