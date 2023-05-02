package com.nitc.fyproject.classes;

public class WordInfo {
    private String synonyms;
    private String partOfSpeech;
    private String definition;

    public WordInfo(){

    }

    // Constructor
    public WordInfo(String synonyms, String partOfSpeech, String definition) {
        this.synonyms = synonyms;
        this.partOfSpeech = partOfSpeech;
        this.definition = definition;
    }

    // Getters and Setters
    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
