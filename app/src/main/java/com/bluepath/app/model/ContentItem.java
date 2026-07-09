package com.bluepath.app.model;

public class ContentItem {
    public final String id;
    public final String title;
    public final String source;
    public final String url;
    public final String difficulty; // 하, 중, 상
    public final String requiredTier;
    public final String topic;
    public final String careerTag;
    public final int minutes;

    public ContentItem(String id, String title, String source, String url, String difficulty,
                       String requiredTier, String topic, String careerTag, int minutes) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.url = url;
        this.difficulty = difficulty;
        this.requiredTier = requiredTier;
        this.topic = topic;
        this.careerTag = careerTag;
        this.minutes = minutes;
    }
}
