package xyz.duncanruns.julti.benchmarkplugin;

import org.apache.logging.log4j.Level;
import xyz.duncanruns.julti.Julti;
import xyz.duncanruns.julti.JultiOptions;
import xyz.duncanruns.julti.affinity.AffinityManager;
import xyz.duncanruns.julti.instance.MinecraftInstance;
import xyz.duncanruns.julti.management.InstanceManager;
import xyz.duncanruns.julti.management.OBSStateManager;
import xyz.duncanruns.julti.resetting.ActionResult;
import xyz.duncanruns.julti.resetting.ResetHelper;
import xyz.duncanruns.julti.resetting.ResetManager;
import xyz.duncanruns.julti.util.DoAllFastUtil;

import java.awt.*;
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

    private static void resetInstanceFast(MinecraftInstance instance) {
        instance.getKeyPresser().pressKey(instance.getGameOptions().createWorldKey);
    }

    @Override
    public List<ActionResult> doReset() {
        return Collections.emptyList();
    }

    @Override
    public void notifyPreviewLoaded(MinecraftInstance instance) {
        resetInstanceFast(instance);
        super.notifyPreviewLoaded(instance);
        if (!isRunning()) return;
        previewsLoaded++;

        if (previewsLoaded >= BenchmarkOptions.getBenchmarkOptions().resetGoal) {
            endBenchmark();
        } else if (previewsLoaded % 100 == 0) {
            reportProgress();
        }
    }

    @Override
    public void notifyWorldLoaded(MinecraftInstance instance) {
        resetInstanceFast(instance);
    }

    @Override
    public Rectangle getInstancePosition(MinecraftInstance instance, Dimension sceneSize) {
        List<MinecraftInstance> instances = InstanceManager.getInstanceManager().getInstances();

        int totalRows = (int) Math.max(1, Math.ceil(Math.sqrt(instances.size())));
        int totalColumns = (int) Math.max(1, Math.ceil(instances.size() / (float) totalRows));

        int instanceInd = instances.indexOf(instance);

        Dimension size = sceneSize == null ? OBSStateManager.getOBSStateManager().getOBSSceneSize() : sceneSize;

        // Using floats here so there won't be any gaps in the wall after converting back to int
        float iWidth = size.width / (float) totalColumns;
        float iHeight = size.height / (float) totalRows;

        int row = instanceInd / totalColumns;
        int col = instanceInd % totalColumns;

        int x = (int) (col * iWidth);
        int y = (int) (row * iHeight);
        return new Rectangle(
                x,
                y,
                (int) ((col + 1) * iWidth) - x,
                (int) ((row + 1) * iHeight) - y
        );
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
        jo.doDirtCovers = previousOptions.doDirtCovers;
    }

    public void endOfTick() {
        OBSStateManager.getOBSStateManager().setLocationToWall();
        JultiOptions jultiOptions = JultiOptions.getJultiOptions();
        if (!isRunning()) {
            BenchmarkOptions benchmarkOptions = BenchmarkOptions.getBenchmarkOptions();
            jultiOptions.resetStyle = benchmarkOptions.lastResetStyle;
            jultiOptions.doDirtCovers = benchmarkOptions.lastDoDirtCovers;
            Julti.doLater(() -> ResetHelper.getManager().reload());
        }
    }

    private boolean isRunning() {
        return startTime != -1L;
    }

    public void startBenchmark() {
        JultiOptions jo = JultiOptions.getJultiOptions();
        if (isRunning()) {
            if (jo.resetStyle.equals("Benchmark")) {
                return;
            } else {
                endBenchmark();
            }
        }

        InstanceManager instanceManager = InstanceManager.getInstanceManager();
        instanceManager.checkOpenedInstances();
        if (instanceManager.areInstancesMissing()) {
            Julti.log(Level.ERROR, "Could not start benchmark! (All instances need to be open)");
            return;
        }

        previousOptions.resetStyle = jo.resetStyle;
        previousOptions.doDirtCovers = jo.doDirtCovers;

        jo.resetStyle = "Benchmark";
        jo.doDirtCovers = false;

        startTime = System.currentTimeMillis();
        DoAllFastUtil.doAllFast(BenchmarkResetManager::resetInstanceFast);
        AffinityManager.release();
        Julti.log(Level.INFO, "Running benchmark...");
    }

    private static final class PreviousOptions {
        public String resetStyle;
        public boolean doDirtCovers;
    }
}
