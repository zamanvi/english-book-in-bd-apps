package com.abmn.englishhub.Model;
public class LessonModel {
    private final int id;
    private final String title;
    private final String plan;
    private final String plan_id;
    private final String chapter_type;
    private final String chapter_id;
    private final Boolean status;
    private final String created_at;
    private final String updated_at;
    private final boolean isPremium;

    public LessonModel(int id, String title, String plan, String plan_id, String chapter_type, String chapter_id, Boolean status, String created_at, String updated_at) {
        this(id, title, plan, plan_id, chapter_type, chapter_id, status, created_at, updated_at, false);
    }

    public LessonModel(int id, String title, String plan, String plan_id, String chapter_type, String chapter_id, Boolean status, String created_at, String updated_at, boolean isPremium) {
        this.id = id;
        this.title = title;
        this.plan = plan;
        this.plan_id = plan_id;
        this.chapter_type = chapter_type;
        this.chapter_id = chapter_id;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.isPremium = isPremium;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPlan() {
        return plan;
    }

    public String getPlan_id() {
        return plan_id;
    }

    public String getChapter_type() {
        return chapter_type;
    }

    public String getChapter_id() {
        return chapter_id;
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

    public boolean isPremium() {
        return isPremium;
    }
}
