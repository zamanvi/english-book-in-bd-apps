package com.abmn.englishhub.Model;

public class WordChapterModel {
    private final int id;
    private final String title;
    private final String type;
    private final String image_path;
    private final Boolean status;
    private final String created_at;
    private final String updated_at;

    public WordChapterModel(int id, String title, String type, String image_path, Boolean status, String created_at, String updated_at) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.image_path = image_path;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getImage_path() {
        return image_path;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }
}