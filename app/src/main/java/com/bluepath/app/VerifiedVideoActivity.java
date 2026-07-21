package com.bluepath.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bluepath.app.storage.UserStore;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plays supported YouTube learning videos inside the app and records actual player state,
 * cumulative playing time, progress and the ended event. External browser time is never
 * accepted as verified viewing evidence.
 */
public class VerifiedVideoActivity extends AppCompatActivity {
    public static final String EXTRA_CONTENT_ID = "content_id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_MINUTES = "minutes";

    private static final Pattern YOUTUBE_ID = Pattern.compile(
            "(?:youtu\\.be/|youtube(?:-nocookie)?\\.com/(?:watch\\?v=|embed/|shorts/))([A-Za-z0-9_-]{6,})");

    private UserStore store;
    private String contentId;
    private int baseWatchedSeconds;
    private TextView status;
    private ProgressBar progress;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#06223F"));
        getWindow().setNavigationBarColor(Color.parseColor("#06223F"));

        contentId = value(EXTRA_CONTENT_ID);
        String title = value(EXTRA_TITLE);
        String url = value(EXTRA_URL);
        String videoId = extractYouTubeId(url);
        store = new UserStore(this);
        baseWatchedSeconds = store.getVideoWatchSeconds(contentId);

        if (contentId.isEmpty() || videoId.isEmpty()) {
            Toast.makeText(this, "이 영상은 앱 내 검증 재생을 지원하지 않아 외부 링크로 엽니다.", Toast.LENGTH_LONG).show();
            if (!url.isEmpty()) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#06223F"));

        TextView heading = new TextView(this);
        heading.setText(title.isEmpty() ? "BluePath 검증 영상 학습" : title);
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(18);
        heading.setPadding(dp(16), dp(14), dp(16), dp(8));
        root.addView(heading);

        status = new TextView(this);
        status.setTextColor(Color.parseColor("#C9FFFF"));
        status.setTextSize(13);
        status.setPadding(dp(16), 0, dp(16), dp(10));
        updateStatus(baseWatchedSeconds, store.getVideoProgressPercent(contentId), store.hasVideoEnded(contentId));
        root.addView(status);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(store.getVideoProgressPercent(contentId));
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(8)));

        WebView webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        webView.addJavascriptInterface(new PlaybackBridge(), "BluePathAndroid");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri target = request.getUrl();
                if (target != null && target.getHost() != null
                        && !target.getHost().contains("youtube.com")
                        && !target.getHost().contains("googlevideo.com")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, target));
                    return true;
                }
                return false;
            }
        });
        webView.loadDataWithBaseURL("https://www.youtube.com", playerHtml(videoId), "text/html", "UTF-8", null);
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView guide = new TextView(this);
        guide.setText("재생 상태와 실제 시청 구간만 기록됩니다. 70% 이상 시청하고 최소 학습 시간을 충족하면 완료 인증을 진행할 수 있습니다.");
        guide.setTextColor(Color.WHITE);
        guide.setTextSize(12);
        guide.setPadding(dp(16), dp(10), dp(16), dp(14));
        root.addView(guide);
        setContentView(root);
    }

    private String value(String key) {
        String value = getIntent().getStringExtra(key);
        return value == null ? "" : value.trim();
    }

    private String extractYouTubeId(String url) {
        if (url == null) return "";
        Matcher matcher = YOUTUBE_ID.matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String playerHtml(String videoId) {
        return "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>html,body,#player{margin:0;width:100%;height:100%;background:#000}</style></head><body>"
                + "<div id='player'></div><script src='https://www.youtube.com/iframe_api'></script><script>"
                + "let player, watched=0, lastTick=Date.now(), ended=false;"
                + "function onYouTubeIframeAPIReady(){player=new YT.Player('player',{videoId:'" + videoId
                + "',playerVars:{playsinline:1,rel:0,modestbranding:1},events:{onStateChange:onState}});"
                + "setInterval(report,1000);}"
                + "function onState(e){lastTick=Date.now();if(e.data===YT.PlayerState.ENDED){ended=true;report();}}"
                + "function report(){if(!player||!player.getPlayerState)return;let now=Date.now();"
                + "if(player.getPlayerState()===YT.PlayerState.PLAYING){watched+=Math.max(0,Math.min(2,(now-lastTick)/1000));}"
                + "lastTick=now;let d=Number(player.getDuration()||0),c=Number(player.getCurrentTime()||0);"
                + "let pct=d>0?Math.round(c*100/d):0;BluePathAndroid.onProgress(Math.floor(watched),pct,ended);}" 
                + "</script></body></html>";
    }

    private final class PlaybackBridge {
        @JavascriptInterface
        public void onProgress(int sessionWatchedSeconds, int progressPercent, boolean ended) {
            int total = baseWatchedSeconds + Math.max(0, sessionWatchedSeconds);
            store.recordVerifiedPlayback(contentId, total, progressPercent, ended);
            runOnUiThread(() -> updateStatus(total, progressPercent, ended));
        }
    }

    private void updateStatus(int watchedSeconds, int progressPercent, boolean ended) {
        if (progress != null) progress.setProgress(Math.max(0, Math.min(100, progressPercent)));
        if (status != null) {
            status.setText(String.format(Locale.KOREA, "검증 시청 %d분 %02d초 · 진행률 %d%%%s",
                    watchedSeconds / 60, watchedSeconds % 60, progressPercent, ended ? " · 재생 완료" : ""));
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
