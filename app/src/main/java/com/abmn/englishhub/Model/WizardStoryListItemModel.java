package com.abmn.englishhub.Model;

// Lightweight row for the lesson list — the full WizardStoryModel (with
// paragraphs and grammar notes) is only fetched once a single story is opened.
public class WizardStoryListItemModel {
    private final int id;
    private final String hookTitle;

    public WizardStoryListItemModel(int id, String hookTitle) {
        this.id = id;
        this.hookTitle = hookTitle;
    }

    public int getId() { return id; }
    public String getHookTitle() { return hookTitle; }
}
