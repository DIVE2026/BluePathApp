package com.bluepath.app.model;

public class ProgramItem {
    public final String id;
    public final String title;
    public final String target;
    public final String startDate;
    public final String endDate;
    public final String method;
    public final String topic;
    public final String description;
    public final String source;
    public final String applicationUrl;
    public final String applicationDeadline;
    public final int capacity;
    public final boolean waitlistAvailable;
    public final String timezone;

    public ProgramItem(String id, String title, String target, String startDate, String endDate,
                       String method, String topic, String description) {
        this(id, title, target, startDate, endDate, method, topic, description, "제공 데이터", "");
    }

    public ProgramItem(String id, String title, String target, String startDate, String endDate,
                       String method, String topic, String description, String source) {
        this(id, title, target, startDate, endDate, method, topic, description, source, "");
    }

    public ProgramItem(String id, String title, String target, String startDate, String endDate,
                       String method, String topic, String description, String source, String applicationUrl) {
        this(id, title, target, startDate, endDate, method, topic, description, source, applicationUrl,
                "", 0, false, "Asia/Seoul");
    }

    public ProgramItem(String id, String title, String target, String startDate, String endDate,
                       String method, String topic, String description, String source, String applicationUrl,
                       String applicationDeadline, int capacity, boolean waitlistAvailable, String timezone) {
        this.id = id;
        this.title = title;
        this.target = target;
        this.startDate = startDate;
        this.endDate = endDate;
        this.method = method;
        this.topic = topic;
        this.description = description;
        this.source = source == null ? "" : source;
        this.applicationUrl = applicationUrl == null ? "" : applicationUrl;
        this.applicationDeadline = applicationDeadline == null ? "" : applicationDeadline;
        this.capacity = Math.max(0, capacity);
        this.waitlistAvailable = waitlistAvailable;
        this.timezone = timezone == null || timezone.trim().isEmpty() ? "Asia/Seoul" : timezone;
    }
}
