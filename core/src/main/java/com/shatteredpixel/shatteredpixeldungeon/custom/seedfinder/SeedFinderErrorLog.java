package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 查种器专用错误记录。
 * 与全局崩溃日志分开存放，只记录 SeedFinder 工作线程/主线程产生的异常，
 * 供 SeedFindScene 第一页的错误记录入口展示。
 */
public final class SeedFinderErrorLog {

    public static final String DIR = "seedfinder_errors";
    public static final String EXT = ".log";

    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String FILE_TIME_FORMAT = "yyyy-MM-dd_HH-mm-ss-SSS";
    private static final AtomicInteger SEQ = new AtomicInteger();

    private SeedFinderErrorLog() {}

    public static synchronized void record(String source, Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SEED FINDER ERROR ===\n");
        sb.append("Time: ").append(now(TIME_FORMAT)).append("\n");
        sb.append("Source: ").append(source).append("\n");
        sb.append("Message: ").append(t.getClass().getName());
        if (t.getMessage() != null) {
            sb.append(": ").append(t.getMessage());
        }
        sb.append("\n\nStack Trace:\n");
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        sb.append(sw);
        if (sb.length() == 0 || sb.charAt(sb.length() - 1) != '\n') {
            sb.append("\n");
        }
        sb.append("=== END REPORT ===\n");
        write(sb.toString());
    }

    private static void write(String content) {
        if (Gdx.files == null) {
            System.err.println(content);
            return;
        }
        try {
            FileHandle dir = Gdx.files.local(DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String name = "seedfinder_" + now(FILE_TIME_FORMAT)
                    + "_" + SEQ.incrementAndGet() + EXT;
            dir.child(name).writeString(content, false);
        } catch (Exception e) {
            System.err.println("Failed to save seedfinder error: " + e.getMessage());
        }
    }

    public static synchronized ArrayList<Entry> list() {
        ArrayList<Entry> result = new ArrayList<>();
        if (Gdx.files == null) {
            return result;
        }
        FileHandle dir = Gdx.files.local(DIR);
        if (!dir.exists()) {
            return result;
        }
        FileHandle[] files = dir.list();
        if (files == null) {
            return result;
        }
        for (FileHandle file : files) {
            if (!file.name().endsWith(EXT)) {
                continue;
            }
            try {
                result.add(new Entry(file.name(), file.readString()));
            } catch (Exception e) {
                System.err.println("Failed to read seedfinder error: " + e.getMessage());
            }
        }
        Collections.sort(result, (a, b) -> b.time.compareTo(a.time));
        return result;
    }

    public static int count() {
        return list().size();
    }

    public static boolean hasEntries() {
        return count() > 0;
    }

    public static synchronized void delete(String fileName) {
        if (Gdx.files == null) {
            return;
        }
        try {
            FileHandle file = Gdx.files.local(DIR).child(fileName);
            if (file.exists()) {
                file.delete();
            }
            FileHandle dir = Gdx.files.local(DIR);
            if (dir.exists() && dir.list().length == 0) {
                dir.deleteDirectory();
            }
        } catch (Exception e) {
            System.err.println("Failed to delete seedfinder error: " + e.getMessage());
        }
    }

    public static synchronized void clear() {
        if (Gdx.files == null) {
            return;
        }
        try {
            FileHandle dir = Gdx.files.local(DIR);
            if (!dir.exists()) {
                return;
            }
            FileHandle[] files = dir.list();
            if (files != null) {
                for (FileHandle file : files) {
                    if (file.name().endsWith(EXT)) {
                        file.delete();
                    }
                }
            }
            if (dir.exists() && dir.list().length == 0) {
                dir.deleteDirectory();
            }
        } catch (Exception e) {
            System.err.println("Failed to clear seedfinder errors: " + e.getMessage());
        }
    }

    private static String now(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
    }

    public static final class Entry {
        public final String fileName;
        public final String content;
        public final String time;

        Entry(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
            this.time = field(content, "Time: ");
        }

        private static String field(String content, String prefix) {
            for (String line : content.split("\n")) {
                if (line.startsWith(prefix)) {
                    return line.substring(prefix.length()).trim();
                }
            }
            return "";
        }
    }
}
