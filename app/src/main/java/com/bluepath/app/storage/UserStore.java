package com.bluepath.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.bluepath.app.model.UserProfile;
import com.bluepath.app.util.PromotionRules;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
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
        String xpTier = tierForXp(prefs.getInt("xp", 0));
        String quizTier = PromotionRules.tierForRank(prefs.getInt("quizTierRank", 1));
        return PromotionRules.rank(xpTier) >= PromotionRules.rank(quizTier) ? xpTier : quizTier;
    }

    public String getXpTier() {
        return tierForXp(prefs.getInt("xp", 0));
    }

    public String getQuizTier() {
        return PromotionRules.tierForRank(prefs.getInt("quizTierRank", 1));
    }

    public void promoteByQuiz(String fromTier) {
        int targetRank = PromotionRules.rank(PromotionRules.nextTier(fromTier));
        int currentRank = prefs.getInt("quizTierRank", 1);
        if (targetRank > currentRank) prefs.edit().putInt("quizTierRank", targetRank).apply();
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

    public boolean isBookmarked(String id) {
        return getBookmarks().contains(id);
    }

    public void toggleBookmark(String id) {
        Set<String> ids = getBookmarks();
        if (ids.contains(id)) ids.remove(id); else ids.add(id);
        prefs.edit().putStringSet("bookmarks", ids).apply();
    }

    public void recordQuizAttempt(String tier, int correct, int total, boolean passed, String source) {
        int attempts = prefs.getInt("quizAttempts", 0) + 1;
        int best = Math.max(correct, prefs.getInt("bestQuiz_" + tier, 0));
        String date = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(new Date());
        String summary = date + " · " + PromotionRules.displayName(tier) + " " + correct + "/" + total + " · "
                + (passed ? "합격" : "재도전") + " · " + source;
        prefs.edit()
                .putInt("quizAttempts", attempts)
                .putInt("bestQuiz_" + tier, best)
                .putString("lastQuizSummary", summary)
                .apply();
    }

    public int getQuizAttempts() {
        return prefs.getInt("quizAttempts", 0);
    }

    public int getBestQuizScore(String tier) {
        return prefs.getInt("bestQuiz_" + tier, 0);
    }

    public String getLastQuizSummary() {
        return prefs.getString("lastQuizSummary", "아직 응시 기록이 없습니다.");
    }

    public void saveLlmConfig(String endpoint, String model, String apiKey) {
        prefs.edit()
                .putString("llmEndpoint", endpoint == null ? "" : endpoint.trim())
                .putString("llmModel", model == null ? "" : model.trim())
                .putString("llmApiKey", apiKey == null ? "" : apiKey.trim())
                .apply();
    }

    public String getLlmEndpoint() {
        return prefs.getString("llmEndpoint", "");
    }

    public String getLlmModel() {
        return prefs.getString("llmModel", "bluepath-marine-ft-v1");
    }

    public String getLlmApiKey() {
        return prefs.getString("llmApiKey", "");
    }

    public boolean hasLlmConfig() {
        return !getLlmEndpoint().trim().isEmpty() && !getLlmModel().trim().isEmpty();
    }

    public void reset() {
        prefs.edit().clear().apply();
    }
}
