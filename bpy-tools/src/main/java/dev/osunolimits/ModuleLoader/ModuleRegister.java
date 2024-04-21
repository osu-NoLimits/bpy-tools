package dev.osunolimits.ModuleLoader;

import java.util.ArrayList;

import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.Flogger.Prefix;
import commons.marcandreher.Utils.Color;
import dev.osunolimits.App;

public class ModuleRegister {

    public static ArrayList<ModuleInstance> modules = new ArrayList<>();

    public static void addModuleToRegister(ModuleInstance module) {
        modules.add(module);
    }

    public static void loadModules() {
        for (ModuleInstance module : modules) {
            try {
                if (Boolean.parseBoolean(App.dotenv.get(module.getConfigToggleKey()))) {
                    Flogger.instance.log(Prefix.INFO,
                            "Loading [" + Color.GREEN + module.getName() + Color.RESET + "] Module", 0);
                    module.initialize();
                } else {
                    Flogger.instance.log(Prefix.INFO,
                            "Not loading [" + Color.RED + module.getName() + Color.RESET + "] Module is deactivated",
                            0);
                }
            } catch (Exception e) {
                Flogger.instance.error(new Exception(
                        "Failed to load ModuleToggleKey [" + module.getConfigToggleKey() + "] should be 'boolean'"));
            }

        }
    }

}
