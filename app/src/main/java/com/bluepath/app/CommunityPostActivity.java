package com.bluepath.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bluepath.app.repository.BluePathRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommunityPostActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY = "community_category";

    private final int NAVY = Color.parseColor("#06223F");
    private final int OCEAN = Color.parseColor("#0E7490");
    private final int BG = Color.parseColor("#F2FAFB");
    private final int TEXT = Color.parseColor("#17324D");
    private final int MUTED = Color.parseColor("#64748B");

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private BluePathRepository repository;
    private EditText titleInput;
    private EditText bodyInput;
    private Button submitButton;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);

        repository = new BluePathRepository(this);
        category = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (!"question".equals(category)) category = "free";

        setContentView(buildScreen());
        titleInput.requestFocus();
        titleInput.postDelayed(() -> {
            InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.showSoftInput(titleInput, InputMethodManager.SHOW_IMPLICIT);
        }, 180L);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private View buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(10), dp(12), dp(10));
        header.setBackgroundColor(NAVY);

        Button back = secondaryButton("‹");
        back.setTextSize(28);
        back.setContentDescription("커뮤니티로 돌아가기");
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(48), dp(44)));

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.setPadding(dp(12), 0, 0, 0);
        TextView title = text("question".equals(category) ? "질문 게시판 글쓰기" : "자유 게시판 글쓰기", 20, Color.WHITE, true);
        TextView subtitle = text("커뮤니티에 새 글을 작성합니다", 11, Color.parseColor("#C9FFFF"), false);
        heading.addView(title);
        heading.addView(subtitle);
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView board = text(
                "question".equals(category) ? "질문 게시판" : "자유 게시판",
                13,
                OCEAN,
                true);
        form.addView(board);

        TextView titleLabel = text("제목", 13, TEXT, true);
        LinearLayout.LayoutParams titleLabelParams = new LinearLayout.LayoutParams(-1, -2);
        titleLabelParams.setMargins(0, dp(16), 0, dp(7));
        form.addView(titleLabel, titleLabelParams);

        titleInput = input("제목을 입력하세요");
        titleInput.setSingleLine(true);
        form.addView(titleInput, new LinearLayout.LayoutParams(-1, dp(56)));

        TextView bodyLabel = text("내용", 13, TEXT, true);
        LinearLayout.LayoutParams bodyLabelParams = new LinearLayout.LayoutParams(-1, -2);
        bodyLabelParams.setMargins(0, dp(18), 0, dp(7));
        form.addView(bodyLabel, bodyLabelParams);

        bodyInput = input("내용을 입력하세요");
        bodyInput.setSingleLine(false);
        bodyInput.setGravity(Gravity.TOP | Gravity.START);
        bodyInput.setPadding(dp(14), dp(14), dp(14), dp(14));
        bodyInput.setMinLines(12);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(-1, 0, 1);
        form.addView(bodyInput, bodyParams);

        TextView guide = text("제목과 내용은 각각 2자 이상 입력해 주세요.", 12, MUTED, false);
        LinearLayout.LayoutParams guideParams = new LinearLayout.LayoutParams(-1, -2);
        guideParams.setMargins(0, dp(10), 0, dp(12));
        form.addView(guide, guideParams);

        submitButton = primaryButton("등록하기");
        submitButton.setOnClickListener(v -> submitPost());
        form.addView(submitButton, new LinearLayout.LayoutParams(-1, dp(54)));

        root.addView(form, new LinearLayout.LayoutParams(-1, 0, 1));
        return root;
    }

    private void submitPost() {
        String title = titleInput.getText().toString().trim();
        String body = bodyInput.getText().toString().trim();
        if (title.length() < 2 || body.length() < 2) {
            Toast.makeText(this, "제목과 내용을 2자 이상 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        setSubmitting(true);
        executor.execute(() -> {
            try {
                repository.createCommunityPost(category, title, body);
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    Toast.makeText(this, "게시글을 등록했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setSubmitting(false);
                    String message = e.getMessage();
                    if (message == null || message.trim().isEmpty()) message = e.getClass().getSimpleName();
                    Toast.makeText(this, "글 작성 실패: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setSubmitting(boolean submitting) {
        titleInput.setEnabled(!submitting);
        bodyInput.setEnabled(!submitting);
        submitButton.setEnabled(!submitting);
        submitButton.setText(submitting ? "등록 중…" : "등록하기");
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(Color.parseColor("#94A3B8"));
        input.setTextColor(TEXT);
        input.setTextSize(15);
        input.setPadding(dp(14), 0, dp(14), 0);
        input.setBackground(rounded(Color.WHITE, Color.parseColor("#B8D7DF"), 14));
        return input;
    }

    private Button primaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setAllCaps(false);
        button.setBackground(rounded(OCEAN, OCEAN, 14));
        return button;
    }

    private Button secondaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackground(rounded(Color.TRANSPARENT, Color.parseColor("#66FFFFFF"), 14));
        return button;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    private GradientDrawable rounded(int fillColor, int strokeColor, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
