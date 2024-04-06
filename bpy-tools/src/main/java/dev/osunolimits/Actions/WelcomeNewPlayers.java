package dev.osunolimits.Actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedAuthor;
import commons.marcandreher.Cache.Action.DatabaseAction;
import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.WebHook;
import dev.osunolimits.App;

public class WelcomeNewPlayers extends DatabaseAction {

    private final String CHECK_LATEST_SQL = "SELECT `id`, `name` FROM `users` ORDER BY `users`.`id` DESC LIMIT 1";

    @Override
    public void executeAction(Flogger logger) {
        super.executeAction(logger);

        try {
            ResultSet latestUserSet = mysql.Query(CHECK_LATEST_SQL);

            while (latestUserSet.next()) {
                if (!readFromFile().contains(latestUserSet.getString("id"))) {
                    WebHook webHook = new WebHook(App.dotenv.get("WELCOMENEWPLAYERS_HOOK"));

                    EmbedAuthor embedAuthor = new EmbedAuthor(latestUserSet.getString("name"),
                            App.dotenv.get("AVATARSRV") + "/u/" + latestUserSet.getString("id"),
                            App.dotenv.get("DOMAIN") + "/u/" + latestUserSet.getString("id"));

                    WebhookEmbed webhookEmbed = new WebhookEmbed(null, null,
                            "Welcome to " + App.dotenv.get("SRVNAME") + " [" + latestUserSet.getString("name") + "]("
                                    + App.dotenv.get("DOMAIN") + "/u/" + latestUserSet.getString("id") + ")",
                            null, null, null, null, embedAuthor, new ArrayList<>());
                    webHook.setWebHookEmbed(webhookEmbed);
                    webHook.send();

                    writeToFile(latestUserSet.getString("id"));
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

    }

    public static void writeToFile(String text) {
        String filePath = "/tmp/bpy-tools/last_post_user.dat";

        try {
            File directory = new File("/tmp/bpy-tools");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            FileWriter writer = new FileWriter(filePath, false); 
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static String readFromFile() {
        String filePath = "/tmp/bpy-tools/last_post_user.dat";
        StringBuilder content = new StringBuilder();
        if (!new File(filePath).exists())
            return "";

        try {
            FileReader reader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }

            bufferedReader.close();
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }

        return content.toString();
    }

}
