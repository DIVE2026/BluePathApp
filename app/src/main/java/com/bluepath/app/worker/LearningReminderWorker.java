package com.bluepath.app.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bluepath.app.R;

public class LearningReminderWorker extends Worker {
    public static final String CHANNEL_ID = "bluepath_learning";

    public LearningReminderWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "BluePath learning reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Ocean learning, quiz, and schedule reminders");
            manager.createNotificationChannel(channel);
        }

        String title = getInputData().getString("title");
        String message = getInputData().getString("message");
        if (title == null || title.trim().isEmpty()) title = "🌊 오늘의 BluePath 항해";
        if (message == null || message.trim().isEmpty()) message = "추천 영상을 학습하고 다음 티어를 준비해 보세요.";

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_anchor)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        manager.notify(2401, notification.build());
        return Result.success();
    }
}
