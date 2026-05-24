package com.leaderboard.api;

import com.leaderboard.model.AcbLoginRequest;
import com.leaderboard.model.AcbLoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.Map;

/**
 * Retrofit interface for ACB mobile banking endpoints.
 *
 * This is a best-effort translation of the PHP ACB client into Retrofit calls.
 */
public interface AcbApi {

    @POST("mb/v2/auth/tokens")
    Call<AcbLoginResponse> login(@Body AcbLoginRequest body);

    @POST("mb/v2/auth/refresh")
    Call<AcbLoginResponse> refresh(@Header("Authorization") String bearerToken);

    @GET("mb/legacy/ss/cs/bankservice/transfers/list/account-payment")
    Call<Map<String, Object>> getAccountDetail(@Header("Authorization") String bearerToken);

    @GET("mb/legacy/ss/cs/bankservice/saving/tx-history")
    Call<Map<String, Object>> getTransactions(
            @Header("Authorization") String bearerToken,
            @Query("maxRows") int maxRows,
            @Query("account") String accountNumber
    );

    @GET("mb/legacy/ss/cs/bankservice/saving/{account}/tx-history")
    Call<Map<String, Object>> getHistory(
            @Header("Authorization") String bearerToken,
            @Path("account") String accountNumber,
            @Query("transactionType") String transactionType,
            @Query("from") long from,
            @Query("to") long to,
            @Query("min") long min,
            @Query("max") long max,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("mb/legacy/ss/cs/bankservice/transfers/accounts/{account}")
    Call<Map<String, Object>> resolveAccountName(
            @Header("Authorization") String bearerToken,
            @Path("account") String beneficiaryAccount,
            @Query("bankCode") String bankCode,
            @Query("accountNumber") String ownAccount
    );
}
