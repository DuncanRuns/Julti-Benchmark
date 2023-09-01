package xyz.duncanruns.julti.benchmarkplugin;

import org.apache.logging.log4j.Level;
import xyz.duncanruns.julti.Julti;
import xyz.duncanruns.julti.JultiOptions;
import xyz.duncanruns.julti.instance.InstanceState;
import xyz.duncanruns.julti.instance.MinecraftInstance;
import xyz.duncanruns.julti.management.OBSStateManager;
import xyz.duncanruns.julti.resetting.ActionResult;
import xyz.duncanruns.julti.resetting.ResetManager;
import xyz.duncanruns.julti.util.DoAllFastUtil;

import java.util.Collections;
import java.util.List;

public class BenchmarkResetManager extends ResetManager {
    private static final BenchmarkResetManager INSTANCE = new BenchmarkResetManager();
    private long startTime = -1L;
    private int previewsLoaded = 0;
    private final PreviousOptions previousOptions = new PreviousOptions();

    public static BenchmarkResetManager getBenchmarkResetManager() {
        return INSTANCE;
    }

    @Override
    public List<ActionResult> doReset() {
        return Collections.emptyList();
    }

    @Override
    public void notifyPreviewLoaded(MinecraftInstance instance) {
        super.notifyPreviewLoaded(instance);
        if (!isRunning()) return;
        // Instead of calling reset here, we do it at the end of the tick so that all instances which need resetting can be processed in parallel
        previewsLoaded++;

        if (previewsLoaded >= BenchmarkOptions.getBenchmarkOptions().resetGoal) {
            endBenchmark();
        } else if (previewsLoaded % 100 == 0) {
            reportProgress();
        }
    }

    private void reportProgress() {
        long currentTime = System.currentTimeMillis();
        int resetGoal = BenchmarkOptions.getBenchmarkOptions().resetGoal;
        long millisElapsed = currentTime - startTime;
        float secondsElapsed = (millisElapsed / 1000f);
        float rps = previewsLoaded / secondsElapsed;
        float estimatedFinalTime = resetGoal / rps;
        float eta = estimatedFinalTime - secondsElapsed;
        Julti.log(Level.INFO, String.format("(%d/%d) ETA: %.2fs, RPS: %.2f, Final Estimate: %.2fs", previewsLoaded, resetGoal, eta, rps, estimatedFinalTime));
    }

    public void endBenchmark() {
        if (!isRunning()) return;
        long currentTime = System.currentTimeMillis();
        long millisElapsed = currentTime - startTime;
        float secondsElapsed = millisElapsed / 1000f;
        float rps = previewsLoaded / secondsElapsed;
        Julti.log(Level.INFO, String.format("Benchmark ended! Finished %d preview loads in %.2f seconds. RPS: %.2f", previewsLoaded, secondsElapsed, rps));
        previewsLoaded = 0;
        startTime = -1L;

        JultiOptions jo = JultiOptions.getJultiOptions();
        jo.resetStyle = previousOptions.resetStyle;
        jo.autoCalcWallSize = previousOptions.autoCalcWallSize;
        jo.doDirtCovers = previousOptions.doDirtCovers;
        jo.resetCounter = previousOptions.resetCounter;
    }

    public void endOfTick() {
        OBSStateManager.getOBSStateManager().setLocationToWall();
        if (!isRunning()) return;
        DoAllFastUtil.doAllFast(instance -> {
            if (instance.getStateTracker().isCurrentState(InstanceState.PREVIEWING) || instance.getStateTracker().isCurrentState(InstanceState.INWORLD)) {
                instance.reset();
            }
        });
    }

    private boolean isRunning() {
        return startTime != -1L;
    }

    public void startBenchmark() {
        JultiOptions jo = JultiOptions.getJultiOptions();
        previousOptions.resetStyle = jo.resetStyle;
        previousOptions.autoCalcWallSize = jo.autoCalcWallSize;
        previousOptions.doDirtCovers = jo.doDirtCovers;
        previousOptions.resetCounter = jo.resetCounter;

        jo.resetStyle = "Benchmark";
        jo.autoCalcWallSize = true;
        jo.doDirtCovers = false;


        startTime = System.currentTimeMillis();
        DoAllFastUtil.doAllFast(MinecraftInstance::reset);
        Julti.log(Level.INFO, "Running benchmark...");
    }

    private static final class PreviousOptions {
        public String resetStyle;
        public boolean doDirtCovers;
        public boolean autoCalcWallSize;
        public int resetCounter;
    }
}
