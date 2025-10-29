package com.abmn.englishhub.Model;

public class WordModel {
    private final int id;
    private final String word;
    private final String meaning;
    private final String synonyms;
    private final String antonyms;
    private final String lesson_id;
    private final Boolean status;
    private final String created_at;
    private final String updated_at;

    public WordModel(int id, String word, String meaning, String synonyms, String antonyms, String lesson_id, Boolean status, String created_at, String updated_at) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.synonyms = synonyms;
        this.antonyms = antonyms;
        this.lesson_id = lesson_id;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public String getAntonyms() {
        return antonyms;
    }

    public String getLesson_id() {
        return lesson_id;
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