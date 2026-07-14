package com.bluepath.app.util;

public final class PromotionRules {
    private static final String SHIELD_PREFIX = "[[tier-shield:";
    private static final String SHIELD_SUFFIX = "]]";

    private PromotionRules() {}

    /**
     * Inline UI marker. MainActivity converts this marker into the same colored
     * shield that is used on the Home and MY tabs.
     */
    public static String shieldMarker(String tier) {
        return SHIELD_PREFIX + normalizeTier(tier) + SHIELD_SUFFIX;
    }

    /**
     * Kept for source compatibility with older callers. It no longer returns a
     * medal emoji; it returns an inline shield marker instead.
     */
    public static String emoji(String tier) {
        return shieldMarker(tier);
    }

    public static String displayName(String tier) {
        String normalized = normalizeTier(tier);
        return shieldMarker(normalized) + " " + normalized;
    }

    public static String plainDisplayName(String tier) {
        return normalizeTier(tier);
    }

    public static String displayTransition(String fromTier) {
        return displayName(fromTier) + " → " + displayName(nextTier(fromTier));
    }

    public static String stripShieldMarkers(String text) {
        if (text == null || text.isEmpty()) return "";
        String result = text;
        int start;
        while ((start = result.indexOf(SHIELD_PREFIX)) >= 0) {
            int end = result.indexOf(SHIELD_SUFFIX, start + SHIELD_PREFIX.length());
            if (end < 0) break;
            int after = end + SHIELD_SUFFIX.length();
            if (after < result.length() && result.charAt(after) == ' ') after++;
            result = result.substring(0, start) + result.substring(after);
        }
        return result.trim();
    }

    public static int questionCount(String tier) {
        switch (normalizeTier(tier)) {
            case "브론즈": return 10;
            case "실버": return 12;
            case "골드": return 15;
            case "플래티넘": return 20;
            default: return 0;
        }
    }

    public static int passCount(String tier) {
        switch (normalizeTier(tier)) {
            case "브론즈": return 7;
            case "실버": return 9;
            case "골드": return 10;
            case "플래티넘": return 16;
            default: return 0;
        }
    }

    public static String nextTier(String tier) {
        switch (normalizeTier(tier)) {
            case "브론즈": return "실버";
            case "실버": return "골드";
            case "골드": return "플래티넘";
            case "플래티넘": return "다이아";
            default: return "다이아";
        }
    }

    public static int rank(String tier) {
        switch (normalizeTier(tier)) {
            case "브론즈": return 1;
            case "실버": return 2;
            case "골드": return 3;
            case "플래티넘": return 4;
            case "다이아": return 5;
            default: return 1;
        }
    }

    public static String tierForRank(int rank) {
        if (rank >= 5) return "다이아";
        if (rank == 4) return "플래티넘";
        if (rank == 3) return "골드";
        if (rank == 2) return "실버";
        return "브론즈";
    }

    public static String quizRule(String tier) {
        String normalized = normalizeTier(tier);
        int total = questionCount(normalized);
        int pass = passCount(normalized);
        if ("플래티넘".equals(normalized)) {
            return total + "문제 중 " + pass + "문제 이상 + 자격 증빙 + 해양 프로젝트 승인 시 "
                    + displayName("다이아") + " 승급";
        }
        if (total == 0) return "해당 티어는 퀴즈 단독 승급 대상이 아닙니다.";
        return total + "문제 중 " + pass + "문제 이상 정답 시 " + displayName(nextTier(normalized)) + " 승급";
    }

    public static String fullManual() {
        return "[기존 XP 승급 기준]\n"
                + "• " + displayName("브론즈") + ": 0~699 XP\n"
                + "• " + displayName("실버") + ": 700 XP 이상\n"
                + "• " + displayName("골드") + ": 1,600 XP 이상\n"
                + "• " + displayName("플래티넘") + ": 2,800 XP 이상\n"
                + "• " + displayName("다이아") + ": 4,200 XP 이상\n\n"
                + "[퀴즈 승급 기준]\n"
                + "• " + displayName("브론즈") + " → " + displayName("실버") + ": 10문제 중 7문제 이상\n"
                + "• " + displayName("실버") + " → " + displayName("골드") + ": 12문제 중 9문제 이상\n"
                + "• " + displayName("골드") + " → " + displayName("플래티넘") + ": 15문제 중 10문제 이상\n"
                + "• " + displayName("플래티넘") + " → " + displayName("다이아")
                + ": 고급 20문제 중 16문제 이상, 자격 증빙 승인, 해양 프로젝트 승인\n"
                + "• 모든 문제는 4지선다이며, 전 문항 선택 후 한 번에 채점합니다.\n"
                + "• 채점 후 총점, 합격 여부, 문항별 정답과 해설을 제공합니다.\n\n"
                + "최종 티어는 XP 기준, 퀴즈 획득 티어, 다이아 인증 경로 중 가장 높은 단계가 적용됩니다.";
    }

    public static String fullManualPlain() {
        return stripShieldMarkers(fullManual());
    }

    private static String normalizeTier(String tier) {
        if (tier == null) return "브론즈";
        String value = tier.trim();
        switch (value) {
            case "브론즈":
            case "실버":
            case "골드":
            case "플래티넘":
            case "다이아":
                return value;
            default:
                return "브론즈";
        }
    }
}
