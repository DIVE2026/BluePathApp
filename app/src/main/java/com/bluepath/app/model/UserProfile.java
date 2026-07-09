package com.bluepath.app.model;

public class UserProfile {
    public String ageGroup;
    public String interest;
    public String goal;
    public String level;
    public String persona;
    public int xp;

    public UserProfile(String ageGroup, String interest, String goal, String level, String persona, int xp) {
        this.ageGroup = ageGroup;
        this.interest = interest;
        this.goal = goal;
        this.level = level;
        this.persona = persona;
        this.xp = xp;
    }
}
