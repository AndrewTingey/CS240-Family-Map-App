package com.example.familymapapp.cache;

public class SettingsCache {
    private static SettingsCache instance;
    private SettingsCache() {}
    public static SettingsCache getInstance() {
        if (instance == null) {
            instance = new SettingsCache();
        }
        return instance;
    }
    boolean isLifeStoryLines = true;
    boolean isFamilyTreeLines = true;
    boolean isSpouseLines = true;
    boolean isFatherSide = true;
    boolean isMotherSide = true;
    boolean isMaleEvents = true;
    boolean isFemaleEvents = true;

    public void setSettings(boolean lifeStoryLines, boolean isFamilyTreeLines,
                                  boolean isSpouseLines, boolean isFatherSide,
                                  boolean isMotherSide, boolean isMaleEvents,
                                  boolean isFemaleEvents) {
        this.isLifeStoryLines = lifeStoryLines;
        this.isFamilyTreeLines = isFamilyTreeLines;
        this.isSpouseLines = isSpouseLines;
        this.isFatherSide = isFatherSide;
        this.isMotherSide = isMotherSide;
        this.isMaleEvents = isMaleEvents;
        this.isFemaleEvents = isFemaleEvents;
    }

    public boolean isLifeStoryLines() {
        return isLifeStoryLines;
    }

    public boolean isFamilyTreeLines() {
        return isFamilyTreeLines;
    }

    public boolean isSpouseLines() {
        return isSpouseLines;
    }

    public boolean isFatherSide() {
        return isFatherSide;
    }

    public boolean isMotherSide() {
        return isMotherSide;
    }

    public boolean isMaleEvents() {
        return isMaleEvents;
    }

    public boolean isFemaleEvents() {
        return isFemaleEvents;
    }
}
