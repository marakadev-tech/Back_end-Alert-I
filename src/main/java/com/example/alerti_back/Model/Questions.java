package com.example.alerti_back.Model;

import java.util.Date;
import java.util.List;

public class Questions {
    private int id;
    private String textequestion;
    private Date updatedAt;
    private int quiz_id;
    private List<Reponses> reponses;

    public int getId(){
        return id;
    }
    public String getTextequestion(){
        return textequestion;
    }
    public int getQuiz_id(){
        return quiz_id;
    }
    public void setQuiz_id(int quiz_id){
        this.quiz_id = quiz_id;
    }
    public void setTextequestion(String textequestion){
        this.textequestion = textequestion;
    }
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setId(int id){
        this.id = id;
    }
    public List<Reponses> getReponses() {
        return reponses;
    }

    public void setReponses(List<Reponses> reponses) {
        this.reponses = reponses;
    }
}
