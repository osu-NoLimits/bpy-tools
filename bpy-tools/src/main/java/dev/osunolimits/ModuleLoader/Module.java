package dev.osunolimits.ModuleLoader;

import commons.marcandreher.Cache.Action.DatabaseAction;

public interface Module {

    public void initialize();

    public void start();

    public DatabaseAction run();

    public String getName();

    public String getConfigToggleKey();

}
