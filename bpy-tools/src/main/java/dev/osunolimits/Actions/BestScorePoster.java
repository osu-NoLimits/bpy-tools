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

        private final String QUERY_SQL = "SELECT `scores`.`id` AS `score_id`, `maps`.`id` AS `beatmap_id`, `scores`.`status` AS `score_status`, `maps`.`status` AS `map_status`, `scores`.`mode` AS `mode`, `title` AS `map_title`, `artist` AS `map_artist`, `userid` FROM `scores` LEFT JOIN `maps` ON `maps`.`md5` = `map_md5` WHERE `scores`.`status` = 2 AND `maps`.`status` = 2 ORDER BY `scores`.`pp` DESC LIMIT 1";
        private final String CHECK_POSTED_SQL = "SELECT COUNT(`score_id`) AS `is_posted` FROM `bt_posted_scores` WHERE `score_id` = ?";
        private final String EXEC_POST_SQL = "INSERT INTO `bt_posted_scores`(`score_id`, `pp`) VALUES (?,?)";

        @Override
        public void executeAction(Flogger log) {
                super.executeAction(log);
                if (Boolean.parseBoolean(App.dotenv.get("BESTSCOREPOSTER")) == false)
                        return;

                try {
                        ResultSet bestScoreSet = mysql.Query(QUERY_SQL);
                        while (bestScoreSet.next()) {
                                ResultSet isPostedSet = mysql.Query(CHECK_POSTED_SQL, bestScoreSet.getString("score_id"));
                                while (isPostedSet.next() && isPostedSet.getInt("is_posted") == 0) {
                                        JSONObject apiRequestScore = App.parseJsonResponse(new GetRequest(App.dotenv.get("APIURL") + "/get_score_info?id=" + bestScoreSet.getString("score_id")).send("bpy-tools"));
                                        JSONObject apiScore = (JSONObject) apiRequestScore.get("score");
                                        Double pp = (Double) apiScore.get("pp");

                                        String beatmapId = bestScoreSet.getString("beatmap_id");
                                        String beatmapTitle = bestScoreSet.getString("map_title");
                                        String beatmapArtist = bestScoreSet.getString("map_artist");

                                        log.log(App.dotenv.get("APIURL") + "/get_player_info?id=" + bestScoreSet.getString("userid") + "?scope=info", 0);
                                        JSONObject apiRequestUser = App
                                                        .parseJsonResponse(new GetRequest(App.dotenv.get("APIURL")
                                                                        + "/get_player_info?id="
                                                                        + bestScoreSet.getString("userid")
                                                                        + "&scope=info").send("bpy-tools"));
                                        JSONObject apiPlayer = (JSONObject) apiRequestUser.get("player");
                                        JSONObject apiInfo = (JSONObject) apiPlayer.get("info");
                                        WebHook webHook = new WebHook(App.dotenv.get("BESTSCOREPOSTER_HOOK"));
                                        EmbedAuthor embedAuthor = new EmbedAuthor((String) apiInfo.get("name"),
                                                        App.dotenv.get("AVATARSRV") + "/u/"
                                                                        + bestScoreSet.getString("userid"),
                                                        App.dotenv.get("DOMAIN") + "/u/"
                                                                        + bestScoreSet.getString("userid"));
                                        List<EmbedField> embedFields = new ArrayList<>();
                                        EmbedTitle embedTitle = new EmbedTitle(
                                                        "New Top Score " + pp + "pp on " + beatmapTitle,
                                                        App.dotenv.get("DOMAIN") + "/beatmaps/" + beatmapId);

                                        String description = "Artist: " + beatmapArtist + "\nBeatmap: " + beatmapTitle
                                                        + "\nPP: " + pp + " | ACC: " + (Double) apiScore.get("acc")
                                                        + "\nMax Combo: " + (Long) apiScore.get("max_combo")
                                                        + " | Grade: " + (String) apiScore.get("grade");

                                        WebhookEmbed webhookEmbed = new WebhookEmbed(null, 2303786, description, null,
                                                        null, null, embedTitle,
                                                        embedAuthor, embedFields);

                                        webHook.setWebHookEmbed(webhookEmbed);
                                        webHook.send();

                                        mysql.Exec(EXEC_POST_SQL, bestScoreSet.getString("score_id"), pp.toString());
                                }
                        }
                } catch (Exception e) {
                        log.error(e);
                }
        }

}
