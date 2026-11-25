package com.bypasscharge.tile;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Quick Settings tile that toggles bypass charging on the Infinix Note 30 (X6833B)
 * by writing 0/1 to the MTK charger driver node. Needs root (su).
 */
public class BypassTileService extends TileService {

    private static final String NODE = "/sys/devices/platform/charger/bypass_charger";

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    public void onStartListening() {
        io.execute(() -> show(readState()));
    }

    @Override
    public void onClick() {
        io.execute(() -> {
            String current = su("cat " + NODE);
            if (current == null) {
                show(Tile.STATE_UNAVAILABLE);
                return;
            }
            boolean on = current.trim().equals("1");
            su("echo " + (on ? "0" : "1") + " > " + NODE);
            show(readState());
        });
    }

    @Override
    public void onDestroy() {
        io.shutdownNow();
        super.onDestroy();
    }

    private int readState() {
        String v = su("cat " + NODE);
        if (v == null) return Tile.STATE_UNAVAILABLE;
        return v.trim().equals("1") ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    private void show(int state) {
        main.post(() -> {
            Tile tile = getQsTile();
            if (tile == null) return;
            tile.setLabel("Bypass charge");
            tile.setState(state);
            if (Build.VERSION.SDK_INT >= 29) {
                tile.setSubtitle(state == Tile.STATE_ACTIVE ? "On"
                        : state == Tile.STATE_INACTIVE ? "Off" : "No root");
            }
            tile.updateTile();
        });
    }

    /** Runs a command as root. Returns its output, or null if su failed or timed out. */
    private static String su(String command) {
        Process p = null;
        try {
            p = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
            if (!p.waitFor(5, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return null;
            }
            if (p.exitValue() != 0) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (p != null) p.destroy();
        }
    }
}
