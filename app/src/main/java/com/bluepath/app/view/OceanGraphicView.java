package com.bluepath.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class OceanGraphicView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public OceanGraphicView(Context context) {
        super(context);
    }

    public OceanGraphicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();

        paint.setColor(Color.parseColor("#0B4F6C"));
        canvas.drawRect(0, 0, w, h, paint);

        drawWave(canvas, h * 0.45f, Color.parseColor("#0E7490"), 18f);
        drawWave(canvas, h * 0.62f, Color.parseColor("#0891B2"), 24f);
        drawWave(canvas, h * 0.78f, Color.parseColor("#06B6D4"), 28f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.parseColor("#BFFBFA"));
        canvas.drawCircle(w * 0.84f, h * 0.28f, dp(18), paint);
        canvas.drawLine(w * 0.84f, h * 0.28f - dp(14), w * 0.84f, h * 0.28f + dp(14), paint);
        canvas.drawLine(w * 0.84f - dp(14), h * 0.28f, w * 0.84f + dp(14), h * 0.28f, paint);
        paint.setStyle(Paint.Style.FILL);

        drawFish(canvas, w * 0.22f, h * 0.55f, dp(11));
        drawFish(canvas, w * 0.52f, h * 0.72f, dp(8));

        paint.setColor(Color.argb(150, 210, 250, 250));
        canvas.drawCircle(w * 0.34f, h * 0.34f, dp(3), paint);
        canvas.drawCircle(w * 0.38f, h * 0.25f, dp(5), paint);
        canvas.drawCircle(w * 0.68f, h * 0.45f, dp(4), paint);
        canvas.drawCircle(w * 0.72f, h * 0.35f, dp(2), paint);
    }

    private void drawWave(Canvas canvas, float baseY, int color, float amplitude) {
        Path path = new Path();
        path.moveTo(0, baseY);
        float w = getWidth();
        float h = getHeight();
        path.cubicTo(w * 0.18f, baseY - amplitude, w * 0.32f, baseY + amplitude, w * 0.5f, baseY);
        path.cubicTo(w * 0.68f, baseY - amplitude, w * 0.82f, baseY + amplitude, w, baseY);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }

    private void drawFish(Canvas canvas, float x, float y, float size) {
        paint.setColor(Color.argb(210, 255, 255, 255));
        canvas.drawOval(x - size, y - size * 0.55f, x + size, y + size * 0.55f, paint);
        Path tail = new Path();
        tail.moveTo(x - size, y);
        tail.lineTo(x - size * 1.75f, y - size * 0.75f);
        tail.lineTo(x - size * 1.75f, y + size * 0.75f);
        tail.close();
        canvas.drawPath(tail, paint);
        paint.setColor(Color.parseColor("#06223F"));
        canvas.drawCircle(x + size * 0.55f, y - size * 0.12f, Math.max(1f, size * 0.12f), paint);
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
