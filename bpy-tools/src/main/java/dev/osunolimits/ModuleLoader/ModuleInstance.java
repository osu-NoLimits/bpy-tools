package dev.osunolimits.ModuleLoader;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import commons.marcandreher.Cache.CacheTimer;
import commons.marcandreher.Cache.Action.DatabaseAction;
import commons.marcandreher.Commons.Database;
import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.MySQL;

public class ModuleInstance implements Module {

    protected MySQL mysql;
    protected Flogger logger;

    protected CacheTimer cacheTimer;

    protected int duration;
    protected TimeUnit timeUnit;

    public ModuleInstance(int duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    @Override
    public void initialize() {
        try {
            logger = Flogger.instance;
            mysql = Database.getConnection();
        } catch (SQLException e) {
            logger.error(e);
        }

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
