package com.example.alerti_back.Model;




import java.util.Date;
import java.util.List;

public class Quiz {

    private int id;
    private String description;

    private Date updatedAt;
    private List<Questions> questions;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    public List<Questions> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Questions> reponses) {
        this.questions = reponses;
    }
}
