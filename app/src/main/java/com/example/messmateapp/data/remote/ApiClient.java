package com.example.messmateapp.data.remote;

import android.content.Context;
import android.util.Log;

import com.example.messmateapp.utils.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    /* ================= BASE URL ================= */

    private static final String BASE_URL =
            "http://10.0.2.2:4000/api/";




    private static Retrofit retrofit;
    private static Retrofit authRetrofit;


    /* ================= BASIC CLIENT ================= */

    public static Retrofit getClient() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }

        return retrofit;
    }


    /* ================= AUTH CLIENT ================= */

    public static Retrofit getAuthClient(Context context) {

        if (authRetrofit == null) {

            /* ===== Logging (Debug Only) ===== */

            HttpLoggingInterceptor logging =
                    new HttpLoggingInterceptor();

            logging.setLevel(
                    HttpLoggingInterceptor.Level.BODY
            );


            OkHttpClient client =
                    new OkHttpClient.Builder()

                            // Timeout safety
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)

                            // Interceptors
                            .addInterceptor(new AuthInterceptor(context))
                            .addInterceptor(logging)

                            .build();


            authRetrofit =
                    new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(
                                    GsonConverterFactory.create()
                            )
                            .build();
        }

        return authRetrofit;
    }


    /* ================= TOKEN INTERCEPTOR ================= */

    static class AuthInterceptor implements Interceptor {

        private final Context context;

        AuthInterceptor(Context ctx) {
            context = ctx.getApplicationContext();
        }

        @Override
        public Response intercept(Chain chain)
                throws IOException {

            // ✅ Get token from SessionManager
            SessionManager sm =
                    new SessionManager(context);

            String token = sm.getToken();


            Request.Builder builder =
                    chain.request()
                            .newBuilder();


            if (token != null && !token.isEmpty()) {

                builder.addHeader(
                        "Authorization",
                        "Bearer " + token
                );

                Log.d("API_TOKEN", "✅ Token Sent");

            } else {

                Log.e("API_TOKEN", "❌ Token Missing!");
            }


            return chain.proceed(builder.build());
        }
    }
}
