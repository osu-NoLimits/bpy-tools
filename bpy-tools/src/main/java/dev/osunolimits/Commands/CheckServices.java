package dev.osunolimits.Commands;

import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Input.Command;
import dev.osunolimits.App;

public class CheckServices implements Command {

    @Override
    public void executeAction(String[] args, Flogger logger) {
        App.failedConnection = true;
    }

    @Override
    public String getAlias() {
        return "checkservices";
    }

    @Override
    public String getDescription() {
        return "Check you're services";
    }

    @Override
    public String getName() {
        return "checkservices";
    }

}
