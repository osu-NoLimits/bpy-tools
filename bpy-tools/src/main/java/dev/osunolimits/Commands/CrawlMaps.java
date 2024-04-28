package dev.osunolimits.Commands;

import java.io.IOException;
import java.sql.SQLException;

import org.json.simple.parser.JSONParser;

import commons.marcandreher.Commons.Database;
import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.MySQL;
import commons.marcandreher.Commons.Flogger.Prefix;
import commons.marcandreher.Input.Command;
import dev.osunolimits.App;
import dev.osunolimits.Utils.OsuDirectClient;

public class CrawlMaps implements Command {

    @Override
    public void executeAction(String[] args, Flogger logger) {
        if(App.failedConnection)return;

        JSONParser parser = new JSONParser();

        if (args.length == 1) {
            System.out.println(getAlias());
            return;
        }
        if (args.length >= 5) {
            System.out.println(getAlias());
            return;
        }

        String query = " ";

        int baseOffset = Integer.parseInt(args[2]);

        if (args.length == 4) {
            query = args[3];
        }

        MySQL mysql = null;
        try {
            mysql = Database.getConnection();
        } catch (SQLException e) {
            Flogger.instance.error(e);
            App.failedConnection = true;
            return;
        }

        try {
            OsuDirectClient osuDirectClient = new OsuDirectClient(mysql,parser);
            Integer downloadedMaps = osuDirectClient.downloadBeatmapsFromQuery(Integer.parseInt(args[1]), baseOffset, query);
            logger.log(Prefix.INFO, "Downloaded " + downloadedMaps + " maps", 0);
        } catch (IOException | org.json.simple.parser.ParseException | InterruptedException e) {
            logger.error(e);
        }

       
        mysql.close();
    }

 

    @Override
    public String getAlias() {
        return "crawlmaps <int:amount> <int:base_offset> <string:query>?";
    }

    @Override
    public String getDescription() {
        return "Crawl maps and fill up you're db";
    }

    @Override
    public String getName() {
        return "crawlmaps";
    }

   

}
