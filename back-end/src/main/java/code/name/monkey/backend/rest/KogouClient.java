package code.name.monkey.backend.rest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import code.name.monkey.backend.RetroConstants;
import code.name.monkey.backend.rest.service.KuGouApiService;

import java.io.File;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hemanths on 23/08/17.
 */

public class KogouClient {
    public static final String BASE_URL = RetroConstants.BASE_API_URL_KUGOU;

    private KuGouApiService apiService;

    public KogouClient(@NonNull Context context) {
        this(createDefaultOkHttpClientBuilder(context).build());
    }

    public KogouClient(@NonNull Call.Factory client) {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .callFactory(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        apiService = restAdapter.create(KuGouApiService.class);
    }

    @Nullable
    public static Cache createDefaultCache(Context context) {
        File cacheDir = new File(context.getCacheDir().getAbsolutePath(), "/okhttp-lastfm/");
        if (cacheDir.mkdirs() || cacheDir.isDirectory()) {
            return new Cache(cacheDir, 1024 * 1024 * 10);
        }
        return null;
    }

    public static Interceptor createCacheControlInterceptor() {
        return chain -> {
            Request modifiedRequest = chain.request().newBuilder()
                    .addHeader("Cache-Control", String.format("max-age=%d, max-stale=%d", 31536000, 31536000))
                    .build();
            return chain.proceed(modifiedRequest);
        };
    }

    public static OkHttpClient.Builder createDefaultOkHttpClientBuilder(Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .cache(createDefaultCache(context))
                .addInterceptor(createCacheControlInterceptor());
    }

    public KuGouApiService getApiService() {
        return apiService;
    }
}
