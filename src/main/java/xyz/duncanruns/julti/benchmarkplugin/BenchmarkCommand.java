package xyz.duncanruns.julti.benchmarkplugin;

import xyz.duncanruns.julti.Julti;
import xyz.duncanruns.julti.cancelrequester.CancelRequester;
import xyz.duncanruns.julti.command.Command;

public class BenchmarkCommand extends Command {
    private static void internalRun(String[] args) {
        if (args.length == 1) {
            if (args[0].equals("end")) {
                BenchmarkResetManager.getBenchmarkResetManager().endBenchmark();
                return;
            }
            BenchmarkOptions.getBenchmarkOptions().resetGoal = Integer.parseInt(args[0]);
        }
        BenchmarkResetManager.getBenchmarkResetManager().startBenchmark();
    }

    @Override
    public String helpDescription() {
        return "benchmark - Starts a benchmark with the existing reset goal\n" +
                "benchmark [reset goal] - Sets the reset goal and starts the benchmark\n" +
                "benchmark end - Ends the benchmark (if running)";
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public String getName() {
        return "benchmark";
    }

    @Override
    public void run(String[] args, CancelRequester cancelRequester) {
        Julti.waitForExecute(() -> internalRun(args));
    }
}
