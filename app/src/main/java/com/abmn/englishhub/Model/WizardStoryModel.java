package com.abmn.englishhub.Model;

import java.util.List;

public class WizardStoryModel {
    private final int id;
    private final String hookTitle;
    private final String meta;
    private final List<String> englishParagraphs;
    private final String banglaTitle;
    private final List<String> banglaParagraphs;
    private final List<String[]> grammarNotes;

    public WizardStoryModel(int id, String hookTitle, String meta,
                             List<String> englishParagraphs,
                             String banglaTitle, List<String> banglaParagraphs,
                             List<String[]> grammarNotes) {
        this.id = id;
        this.hookTitle = hookTitle;
        this.meta = meta;
        this.englishParagraphs = englishParagraphs;
        this.banglaTitle = banglaTitle;
        this.banglaParagraphs = banglaParagraphs;
        this.grammarNotes = grammarNotes;
    }

    public int getId() { return id; }
    public String getHookTitle() { return hookTitle; }
    public String getMeta() { return meta; }
    public List<String> getEnglishParagraphs() { return englishParagraphs; }
    public String getBanglaTitle() { return banglaTitle; }
    public List<String> getBanglaParagraphs() { return banglaParagraphs; }
    public List<String[]> getGrammarNotes() { return grammarNotes; }
}
