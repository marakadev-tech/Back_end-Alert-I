package com.example.alerti_back.Model;

import java.util.Date;

public class Reponses {
    private int id;
    private String reponse1;
    private String reponse2;
    private String reponse3;
    private String estCorrect;
    private int question_id;

    private Date updatedAt;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getQuestion_id() {
        return question_id;
    }
    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }
    public String getReponse1() {
        return reponse1;
    }
    public void setReponse1(String reponse1) {
        this.reponse1 = reponse1;
    }
    public String getReponse2() {
        return reponse2;
    }
    public void setReponse2(String reponse2) {
        this.reponse2 = reponse2;
    }
    public String getReponse3() {
        return reponse3;
    }
    public void setReponse3(String reponse3) {
        this.reponse3 = reponse3;
    }
    public String getEstCorrect() {
        return estCorrect;
    }
    public void setEstCorrect(String estCorrect) {
        this.estCorrect = estCorrect;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
