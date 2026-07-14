package com.bluepath.app.view;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.bluepath.app.util.PromotionRules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TierTextFormatter {
    private static final Pattern SHIELD_MARKER = Pattern.compile(
            "\\[\\[tier-shield:(브론즈|실버|골드|플래티넘|다이아)\\]\\]"
    );

    private TierTextFormatter() {}

    public static CharSequence format(Context context, String source) {
        if (source == null || source.isEmpty()) return "";

        String normalized = replaceLegacyMedals(source);
        Matcher matcher = SHIELD_MARKER.matcher(normalized);
        SpannableStringBuilder result = new SpannableStringBuilder();
        int cursor = 0;

        while (matcher.find()) {
            result.append(normalized, cursor, matcher.start());
            int spanStart = result.length();
            result.append('\uFFFC');
            result.setSpan(
                    new TierShieldSpan(matcher.group(1)),
                    spanStart,
                    spanStart + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            cursor = matcher.end();
        }
        result.append(normalized, cursor, normalized.length());
        return result;
    }

    private static String replaceLegacyMedals(String text) {
        return text
                .replace("🥉", PromotionRules.shieldMarker("브론즈"))
                .replace("🥈", PromotionRules.shieldMarker("실버"))
                .replace("🥇", PromotionRules.shieldMarker("골드"))
                .replace("🏆", PromotionRules.shieldMarker("플래티넘"))
                .replace("💎", PromotionRules.shieldMarker("다이아"));
    }
}
