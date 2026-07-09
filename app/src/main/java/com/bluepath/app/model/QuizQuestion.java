package com.bluepath.app.model;

public class QuizQuestion {
    public final String id;
    public final String tier;
    public final String topic;
    public final String question;
    public final String[] options;
    public final int answerIndex;
    public final String explanation;

    public QuizQuestion(String id, String tier, String topic, String question, String[] options,
                        int answerIndex, String explanation) {
        this.id = id;
        this.tier = tier;
        this.topic = topic;
        this.question = question;
        this.options = options;
        this.answerIndex = answerIndex;
        this.explanation = explanation;
    }
}
