package dev.osunolimits.Commands;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;

import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.GetRequest;
import commons.marcandreher.Input.Command;
import dev.osunolimits.App;
import dev.osunolimits.Utils.BanchoPlayer;
import dev.osunolimits.Utils.BanchoScraper;

public class GenerateOnlinePanel implements Command {

    @Override
    public void executeAction(String[] args, Flogger logger) {
        try {
            File baseImageFile = new File("base_image.png");
            BufferedImage baseImage = ImageIO.read(baseImageFile);
            Graphics2D g2d = baseImage.createGraphics();
            BanchoScraper scraper = new BanchoScraper();
     
            for(int i = 0; i < scraper.getOnlinePlayers().size(); i++) {
                BanchoPlayer p = scraper.getOnlinePlayers().get(i);
                int textX = 100;
                int textY = 0;
                if(i > 9) { 
                    textX = 450;
                    textY = 225 + (50 * (i-10));
                } else {
                    textY = 225 + (50 * (i));
                }
                
                g2d.setColor(Color.WHITE); // Set text 
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
                        rank = " • #"+rankTmp;
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
                    avatarImageFile = new File(App.dotenv.get("AVATAR_FOLDER") + "default.jpg");
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
            File outputFile = new File("output_image.png");
            ImageIO.write(baseImage, "png", outputFile);
    
            System.out.println("Image manipulation complete. Output saved to: " + outputFile.getAbsolutePath());
    
        } catch (Exception e) {
            logger.error(e);
        }


     
    }

    @Override
    public String getAlias() {
        return "genpanel";
    }

    @Override
    public String getDescription() {
        return "Get online players with an exported image";
    }

    @Override
    public String getName() {
        return "genpanel";
    }

}
