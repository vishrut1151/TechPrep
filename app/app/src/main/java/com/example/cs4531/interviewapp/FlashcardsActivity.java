package com.example.cs4531.interviewapp;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.HEAD;




/**
 * Once database is up and running, we can set functions to the getQuestion
 * button to retrieve a sample question from the database, and then put it in
 * the textview. The getAnswer should have the same functionality.
 */

public class FlashcardsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {




    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    public RestRequests requests; //our RestRequests class
    public String answerString; //the answer response
    private RetroFit retroFit = RetroFit.retro.create(RetroFit.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);
        mDrawerLayout= (DrawerLayout) findViewById(R.id.nav_drawer);
        mToggle = new ActionBarDrawerToggle(FlashcardsActivity.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView=(NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView tv = (TextView)findViewById(R.id.qAView); //Question view
        TextView answerView = (TextView)findViewById(R.id.answerView);
        requests = RestRequests.getInstance(getApplicationContext());
        tv.setText("");
        final Button answerButton = (Button)findViewById(R.id.getAnswer);
        getQuestion(tv);
    }

    /**
     * @author smatthys
     * @param item
     * This function allows the menu toggle button and other menu buttons
     * properly function when clicked.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * @author smatthys
     * @param item
     * This function takes a boolean value to transition between different activities.
     * It holds all the logic necessary for the navigation side bar.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_home){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_recordVideo){
            Intent intent = new Intent(this, RecordVideoActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_flashcards){
            Intent intent = new Intent(this, FlashcardsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_resources){
            Intent intent = new Intent(this, ResourcesActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_myAccount){
            Intent intent = new Intent(this, LogIn.class);
            startActivity(intent);
        }
        return false;
    }

    /**
     * getQuestion changes the name of the buttons, textViews, and updates the view with a new flashcard question.
     * @author Jaron
     * @param v the view
     */
    public void getQuestion(View v) {

        //String targetURL = "http://ukko.d.umn.edu:24559" + "/getFlashCard";


        //these are all views or buttons within the app
        final TextView tv = (TextView) findViewById(R.id.qAView);
        TextView answerView = (TextView) findViewById(R.id.answerView);
        answerView.setText(""); //Resets the answer field to default on click
        Button getQuestionButton = (Button)findViewById(R.id.get_question);
        getQuestionButton.setText(R.string.new_Question);

        final CheckBox flagQuestionBox = findViewById(R.id.flagQuestion);

        flagQuestionBox.setChecked(false);              //these two lines will be removed later maybe?
        flagQuestionBox.setClickable(true);


        final Button answerButton = (Button)findViewById(R.id.getAnswer);
            if(answerButton.getText().toString() == getString(R.string.hide_Answer)) {
                hideAnswer(v);
            }


        //retrofit code, talks with the node server using RetroFit.java interface
        Call<JsonArray> call = this.retroFit.getFlashCard();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                Log.d("getFlashCard", response.body().toString());


                if(!response.body().toString().contains("question")) {
                    showfailurewindow();    //add a popup that signals there was an error
                }

                else if(response.body().toString().contains("answer")) {

                    answerButton.setVisibility(View.VISIBLE);
                    int startQuestionIndex = response.body().toString().indexOf("question");
                    int endQuestionIndex = response.body().toString().indexOf("flagged");

                    int startAnswerIndex = response.body().toString().indexOf("answer");
                    int endAnswerIndex = response.body().toString().indexOf("question");

                    answerString = response.body().toString().substring(startAnswerIndex + 9, endAnswerIndex - 3);
                    Log.d("answerString:", answerString);

                    tv.setText(response.body().toString().substring(startQuestionIndex + 11, endQuestionIndex - 3));

                }
                else {
                    if(response.body().toString().contains("author")) {

                        answerButton.setVisibility(View.INVISIBLE);
                        int startQuestionIndex = response.body().toString().indexOf("question");
                        int endQuestionIndex = response.body().toString().indexOf("author");
                        tv.setText(response.body().toString().substring(startQuestionIndex + 11, endQuestionIndex - 3));
                    } else
                        {
                            answerButton.setVisibility(View.INVISIBLE);
                            int startQuestionIndex = response.body().toString().indexOf("question");
                            int endQuestionIndex = response.body().toString().indexOf("flagged");
                            tv.setText(response.body().toString().substring(startQuestionIndex + 11, endQuestionIndex - 3));
                         }
                    }


                //Check flag question checkbox if question is already flagged
                if(isFlagged(response.body().toString())) flagQuestionBox.setChecked(true);


                }




            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.d("getFlashCard", "FAILED", t);
            }
        });
    }

    private void showfailurewindow() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FlashcardsActivity.this);
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


    public void showAnswer(View v) {
        TextView answerView = (TextView)findViewById(R.id.answerView);
        answerView.setText(answerString);

        Button answerButton = (Button)findViewById(R.id.getAnswer);
        answerButton.setText(R.string.hide_Answer); //change button to hide Answer
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                hideAnswer(v);
            }
        });


    }

    public void flagQuestion(View v) {
        final TextView tv = (TextView) findViewById(R.id.qAView);
        CheckBox flagQuestionBox = findViewById(R.id.flagQuestion);           //for when we want to be able to uncheck the box


        String question = tv.getText().toString();
        Log.d("question", question);

        //retrofit code, talks with the node server using RetroFit.java interface


        if (flagQuestionBox.isChecked()) {


            Call<JsonArray> flag = this.retroFit.flagFlashCard(question);
            flag.enqueue(new Callback<JsonArray>() {
                @Override
                public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                    Log.d("flagFlashCard", response.body().getAsString());
                }

                @Override
                public void onFailure(Call<JsonArray> call, Throwable t) {
                    Log.d("flagFlashCard", "FAILED", t);
                }
            });

            } else {    // un flag the question

            Call<JsonArray> flag = this.retroFit.unFlagFlashCard(question);
                flag.enqueue(new Callback<JsonArray>() {
                    @Override
                    public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                        Log.d("unFlagFlashcard", response.body().toString());
                    }

                    @Override
                    public void onFailure(Call<JsonArray> call, Throwable t) {
                        Log.d("unFlagFlashcard", "FAILED", t);
                    }
                });

            }

        }


    public void hideAnswer(View v) {
        TextView answerView = (TextView)findViewById(R.id.answerView);
        answerView.setText("");

        Button answerButton = (Button)findViewById(R.id.getAnswer);
        answerButton.setText(R.string.Get_Answer); //change button to hide Answer
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                showAnswer(v);
            }
        });
    }
    public void showAllQuestion(View v)
    {
        Button button = (Button) findViewById(R.id.showAllQuestion);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(FlashcardsActivity.this, QuestionListActivity.class);
                startActivity(myIntent);


            }
        });
    }


    public boolean isFlagged(String flashcard) {
        int flaggedIndex = flashcard.indexOf("flagged");
        String flagged = flashcard.substring(flaggedIndex);
        Log.d("isFlagged", flagged);
        if(flagged.contains("flagged") && flagged.contains("true")) return true;
        else return false;
    }

}



