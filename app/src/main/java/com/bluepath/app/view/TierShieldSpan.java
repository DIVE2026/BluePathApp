package com.bluepath.app.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class TierShieldSpan extends ReplacementSpan {
    private final String tier;

    public TierShieldSpan(String tier) {
        this.tier = tier;
    }

    @Override
    public int getSize(
            @NonNull Paint paint,
            CharSequence text,
            int start,
            int end,
            @Nullable Paint.FontMetricsInt fm
    ) {
        float height = paint.getTextSize() * 1.18f;
        float width = height * 0.86f;
        return Math.round(width + paint.getTextSize() * 0.10f);
    }

    @Override
    public void draw(
            @NonNull Canvas canvas,
            CharSequence text,
            int start,
            int end,
            float x,
            int top,
            int y,
            int bottom,
            @NonNull Paint paint
    ) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float lineTop = y + metrics.ascent;
        float lineBottom = y + metrics.descent;
        float lineHeight = lineBottom - lineTop;
        float height = paint.getTextSize() * 1.18f;
        float width = height * 0.86f;
        float shieldTop = lineTop + (lineHeight - height) / 2f;

        TierShieldView.drawShield(
                canvas,
                new RectF(x, shieldTop, x + width, shieldTop + height),
                tier,
                true
        );
    }
}
