package xyz.duncanruns.julti.benchmarkplugin;

import com.google.common.io.Resources;
import xyz.duncanruns.julti.JultiAppLaunch;
import xyz.duncanruns.julti.benchmarkplugin.gui.BenchmarkPluginGUI;
import xyz.duncanruns.julti.gui.JultiGUI;
import xyz.duncanruns.julti.gui.PluginsGUI;
import xyz.duncanruns.julti.plugin.PluginEvents;
import xyz.duncanruns.julti.plugin.PluginInitializer;
import xyz.duncanruns.julti.plugin.PluginManager;
import xyz.duncanruns.julti.resetting.ResetHelper;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;

public class BenchmarkPlugin implements PluginInitializer {

    public static BenchmarkPluginGUI gui = null;

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

    @Override
    public void initialize() {
        BenchmarkOptions.load();
        ResetHelper.registerResetStyle("Benchmark", BenchmarkResetManager::getBenchmarkResetManager);
        PluginEvents.RunnableEventType.END_TICK.register(() -> {
            if (ResetHelper.getManager() instanceof BenchmarkResetManager) {
                BenchmarkResetManager.getBenchmarkResetManager().endOfTick();
            }
        });
    }

    @Override
    public void onMenuButtonPress() {
        openGUI();
    }
}
