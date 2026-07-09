package com.bluepath.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.bluepath.app.model.UserProfile;

import java.util.HashSet;
import java.util.Set;

public class UserStore {
    private final SharedPreferences prefs;
    private static final String PREF = "bluepath_user";

    public UserStore(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean hasProfile() {
        return prefs.contains("ageGroup");
    }

    public UserProfile getProfile() {
        return new UserProfile(
                prefs.getString("ageGroup", "중학생"),
                prefs.getString("interest", "항해"),
                prefs.getString("goal", "진로탐색"),
                prefs.getString("level", "입문"),
                prefs.getString("persona", "진로 탐색 항해자"),
                prefs.getInt("xp", 0)
        );
    }

    public void saveProfile(UserProfile profile) {
        prefs.edit()
                .putString("ageGroup", profile.ageGroup)
                .putString("interest", profile.interest)
                .putString("goal", profile.goal)
                .putString("level", profile.level)
                .putString("persona", profile.persona)
                .putInt("xp", profile.xp)
                .apply();
    }

    public void addXp(int amount) {
        prefs.edit().putInt("xp", Math.max(0, prefs.getInt("xp", 0) + amount)).apply();
    }

    public void setXp(int xp) {
        prefs.edit().putInt("xp", Math.max(0, xp)).apply();
    }

    public String getTier() {
        return tierForXp(prefs.getInt("xp", 0));
    }

    public static String tierForXp(int xp) {
        if (xp >= 4200) return "다이아";
        if (xp >= 2800) return "플래티넘";
        if (xp >= 1600) return "골드";
        if (xp >= 700) return "실버";
        return "브론즈";
    }

    public static int nextTierXp(String tier) {
        switch (tier) {
            case "브론즈": return 700;
            case "실버": return 1600;
            case "골드": return 2800;
            case "플래티넘": return 4200;
            default: return 4200;
        }
    }

    public static int tierBaseXp(String tier) {
        switch (tier) {
            case "실버": return 700;
            case "골드": return 1600;
            case "플래티넘": return 2800;
            case "다이아": return 4200;
            default: return 0;
        }
    }

    public Set<String> getCompletedContentIds() {
        return new HashSet<>(prefs.getStringSet("completed", new HashSet<String>()));
    }

    public void markCompleted(String contentId) {
        Set<String> ids = getCompletedContentIds();
        ids.add(contentId);
        prefs.edit().putStringSet("completed", ids).apply();
    }

    public Set<String> getBookmarks() {
        return new HashSet<>(prefs.getStringSet("bookmarks", new HashSet<String>()));
    }

    public void toggleBookmark(String id) {
        Set<String> ids = getBookmarks();
        if (ids.contains(id)) ids.remove(id); else ids.add(id);
        prefs.edit().putStringSet("bookmarks", ids).apply();
    }

    public void reset() {
        prefs.edit().clear().apply();
    }
}
