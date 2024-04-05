package dev.osunolimits.Actions;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedAuthor;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedField;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedTitle;
import commons.marcandreher.Cache.Action.DatabaseAction;
import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.GetRequest;
import commons.marcandreher.Commons.WebHook;
import dev.osunolimits.App;

public class BestScorePoster extends DatabaseAction {

    @Override
    public void executeAction(Flogger log) {
        super.executeAction(log);
        if (Boolean.parseBoolean(App.dotenv.get("BESTSCOREPOSTER")) == false)
            return;

        try {
            ResultSet bestScoreSet = mysql.Query(
                    "SELECT * FROM `scores` WHERE STATUS = 2 AND `mode` = 0 ORDER BY `scores`.`pp` DESC LIMIT 1");
            while (bestScoreSet.next()) {
                ResultSet isPostedSet = mysql.Query(
                        "SELECT COUNT(`score_id`) AS `is_posted` FROM `bt_posted_scores` WHERE `score_id` = ?",
                        bestScoreSet.getString("id"));
                while (isPostedSet.next() && isPostedSet.getInt("is_posted") == 0) {

                    JSONObject apiRequestScore = App.parseJsonResponse(new GetRequest(
                            App.dotenv.get("APIURL") + "/get_score_info?id=" + bestScoreSet.getString("id"))
                            .send("bpy-tools"));
                    JSONObject apiScore = (JSONObject) apiRequestScore.get("score");
                    Double pp = (Double) apiScore.get("pp");
                    String map_md5 = (String) apiScore.get("map_md5");

                    String beatmapId = "";
                    String beatmapTitle = "";
                    String beatmapArtist = "";

                    ResultSet extraInfoSet = mysql.Query("SELECT `id`, `title`,`artist` FROM `maps` WHERE `md5` = ?",
                            map_md5);
                    while (extraInfoSet.next()) {
                        beatmapId = extraInfoSet.getString("id");
                        beatmapTitle = extraInfoSet.getString("title");
                        beatmapArtist = extraInfoSet.getString("artist");
                    }

                    log.log(App.dotenv.get("APIURL") + "/get_player_info?id=" + bestScoreSet.getString("userid")
                            + "?scope=info", 0);
                    JSONObject apiRequestUser = App
                            .parseJsonResponse(new GetRequest(App.dotenv.get("APIURL") + "/get_player_info?id="
                                    + bestScoreSet.getString("userid") + "&scope=info").send("bpy-tools"));
                    JSONObject apiPlayer = (JSONObject) apiRequestUser.get("player");
                    JSONObject apiInfo = (JSONObject) apiPlayer.get("info");
                    WebHook webHook = new WebHook(App.dotenv.get("BESTSCOREPOSTER_HOOK"));
                    EmbedAuthor embedAuthor = new EmbedAuthor((String) apiInfo.get("name"),
                            App.dotenv.get("AVATARSRV") + "/u/" + bestScoreSet.getString("userid"),
                            App.dotenv.get("DOMAIN") + "/u/" + bestScoreSet.getString("userid"));
                    List<EmbedField> embedFields = new ArrayList<>();
                    EmbedTitle embedTitle = new EmbedTitle("New Top Score " + pp + "pp on " + beatmapTitle,
                            App.dotenv.get("DOMAIN") + "/beatmaps/" + beatmapId);
                    
                    String description= "Artist: " + beatmapArtist+ "\nBeatmap: " + beatmapTitle + "\nPP: " + pp + " | ACC: " + (Double) apiScore.get("acc")
                    + "\nMax Combo: " + (Long) apiScore.get("max_combo") + " | Grade: " + (String) apiScore.get("grade");
                    
                    WebhookEmbed webhookEmbed = new WebhookEmbed(null, 2303786, description, null, null, null, embedTitle,
                            embedAuthor, embedFields);

                    webHook.setWebHookEmbed(webhookEmbed);
                    webHook.send();

                    mysql.Exec("INSERT INTO `bt_posted_scores`(`score_id`, `pp`) VALUES (?,?)", bestScoreSet.getString("id"), pp.toString());


                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

}
