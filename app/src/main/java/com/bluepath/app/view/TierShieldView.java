package com.bluepath.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class TierShieldView extends View {
    private String tier = "브론즈";

    public TierShieldView(Context context) { super(context); init(); }
    public TierShieldView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setTier(String tier) {
        this.tier = normalizeTier(tier);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawShield(canvas, new RectF(0f, 0f, getWidth(), getHeight()), tier, false);
    }

    /**
     * Shared shield renderer used by both the large Home/MY badge and the
     * compact inline shield span.
     */
    public static void drawShield(
            Canvas canvas,
            RectF bounds,
            String tier,
            boolean compact
    ) {
        float w = bounds.width();
        float h = bounds.height();
        if (w <= 0f || h <= 0f) return;

        float left = bounds.left;
        float top = bounds.top;
        float pad = Math.min(w, h) * 0.08f;

        Path shield = new Path();
        shield.moveTo(left + w / 2f, top + pad);
        shield.lineTo(left + w - pad, top + h * 0.22f);
        shield.lineTo(left + w * 0.84f, top + h * 0.72f);
        shield.quadTo(left + w / 2f, top + h - pad, left + w * 0.16f, top + h * 0.72f);
        shield.lineTo(left + pad, top + h * 0.22f);
        shield.close();

        int color = tierColor(tier);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        fill.setShader(new LinearGradient(
                left,
                top,
                left + w,
                top + h,
                lighten(color),
                darken(color),
                Shader.TileMode.CLAMP
        ));
        if (!compact) {
            fill.setShadowLayer(Math.min(w, h) * 0.12f, 0f, Math.min(w, h) * 0.05f,
                    Color.argb(90, 0, 0, 0));
        }

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(Math.max(1f, Math.min(w, h) * (compact ? 0.055f : 0.048f)));
        stroke.setColor(Color.WHITE);

        canvas.drawPath(shield, fill);
        canvas.drawPath(shield, stroke);

        if (compact) return;

        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(Color.WHITE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);

        String tierLabel = normalizeTier(tier);
        float availableWidth = w * 0.72f;
        float tierTextSize = Math.max(14f, Math.min(w, h) * 0.16f);
        text.setTextSize(tierTextSize);
        float measuredWidth = text.measureText(tierLabel);
        if (measuredWidth > availableWidth && measuredWidth > 0f) {
            text.setTextSize(tierTextSize * availableWidth / measuredWidth);
        }
        canvas.drawText(tierLabel, left + w / 2f, top + h * 0.55f, text);
        text.setTextSize(Math.max(9f, Math.min(w, h) * 0.09f));
        canvas.drawText("TIER", left + w / 2f, top + h * 0.70f, text);
    }

    private static int tierColor(String tier) {
        switch (normalizeTier(tier)) {
            case "실버": return Color.parseColor("#94A3B8");
            case "골드": return Color.parseColor("#EAB308");
            case "플래티넘": return Color.parseColor("#22C1C3");
            case "다이아": return Color.parseColor("#60A5FA");
            default: return Color.parseColor("#B7794A");
        }
    }

    private static String normalizeTier(String tier) {
        if (tier == null || tier.trim().isEmpty()) return "브론즈";
        return tier.trim();
    }

    private static int lighten(int color) {
        return Color.rgb(
                Math.min(255, Color.red(color) + 55),
                Math.min(255, Color.green(color) + 55),
                Math.min(255, Color.blue(color) + 55)
        );
    }

    private static int darken(int color) {
        return Color.rgb(
                Math.max(0, Color.red(color) - 55),
                Math.max(0, Color.green(color) - 55),
                Math.max(0, Color.blue(color) - 55)
        );
    }
}
