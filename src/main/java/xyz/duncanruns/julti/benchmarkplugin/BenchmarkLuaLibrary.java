package xyz.duncanruns.julti.benchmarkplugin;

import xyz.duncanruns.julti.Julti;
import xyz.duncanruns.julti.cancelrequester.CancelRequester;
import xyz.duncanruns.julti.script.lua.LuaLibrary;
import xyz.duncanruns.julti.util.SleepUtil;

@SuppressWarnings("unused")
public class BenchmarkLuaLibrary extends LuaLibrary {
    public BenchmarkLuaLibrary(CancelRequester requester) {
        super(requester, "benchmark");
    }

    @LuaDocumentation(description = "Starts a benchmark. An integer newResetGoal can be given to set the reset goal before starting. Alternatively just use benchmark.start() with no number given to use the existing reset goal.")
    public void start(Integer newResetGoal) {
        if (newResetGoal != null) {
            setResetGoal(newResetGoal);
        }
        Julti.waitForExecute(() -> BenchmarkResetManager.getBenchmarkResetManager().startBenchmark());
    }


    @LuaDocumentation(description = "Ends a benchmark if one is currently running.")
    public void stop() {
        Julti.waitForExecute(() -> BenchmarkResetManager.getBenchmarkResetManager().endBenchmark());
    }

    @LuaDocumentation(description = "Sets the reset goal for the benchmark (a benchmark must reset instances a total equal to the reset goal to complete the benchmark).")
    @AllowedWhileCustomizing
    public void setResetGoal(int newResetGoal) {
        Julti.waitForExecute(() -> BenchmarkOptions.getBenchmarkOptions().resetGoal = newResetGoal);
    }

    @LuaDocumentation(description = "Gets the RPS of the last ended benchmark, or 0 if none have ran yet.")
    @AllowedWhileCustomizing
    public float getLatestRPS() {
        return BenchmarkPlugin.lastRPS;
    }

    @LuaDocumentation(description = "Waits for the running benchmark to finish.")
    public void waitForBenchmarkEnd() {
        while (BenchmarkResetManager.getBenchmarkResetManager().isRunning()) {
            SleepUtil.sleep(5);
        }
    }
}
