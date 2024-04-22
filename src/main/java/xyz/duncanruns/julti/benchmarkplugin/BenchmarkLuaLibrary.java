package xyz.duncanruns.julti.benchmarkplugin;

import xyz.duncanruns.julti.Julti;
import xyz.duncanruns.julti.cancelrequester.CancelRequester;
import xyz.duncanruns.julti.script.lua.LuaLibrary;

@SuppressWarnings("unused")
public class BenchmarkLuaLibrary extends LuaLibrary {
    public BenchmarkLuaLibrary(CancelRequester requester) {
        super(requester, "benchmark");
    }

    public void start(Integer newResetGoal) {
        if (newResetGoal != null) {
            setResetGoal(newResetGoal);
        }
        Julti.waitForExecute(() -> BenchmarkResetManager.getBenchmarkResetManager().startBenchmark());
    }

    public void end() {
        Julti.waitForExecute(() -> BenchmarkResetManager.getBenchmarkResetManager().endBenchmark());
    }

    public void setResetGoal(int newResetGoal) {
        Julti.waitForExecute(() -> BenchmarkOptions.getBenchmarkOptions().resetGoal = newResetGoal);
    }
}
