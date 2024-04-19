package dev.osunolimits.Utils;

import java.io.IOException;
import java.util.ArrayList;

import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.GetRequest;
import dev.osunolimits.App;

public class BanchoScraper {

    public ArrayList<BanchoPlayer> getOnlinePlayers() {
        ArrayList<BanchoPlayer> banchoPlayers = new ArrayList<>();
        try {
            String toScrape = new GetRequest(App.dotenv.get("BANCHO") + "/online").send("bpy-tools");
            String[] toScrapeLines = toScrape.split("\n");
            BanchoPlayer curBanchoPlayer = new BanchoPlayer();
            for (int i = 3; i < toScrapeLines.length - 2; i++) {
                String curLine = toScrapeLines[i];

                if (curLine.equals("users:") || curLine.equals("bots:")) {
                    curBanchoPlayer.setBot(curLine.equals("bots:"));
                    continue;
                }

                String[] userSplit = curLine.split(":");
                curBanchoPlayer.setUsername(userSplit[1].replaceFirst(" ", ""));

                String userid = userSplit[0].replace("(", "").replace(")", "").replaceAll(" ", "");
                curBanchoPlayer.setId(Integer.parseInt(userid));

                banchoPlayers.add(new BanchoPlayer(curBanchoPlayer));
            }
        } catch (IOException | InterruptedException e) {
            Flogger.instance.error(e);
        }
        return banchoPlayers;
    }

}
