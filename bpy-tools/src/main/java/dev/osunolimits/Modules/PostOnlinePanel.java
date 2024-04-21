package dev.osunolimits.Modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;

import commons.marcandreher.Cache.Action.DatabaseAction;
import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.GetRequest;
import dev.osunolimits.App;
import dev.osunolimits.ModuleLoader.ModuleInstance;
import dev.osunolimits.Utils.BanchoPlayer;
import dev.osunolimits.Utils.BanchoScraper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

public class PostOnlinePanel extends ModuleInstance {

    public static JDA jdaInstance;

    @Override
    public void initialize() {
        super.initialize();
        try {
            if (jdaInstance == null) {
                jdaInstance = JDABuilder.createDefault(App.dotenv.get("ONLINEPANEL_TOKEN")).build().awaitReady();
            }
        } catch (Exception e) {
            Flogger.instance.error(e);
        }
    }

    public PostOnlinePanel(int duration, TimeUnit timeUnit) {
        super(duration, timeUnit);
    }

    @Override
    public DatabaseAction run() {
        return new DatabaseAction() {
            @Override
            public void executeAction(Flogger logger) {
                super.executeAction(logger);

                try {
                    TextChannel channel = jdaInstance.getTextChannelById(App.dotenv.get("ONLINEPANEL_CHANNEL"));
            
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(genPanel(), "png", baos);
                    byte[] imageData = baos.toByteArray();
        
                    channel.getIterableHistory().takeAsync(1).thenAccept(messages -> {
                        if (!messages.isEmpty()) {
                            Message lastMessage = messages.get(0);
                            lastMessage.editMessage("").setFiles(FileUpload.fromData(imageData, "online.png")).queue();
                        } else {
                            channel.sendMessage("").addFiles(FileUpload.fromData(imageData, "online.png")).queue();
                        }
                    });
            
                } catch (IOException e) {
                   logger.error(e);
                }
            }

        };
    }

    @Override
    public String getName() {
        return "PostOnlinePanel";
    }

    @Override
    public String getConfigToggleKey() {
        return "ONLINEPANEL";
    }



    public static BufferedImage genPanel() {
        try {
            File baseImageFile = new File("base_image.png");
            BufferedImage baseImage = ImageIO.read(baseImageFile);
            Graphics2D g2d = baseImage.createGraphics();
            BanchoScraper scraper = new BanchoScraper();
            List<BanchoPlayer> bpyPlayers = scraper.getOnlinePlayers();
     
            for(int i = 0; i < bpyPlayers.size(); i++) {
                BanchoPlayer p = bpyPlayers.get(i);
                int textX = 100;
                int textY = 0;
                if(i > 9) { 
                    textX = 450;
                    textY = 225 + (50 * (i-10));
                } else {
                    textY = 225 + (50 * (i));
                }
                
                g2d.setColor(Color.WHITE);
                String username;
                String rank;
                if(!p.isBot()) {
                    String out = new GetRequest(App.dotenv.get("APIURL")+ "/get_player_info?scope=all&id="+ p.getId()).send("bpy-tools");
                    JSONObject obj = App.parseJsonResponse(out);
                    JSONObject usernameObj = (JSONObject)obj.get("player");
                    JSONObject infoObj = (JSONObject)usernameObj.get("info");
                    username = (String) infoObj.get("name");

                    JSONObject playerObj = (JSONObject) obj.get("player");
                   
                    JSONObject statsObj = (JSONObject) playerObj.get("stats");
                    
                    try {
                        JSONObject stdObj = (JSONObject) statsObj.get("0");
                        long rankTmp = (Long) stdObj.get("rank");
                        if(rankTmp == (long)0) {
                            rank = " • #-";
                        }else {
                            rank = " • #"+rankTmp;
                        }
                        
                    } catch (Exception e) {
                        rank = " • -";
                        e.printStackTrace();
                    }
                   
                }else{
                    rank = "";     
                    username = p.getUsername() + " Ꙩ";
                }

                File avatarImageFile = new File(App.dotenv.get("AVATAR_FOLDER") + p.getId() + ".jpg");
                
                if (!avatarImageFile.exists()) {
                    avatarImageFile = new File(App.dotenv.get("AVATAR_FOLDER") + p.getId() + ".png");
                    if(!avatarImageFile.exists()) {
                        avatarImageFile = new File(App.dotenv.get("AVATAR_FOLDER") + "default.jpg");
                    }
                   
                }
                BufferedImage avatarImage = ImageIO.read(avatarImageFile);

                BufferedImage scaledAvatarImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
                
                Graphics2D g2dScaled = scaledAvatarImage.createGraphics();
              
                g2dScaled.drawImage(avatarImage, 0, 0, 50, 50, null);
                g2dScaled.dispose(); 
                
                g2d.drawImage(scaledAvatarImage, textX - 65, textY - 30, null);
                
                g2d.setFont(new Font("Calibri", Font.PLAIN, 20)); 
                g2d.drawString(username + rank, textX, textY);
                if(i == 19)break;
            }

            g2d.dispose();
           return baseImage;    

    
        } catch (Exception e) {
            Flogger.instance.error(e);
        }
        return null;
    }
    
}
