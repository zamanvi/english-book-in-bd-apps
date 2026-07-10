package com.abmn.englishhub.Model;

public class WizardChapterModel {
    private final int id;
    private final String title;
    private final String subtitle;

    public WizardChapterModel(int id, String title, String subtitle) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
}
