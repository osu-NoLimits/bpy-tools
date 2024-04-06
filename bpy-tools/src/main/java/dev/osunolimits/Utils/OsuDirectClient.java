package dev.osunolimits.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.Flogger.Prefix;
import commons.marcandreher.Commons.GetRequest;
import commons.marcandreher.Commons.MySQL;
import dev.osunolimits.App;

public class OsuDirectClient {

    private MySQL mysql;
    private JSONParser parser;

    public OsuDirectClient(MySQL mysql, JSONParser parser) {
        this.mysql = mysql;
        this.parser = parser;
    }

    public int downloadBeatmapsFromQuery(Integer crawlAmount, Integer baseOffset, String query) throws IOException, ParseException, InterruptedException {
        Boolean securityOffSwitch = false;
        String toDeleteBeatmapSet = null;
        Integer downloaded = 0;
        try {
            for (int i = 0; i < crawlAmount; i++) {
                Object requestOutput = parser
                                .parse(new GetRequest("https://api.osu.direct/v2/search?query=" + query.replaceAll(" ", "%20")
                                        + "&mode=0&amount=" + crawlAmount + "&offset=" + (baseOffset + i) + "&status=1")
                                        .send("bpy-tools"));
        
                        JSONArray requestOutputJSON = (JSONArray) requestOutput;
        
                        for (Object beatmapSet : requestOutputJSON) {
                            
                            JSONObject beatmapSetObject = (JSONObject) beatmapSet;
                            Integer beatmapSetId = Integer.parseInt(String.valueOf((Long) beatmapSetObject.get("id")));
        
                            if (!beatmapSetInDatabase(beatmapSetId, mysql)) {
                                addBeatmapSetInDatabase(beatmapSetId, mysql);
                                JSONArray beatmaps = (JSONArray) beatmapSetObject.get("beatmaps");
                                for (Object beatmap : beatmaps) {
                                    JSONObject beatmapObject = (JSONObject) beatmap;
                                    String beatmapMode = (String) beatmapObject.get("mode");
                                    if (!beatmapMode.contains("osu"))
                                        continue;
        
                                    String md5 = (String) beatmapObject.get("checksum");
                                    if (!beatmapInDatabase(md5, mysql)) {
                                        String beatmapId = String.valueOf((Long) beatmapObject.get("id"));
                                        String beatmapSetIdStr = String.valueOf(beatmapSetId);
                                        Integer status = 2;
                                        String artist = (String) beatmapSetObject.get("artist");
                                        String title = (String) beatmapSetObject.get("title");
                                        String creator = (String) beatmapSetObject.get("creator");
                                        String version = (String) beatmapObject.get("version");
                                        String filename = (String) beatmapSetObject.get("title_unicode") + " (" + creator
                                                + ") (" + version + ").osu";
                                        String lastUpdated = (String) beatmapObject.get("last_updated");
                                        Long totalLength = (Long) beatmapObject.get("total_length");
                                        Long maxCombo = (Long) beatmapObject.get("max_combo");
                                        String frozen = "0";
                                        String plays = "0";
                                        String passes = "0";
                                        String mode = "0";
                                        String bpm = convertObject((Object) beatmapObject.get("bpm"));
                                        String cs = convertObject((Object) beatmapObject.get("cs"));
                                        String ar = convertObject((Object) beatmapObject.get("ar"));
                                        String od = convertObject((Object) beatmapObject.get("accuracy"));
                                        String hd = convertObject((Object) beatmapObject.get("drain"));
                                        String diff = convertObject((Object) beatmapObject.get("difficulty_rating"));
        
                                        if (!downloadOsuFile("https://api.osu.direct/osu/" + beatmapId, beatmapId + ".osu")) {
                                            securityOffSwitch = true;
                                            toDeleteBeatmapSet = beatmapSetIdStr;
                                            throw new IOException("Couldn't download beatmap");
        
                                        }
        
                                        mysql.Exec(
                                                "INSERT INTO `maps`(`server`, `last_update`, `id`, `set_id`, `status`, `md5`, `artist`, `title`, `version`, `creator`, `filename`, `total_length`, `max_combo`, `frozen`, `plays`, `passes`, `mode`, `bpm`, `cs`, `ar`, `od`, `hp`, `diff`) VALUES ('osu!', STR_TO_DATE(?, '%Y-%m-%dT%H:%i:%sZ'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                                lastUpdated, beatmapId, beatmapSetIdStr, String.valueOf(status), md5, artist,
                                                title, version,
                                                creator, filename, String.valueOf(totalLength),
                                                String.valueOf(maxCombo), frozen, plays, passes, mode, bpm, cs, ar, od, hd,
                                                diff);
                                        downloaded++;
                                        Flogger.instance.log(Prefix.API, "Child successfully downloaded Beatmap (" + beatmapId + ")", 0);
        
                                    }
                                    if (securityOffSwitch)
                                        break;
        
                                }
                            }
        
                        }
                    }
              
        } catch (Exception e) {
            if (toDeleteBeatmapSet != null) {
                mysql.Exec("DELETE FROM `mapsets` WHERE `id` = ?", toDeleteBeatmapSet);
                Flogger.instance.log(Prefix.INFO, "Deleted not finished mapset", 0);
            }
    
        }
        return downloaded;
       
        
         
    }

    public String convertObject(Object obj) {
        if (obj instanceof Long) {
            return String.valueOf((Long) obj);
        } else if (obj instanceof Double) {
            return String.valueOf((Double) obj);
        } else if (obj instanceof String) {
            return String.valueOf(obj);
        }
        return "";
    }


     public Boolean beatmapSetInDatabase(Integer beatmapId, MySQL mysql) {

        ResultSet beatmapSet = mysql.Query("SELECT COUNT(`id`) AS `exists` FROM `mapsets` WHERE `id` = ?",
                String.valueOf(beatmapId));
        try {
            while (beatmapSet.next()) {
                if (beatmapSet.getInt("exists") != 0) {
                    Flogger.instance.log(Prefix.WARNING, "BeatmapSet (" + beatmapId + ") already covered...", 0);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Flogger.instance.log(Prefix.API, "BeatmapSet (" + beatmapId + ") is missing searching informations...", 0);
        return false;
    }

    public Boolean beatmapInDatabase(String md5, MySQL mysql) {

        ResultSet beatmapSet = mysql.Query("SELECT COUNT(`id`) AS `exists` FROM `maps` WHERE `md5` = ?", md5);
        try {
            while (beatmapSet.next()) {
                if (beatmapSet.getInt("exists") != 0) {
                    Flogger.instance.log(Prefix.WARNING, "Beatmap (" + md5 + ") already covered...", 0);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Flogger.instance.log(Prefix.API, "Beatmap (" + md5 + ") is missing searching informations...", 0);
        return false;
    }

    public void addBeatmapSetInDatabase(Integer beatmapId, MySQL mysql) {
        String execSql = "INSERT INTO `mapsets`(`server`, `id`, `last_osuapi_check`) VALUES ('osu!',?,CURRENT_TIMESTAMP())";
        mysql.Exec(execSql, String.valueOf(beatmapId));
    }

    public Boolean downloadOsuFile(String location, String name) {

        String destinationDirectory = App.dotenv.get("BEATMAP_FOLDER");

        try {
            Path directory = Paths.get(destinationDirectory);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            Path destinationPath = Paths.get(destinationDirectory, name);

            @SuppressWarnings("deprecation")
            URL url = new URL(location);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(destinationPath.toFile());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            Flogger.instance.log(Prefix.INFO, "File downloaded to " + name, 0);
            return true;
        } catch (Exception e) {

            return false;
        }

    }
    
}
