package com.example.cs4531.interviewapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionListActivity extends AppCompatActivity
{
    private RetroFit retroFit = RetroFit.retro.create(RetroFit.class);

    private RecyclerView recyclerView;


    private String TAG = QuestionListActivity.class.getSimpleName().toUpperCase();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.question_list);

        recyclerView = (RecyclerView)findViewById(R.id.rv_question_list);


        //call API SERVICE HERE

        progressDialog = new ProgressDialog(QuestionListActivity.this);
        progressDialog.setMessage("Fetching list of questions...");
        progressDialog.setCancelable(false);

        progressDialog.show();

        Call<JsonArray> call = this.retroFit.getAllQuestions();
        call.enqueue(new Callback<JsonArray>()
        {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response)
            {


                Log.e(TAG,"response "+response);
                Log.e(TAG,"response raw "+response.raw());
                Log.e(TAG,"response code "+response.code());
                Log.e(TAG,"response message "+response.message());
                Log.e(TAG,"response body "+response.body());


                if(response.code() == 200)
                {

                    JsonArray jsonArray = response.body();

                    ArrayList<QuestionListModel> questionListModelArrayList = new ArrayList<QuestionListModel>();

                    for(int i=0;i<jsonArray.size();i++)
                    {
                        JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                        String _id = jsonObject.get("_id").getAsString();
                        String question = jsonObject.get("question").getAsString();
                        String flagged = jsonObject.get("flagged").getAsString();

                        QuestionListModel questionListModel = new QuestionListModel(_id,question,flagged);

                        questionListModelArrayList.add(questionListModel);
                    }

                    Log.e(TAG,"questionListModelArrayList size "+questionListModelArrayList.size());
                    QuestionListAdapter questionListAdapter = new QuestionListAdapter(QuestionListActivity.this,questionListModelArrayList);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setAdapter(questionListAdapter);

                    progressDialog.dismiss();




                    //SHOW QUESTION LIST I.E CALL ADAPTER;

                }
                else
                {
                    progressDialog.dismiss();
                    //SHOW ALERT
                    showAlertWindow();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.d("showAllQuestion", "FAILED", t);
                //SHOW ALERT
                showAlertWindow();

                progressDialog.dismiss();
            }

        });
    }


    private void showAlertWindow()
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(QuestionListActivity.this);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setMessage("something went wrong. Please try again later");

        AlertDialog dialog = dialogBuilder.create();

        dialogBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}

