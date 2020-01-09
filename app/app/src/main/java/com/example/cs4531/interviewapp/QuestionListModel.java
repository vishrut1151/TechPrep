package com.example.cs4531.interviewapp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class QuestionListModel {

    public QuestionListModel(String _id,String question,String flagged)
    {
        this._id = _id;
        this.question_text = question;
        this.flagged = flagged;
    }

    @Expose
    @SerializedName("_id")
    private String _id;

    @Expose
    @SerializedName("question")
    private String question_text;

    @Expose
    @SerializedName("flagged")
    private String flagged;

    public String getQuestion_text() {
        return question_text;
    }

    public void setQuestion_text(String question_text) {
        this.question_text = question_text;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFlagged() {
        return flagged;
    }

    public void setFlagged(String flagged) {
        this.flagged = flagged;
    }


}
