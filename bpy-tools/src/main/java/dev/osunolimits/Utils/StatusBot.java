package dev.osunolimits.Utils;

import commons.marcandreher.Commons.Flogger;
import dev.osunolimits.App;
import dev.osunolimits.Actions.PostOnlinePanel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class StatusBot {

    public static JDA jdaInstance;

    public static void initialize() {
        try {
            if (jdaInstance == null) {
                jdaInstance = JDABuilder.createDefault(App.dotenv.get("ONLINEPANEL_TOKEN")).build().awaitReady();
            }
            new PostOnlinePanel().executeAction(Flogger.instance);;
        } catch (Exception e) {
            Flogger.instance.error(e);
        }
    }
}
