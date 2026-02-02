package com.abmn.englishhub.Model;

public class NoticeModel {
    private final int id;
    private final String title;
    private final String app;
    private final String body;
    private final String slug;

    public NoticeModel(int id, String title, String app, String body, String slug) {
        this.id = id;
        this.title = title;
        this.app = app;
        this.body = body;
        this.slug = slug;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getApp() {
        return app;
    }

    public String getBody() {
        return body;
    }

    public String getSlug() {
        return slug;
    }
}
