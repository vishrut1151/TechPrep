package com.example.cs4531.interviewapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface RetroFit
{

    @GET("getFlashCard")
    Call<JsonArray> getFlashCard();

    @POST("flagFlashCard")
    @FormUrlEncoded
    Call<JsonArray> flagFlashCard(@Field("question") String question);


    @GET("getAllQuestions")
    Call<JsonArray> getAllQuestions();

    @POST("unFlagFlashCard")
    @FormUrlEncoded
    Call<JsonArray> unFlagFlashCard(@Field("question") String question);



    Gson gson = new GsonBuilder()
        .setLenient()
        .create();

    //The below can be done outside the class

    public static final Retrofit retro = new Retrofit.Builder()

            .baseUrl("http://ukko.d.umn.edu:24559")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

}

