package dev.osunolimits.Commands;

import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Input.Command;
import dev.osunolimits.Utils.BanchoPlayer;
import dev.osunolimits.Utils.BanchoScraper;

public class GetOnlinePlayers implements Command {

    @Override
    public void executeAction(String[] args, Flogger logger) {
        BanchoScraper scraper = new BanchoScraper();
        for (BanchoPlayer banchoPlayer : scraper.getOnlinePlayers()) {
            System.out.println(banchoPlayer.toString());
        }
    }

    @Override
    public String getAlias() {
        return "online";
    }

    @Override
    public String getDescription() {
        return "Get online players";
    }

    @Override
    public String getName() {
        return "online";
    }

}
