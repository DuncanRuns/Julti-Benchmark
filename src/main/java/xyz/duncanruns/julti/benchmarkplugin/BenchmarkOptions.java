package xyz.duncanruns.julti.benchmarkplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.julti.Julti;
import xyz.duncanruns.julti.JultiOptions;
import xyz.duncanruns.julti.util.ExceptionUtil;
import xyz.duncanruns.julti.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BenchmarkOptions {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SAVE_PATH = JultiOptions.getJultiDir().resolve("benchmarkoptions.json");

    private static BenchmarkOptions instance = null;

    public int resetGoal = 2000;

    public static BenchmarkOptions getBenchmarkOptions() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(SAVE_PATH)) {
            instance = new BenchmarkOptions();
        } else {
            String s;
            try {
                s = FileUtil.readString(SAVE_PATH);
            } catch (IOException e) {
                instance = new BenchmarkOptions();
                return;
            }
            instance = GSON.fromJson(s, BenchmarkOptions.class);
        }
    }

    public static void save() {
        try {
            FileUtil.writeString(SAVE_PATH, GSON.toJson(instance));
        } catch (IOException e) {
            Julti.log(Level.ERROR, "Failed to save Benchmark Options: " + ExceptionUtil.toDetailedString(e));
        }
    }
}
