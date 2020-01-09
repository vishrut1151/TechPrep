package com.example.cs4531.interviewapp;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class QuestionListAdapter extends RecyclerView.Adapter<QuestionListAdapter.MyViewHolder>{

    private ArrayList<QuestionListModel> questionModelArrayList;
    private Context context;

    public QuestionListAdapter(Context context,ArrayList<QuestionListModel> questionModelArrayList)
    {
        this.context = context;
        this.questionModelArrayList = questionModelArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        QuestionListModel questionModel = questionModelArrayList.get(position);

        holder.tv_question_title.setText(questionModel.getQuestion_text());


    }

    @Override
    public int getItemCount() {
        return questionModelArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView tv_question_title;
        public MyViewHolder(View itemView) {
            super(itemView);

            tv_question_title = (TextView)itemView.findViewById(R.id.tv_question_text);
        }
    }
}
