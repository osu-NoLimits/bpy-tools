package dev.osunolimits.Utils;

import java.sql.ResultSet;

import commons.marcandreher.Commons.Database;
import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.GetRequest;
import commons.marcandreher.Commons.MySQL;
import commons.marcandreher.Commons.Flogger.Prefix;
import commons.marcandreher.Utils.Color;
import dev.osunolimits.App;

public class TimeoutChecker extends Thread {

    private Flogger logger = Flogger.instance;
    private boolean failed = false;

    @Override
    public void run() {
        while (true) {
            failed = false;
            try {
                if (!App.failedConnection) {
                    Thread.sleep(2000);
                    continue;
                }
                System.out.println();
                try {
                    MySQL mysql = Database.getConnection();
                    ResultSet rs = mysql.Query("SELECT `id` FROM `users` LIMIT 1");
                    while (rs.next())
                        continue;
                    mysql.close();
                    logger.log(Prefix.INFO, "MySQL " + Color.GREEN + " OK" + Color.RESET, 0);
                } catch (Exception e) {
                    logger.log(Prefix.INFO, "MySQL " + Color.RED + " FAILED" + Color.RESET, 0);
                    failed = true;
                }

                try {
                    new GetRequest(App.dotenv.get("APIURL"));
                    logger.log(Prefix.INFO, "API " + Color.GREEN + " OK" + Color.RESET, 0);
                } catch (Exception e) {
                    logger.log(Prefix.INFO, "API " + Color.RED + " FAILED" + Color.RESET, 0);
                    failed = true;
                }

                try {
                    new GetRequest(App.dotenv.get("BANCHO"));
                    logger.log(Prefix.INFO, "BANCHO " + Color.GREEN + " OK" + Color.RESET, 0);
                } catch (Exception e) {
                    logger.log(Prefix.INFO, "BANCHO " + Color.RED + " FAILED" + Color.RESET, 0);
                    failed = true;
                }

                try {
                    new GetRequest(App.dotenv.get("AVATARSRV"));
                    logger.log(Prefix.INFO, "AVATARS " + Color.GREEN + " OK" + Color.RESET, 0);
                } catch (Exception e) {
                    logger.log(Prefix.INFO, "AVATARS " + Color.RED + " FAILED" + Color.RESET, 0);
                    failed = true;
                }

                try {
                    new GetRequest(App.dotenv.get("DOMAIN"));
                    logger.log(Prefix.INFO, "DOMAIN " + Color.GREEN + " OK" + Color.RESET, 0);
                } catch (Exception e) {
                    logger.log(Prefix.INFO, "DOMAIN " + Color.RED + " FAILED" + Color.RESET, 0);
                    failed = true;
                }

                if (failed == false) {
                    App.failedConnection = false;
                }

                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error(e);
            }

        }
    }

}
