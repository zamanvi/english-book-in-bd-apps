package com.abmn.englishhub.Model;

public class ItemModel {
    private final int id;
    private final String slug;
    private final String chapter_id;
    private final String title;
    private final String pageview;
    private final String book_title;

    public ItemModel(int id, String slug, String chapter_id, String title, String pageview, String book_title) {
        this.id = id;
        this.slug = slug;
        this.chapter_id = chapter_id;
        this.title = title;
        this.pageview = pageview;
        this.book_title = book_title;
    }

    public int getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getChapter_id() {
        return chapter_id;
    }

    public String getTitle() {
        return title;
    }

    public String getPageview() {
        return pageview;
    }

    public String getBook_title() {
        return book_title;
    }
}
