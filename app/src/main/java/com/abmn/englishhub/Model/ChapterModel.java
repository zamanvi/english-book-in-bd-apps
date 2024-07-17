package com.abmn.englishhub.Model;

public class ChapterModel {
    private final int id;
    private final String book_id;
    private final String title;
    private final String slug;
    private final String status;
    private final String pageview;
    private final String book_title;

    public ChapterModel(int id, String book_id, String title, String slug, String status, String pageview, String book_title) {
        this.id = id;
        this.book_id = book_id;
        this.title = title;
        this.slug = slug;
        this.status = status;
        this.pageview = pageview;
        this.book_title = book_title;
    }

    public int getId() {
        return id;
    }

    public String getBook_id() {
        return book_id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getStatus() {
        return status;
    }

    public String getPageview() {
        return pageview;
    }

    public String getBook_title() {
        return book_title;
    }
}
