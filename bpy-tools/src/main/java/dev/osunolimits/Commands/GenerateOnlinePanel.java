package dev.osunolimits.Commands;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Commons.Flogger.Prefix;
import commons.marcandreher.Input.Command;
import dev.osunolimits.App;
import dev.osunolimits.Modules.PostOnlinePanel;

public class GenerateOnlinePanel implements Command {

    @Override
    public void executeAction(String[] args, Flogger logger) {
        if(App.failedConnection)return;
        try {
            
            File outputFile = new File("output_image.png");
            BufferedImage img = PostOnlinePanel.genPanel();
            if(img == null) {
                logger.log(Prefix.ERROR, "Generating panel failed", 0);
                return;
            }
            ImageIO.write(PostOnlinePanel.genPanel(), "png", outputFile);
    
            System.out.println("Image manipulation complete. Output saved to: " + outputFile.getAbsolutePath());
    
        } catch (Exception e) {
            logger.error(e);
            App.failedConnection = true;
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
