package xyz.duncanruns.julti.benchmarkplugin;

import com.google.common.io.Resources;
import xyz.duncanruns.julti.JultiAppLaunch;
import xyz.duncanruns.julti.JultiOptions;
import xyz.duncanruns.julti.affinity.AffinityManager;
import xyz.duncanruns.julti.benchmarkplugin.gui.BenchmarkPluginGUI;
import xyz.duncanruns.julti.command.CommandManager;
import xyz.duncanruns.julti.gui.JultiGUI;
import xyz.duncanruns.julti.gui.PluginsGUI;
import xyz.duncanruns.julti.plugin.PluginEvents;
import xyz.duncanruns.julti.plugin.PluginInitializer;
import xyz.duncanruns.julti.plugin.PluginManager;
import xyz.duncanruns.julti.resetting.ResetHelper;
import xyz.duncanruns.julti.script.lua.LuaLibraries;
import xyz.duncanruns.julti.util.ResetCounter;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;

public class BenchmarkPlugin implements PluginInitializer {

    public static BenchmarkPluginGUI gui = null;
    public static float lastRPS = 0;

    public static void main(String[] args) throws IOException {
        JultiAppLaunch.launchWithDevPlugin(args, PluginManager.JultiPluginData.fromString(
                Resources.toString(Resources.getResource(BenchmarkPlugin.class, "/julti.plugin.json"), Charset.defaultCharset())
        ), new BenchmarkPlugin());
    }

    public static BenchmarkPluginGUI openGUI() {
        if (gui == null || gui.isClosed()) {
            gui = new BenchmarkPluginGUI();
            PluginsGUI pluginsGUI = JultiGUI.getPluginsGUI();
            Point location = pluginsGUI.getLocation();
            location = new Point(location.x + pluginsGUI.getWidth(), location.y);
            gui.setLocation(location);
        }
        gui.requestFocus();
        return gui;
    }

    private static void checkFallbackOptions() {
        JultiOptions jultiOptions = JultiOptions.getJultiOptions();
        BenchmarkOptions benchmarkOptions = BenchmarkOptions.getBenchmarkOptions();

        String currentResetStyle = jultiOptions.resetStyle;
        if (!currentResetStyle.equals("Benchmark")) {
            if ((!benchmarkOptions.lastResetStyle.equals(currentResetStyle)) || (benchmarkOptions.lastDoDirtCovers != jultiOptions.doDirtCovers)) {
                benchmarkOptions.lastResetStyle = currentResetStyle;
                benchmarkOptions.lastDoDirtCovers = jultiOptions.doDirtCovers;
                BenchmarkOptions.save();
            }
        }
    }

    @Override
    public void initialize() {
        BenchmarkOptions.load();
        ResetHelper.registerResetStyle("Benchmark", BenchmarkResetManager::getBenchmarkResetManager);
        PluginEvents.RunnableEventType.END_TICK.register(() -> {
            checkFallbackOptions();
            if (ResetHelper.getManager() instanceof BenchmarkResetManager) {
                BenchmarkResetManager.getBenchmarkResetManager().endOfTick();
            }
        });
        ResetCounter.registerLockCondition(() -> ResetHelper.getManager() instanceof BenchmarkResetManager);
        AffinityManager.registerLockCondition(() -> ResetHelper.getManager() instanceof BenchmarkResetManager);
        CommandManager.getMainManager().registerCommand(new BenchmarkCommand());
        LuaLibraries.registerLuaLibrary(BenchmarkLuaLibrary::new);
    }

    @Override
    public void onMenuButtonPress() {
        openGUI();
    }
}
