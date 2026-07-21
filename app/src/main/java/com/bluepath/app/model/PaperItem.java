package com.bluepath.app.model;

public class PaperItem {
    public final String id;
    public final String title;
    public final String authors;
    public final String year;
    public final String source;
    public final String url;
    public final String topic;
    public final String abstractText;
    public final String doi;

    public PaperItem(String id, String title, String authors, String year, String source,
                     String url, String topic, String abstractText, String doi) {
        this.id = id;
        this.title = title;
        this.authors = authors == null ? "" : authors;
        this.year = year == null ? "" : year;
        this.source = source == null ? "" : source;
        this.url = url == null ? "" : url;
        this.topic = topic == null || topic.trim().isEmpty() ? "해양교육" : topic;
        this.abstractText = abstractText == null ? "" : abstractText;
        this.doi = doi == null ? "" : doi;
    }
}
