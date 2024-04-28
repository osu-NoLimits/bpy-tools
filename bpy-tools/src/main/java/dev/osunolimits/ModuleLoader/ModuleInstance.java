package dev.osunolimits.ModuleLoader;

import java.util.concurrent.TimeUnit;

import commons.marcandreher.Cache.CacheTimer;
import commons.marcandreher.Cache.Action.DatabaseAction;

public class ModuleInstance implements Module {


    protected CacheTimer cacheTimer;

    protected int duration;
    protected TimeUnit timeUnit;

    public ModuleInstance(int duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    @Override
    public void initialize() {
        start();
    }

    @Override
    public void start() {
        cacheTimer = new CacheTimer(duration, 1, timeUnit);
        cacheTimer.addAction(run());
    }

    @Override
    public DatabaseAction run() {
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Unimplemented module 'name'");
    }

    @Override
    public String getConfigToggleKey() {
        throw new UnsupportedOperationException("Unimplemented method 'getConfigToggleKey'");
    }

}
