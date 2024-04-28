package dev.osunolimits;

import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import commons.marcandreher.Commons.Database;
import commons.marcandreher.Commons.Database.ServerTimezone;
import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.WebServer;
import commons.marcandreher.Input.CommandHandler;
import dev.osunolimits.Commands.CheckServices;
import dev.osunolimits.Commands.CrawlMaps;
import dev.osunolimits.Commands.GenerateOnlinePanel;
import dev.osunolimits.Commands.GetOnlinePlayers;
import dev.osunolimits.ModuleLoader.ModuleRegister;
import dev.osunolimits.Modules.BestScorePoster;
import dev.osunolimits.Modules.PostOnlinePanel;
import dev.osunolimits.Modules.WelcomeNewPlayers;
import dev.osunolimits.Utils.TimeoutChecker;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * bpy-tools by Marc Andre Herpers
 */
public class App {

    public static Dotenv dotenv;
    public static boolean failedConnection = false;

    public static void main(String[] args) {
        dotenv = Dotenv.load();

        Integer logLevel = Integer.parseInt(dotenv.get("LOGLEVEL"));

        String dbHost = dotenv.get("DB_HOST");
        String dbUName = dotenv.get("DB_USERNAME");
        String dbPassword = dotenv.get("DB_PASSWORD");
        String dbDatbase = dotenv.get("DB_DATABASE");

        Flogger flogger = new Flogger(logLevel);
        flogger.setInstanceName("bpy-tools");

        Database database = new Database();
        WebServer webserver = new WebServer(flogger, (short) 3);

        CommandHandler cmd = new CommandHandler(flogger);

        webserver.setThreadPool(0, 5, 3000);

        database.setDefaultSettings();
        database.setMaximumPoolSize(5);
        database.setConnectionTimeout(3000);
        database.connectToMySQL(dbHost, dbUName, dbPassword, dbDatbase, ServerTimezone.UTC);

        System.out.println(" ____  ____ ___  _    _____  ____  ____  _     ____ \r\n" + //
                "/  _ \\/  __\\\\  \\//   /__ __\\/  _ \\/  _ \\/ \\   / ___\\\r\n" + //
                "| | //|  \\/| \\  /_____ / \\  | / \\|| / \\|| |   |    \\\r\n" + //
                "| |_\\\\|  __/ / / \\____\\| |  | \\_/|| \\_/|| |_/\\\\___ |\r\n" + //
                "\\____/\\_/   /_/        \\_/  \\____/\\____/\\____/\\____/\r\n" + //
                "                                                     ");

        ModuleRegister.addModuleToRegister(new BestScorePoster(5, TimeUnit.MINUTES));
        ModuleRegister.addModuleToRegister(new WelcomeNewPlayers(2, TimeUnit.MINUTES));
        ModuleRegister.addModuleToRegister(new PostOnlinePanel(2, TimeUnit.MINUTES));

        ModuleRegister.loadModules();
        cmd.registerCommand(new CrawlMaps());
        cmd.registerCommand(new GetOnlinePlayers());
        cmd.registerCommand(new GenerateOnlinePanel());
        cmd.registerCommand(new CheckServices());
        cmd.initialize();

        Thread timeoutChecker = new TimeoutChecker();
        timeoutChecker.setName("TIMEOUT-CHECKER");
        timeoutChecker.start();

    }

    public static JSONObject parseJsonResponse(String jsonResponse) throws Exception {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(jsonResponse);
    }
}
