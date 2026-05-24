package com.leaderboard.service;

import com.leaderboard.api.AcbApi;
import com.leaderboard.model.AcbLoginRequest;
import com.leaderboard.model.AcbLoginResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Minimal Retrofit-backed client for the ACB mobile banking endpoints.
 * <p>
 * This is a port of the provided PHP implementation.
 */
public class AcbClient {
    private static final String BASE_URL = "https://apiapp.acb.com.vn/";
    private static final String DEFAULT_CLIENT_ID = "iuSuHYVufIUuNIREV0FB9EoLn9kHsDbm";
    private static final int DEFAULT_MAX_RETRIES = 3;

    // Headers that mimic the mobile app client.
    private static final String USER_AGENT = "ACB-MBA/1 CFNetwork/1128.0.1 Darwin/19.6.0";
    private static final String ACCEPT = "application/json";
    private static final String ACCEPT_LANGUAGE = "vi";
    private static final String ACCEPT_ENCODING = "gzip, deflate, br";

    private final AcbApi api;
    private final String clientId;
    private final int maxRetries;

    private String username;
    private String password;
    private String accountNumber;
    private String accessToken;
    private String refreshToken;

    public AcbClient() {
        this(DEFAULT_CLIENT_ID, DEFAULT_MAX_RETRIES);
    }

    public AcbClient(String clientId, int maxRetries) {
        this.clientId = clientId;
        this.maxRetries = maxRetries;

        Gson gson = new GsonBuilder().setLenient().create();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient ok = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", USER_AGENT)
                            .header("Accept", ACCEPT)
                            .header("Accept-Language", ACCEPT_LANGUAGE)
                            .header("Accept-Encoding", ACCEPT_ENCODING)
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(ok)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.api = retrofit.create(AcbApi.class);
    }

    public void setCredentials(String username, String password, String accountNumber) {
        this.username = username;
        this.password = password;
        this.accountNumber = accountNumber;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean login() throws IOException {
        AcbLoginRequest request = new AcbLoginRequest(username, password, clientId);
        Response<AcbLoginResponse> response = api.login(request).execute();

        if (!response.isSuccessful() || response.body() == null) {
            return false;
        }

        AcbLoginResponse body = response.body();
        this.accessToken = body.getAccessToken();
        this.refreshToken = body.getRefreshToken();

        // Optional: eagerly refresh the access token if ACB returns a refresh token.
        if (this.refreshToken != null && !this.refreshToken.isBlank()) {
            refreshToken();
        }

        return accessToken != null && !accessToken.isBlank();
    }

    public boolean refreshToken() throws IOException {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }

        Response<AcbLoginResponse> response = api.refresh("Bearer " + refreshToken).execute();
        if (!response.isSuccessful() || response.body() == null) {
            return false;
        }

        AcbLoginResponse body = response.body();
        this.accessToken = body.getAccessToken();
        if (body.getRefreshToken() != null && !body.getRefreshToken().isBlank()) {
            this.refreshToken = body.getRefreshToken();
        }
        return this.accessToken != null && !this.accessToken.isBlank();
    }

    public Map<String, Object> getAccountDetail() throws IOException {
        return executeWithRetry(() -> api.getAccountDetail(getBearer()));
    }

    public Map<String, Object> getTransactions(int limit) throws IOException {
        return executeWithRetry(() -> api.getTransactions(getBearer(), limit, accountNumber));
    }

    public Map<String, Object> getHistories(long fromMillis, long toMillis, int page, int size) throws IOException {
        return executeWithRetry(() -> api.getHistory(
                getBearer(),
                accountNumber,
                "ALL",
                fromMillis,
                toMillis,
                0L,
                9_007_199_254_740_991L,
                page,
                size
        ));
    }

    public Map<String, Object> resolveAccountName(String beneficiaryAccount, String bankCode) throws IOException {
        return executeWithRetry(() -> api.resolveAccountName(getBearer(), beneficiaryAccount, bankCode, accountNumber));
    }

    private <T> T executeWithRetry(Supplier<Call<T>> callSupplier) throws IOException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Call<T> call = callSupplier.get();
                Response<T> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }

                // If unauthorized, attempt refresh/login and retry.
                if (response.code() == 401) {
                    if (!tryRefreshOrLogin()) {
                        break;
                    }
                    continue;
                }

                // Other failures: return empty map to match PHP behavior.
                return (T) Collections.emptyMap();
            } catch (IOException e) {
                lastException = e;
                // Retry after login.
                tryRefreshOrLogin();
            }
        }

        if (lastException != null) {
            throw lastException;
        }

        return (T) Collections.emptyMap();
    }

    private boolean tryRefreshOrLogin() {
        try {
            if (refreshToken != null && !refreshToken.isBlank()) {
                return refreshToken();
            }

            return login();
        } catch (IOException e) {
            return false;
        }
    }

    private String getBearer() {
        return "Bearer " + (accessToken == null ? "" : accessToken.trim());
    }
}
