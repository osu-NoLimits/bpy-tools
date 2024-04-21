package dev.osunolimits.Commands;

import java.io.File;

import javax.imageio.ImageIO;

import commons.marcandreher.Commons.Flogger;
import commons.marcandreher.Input.Command;
import dev.osunolimits.Modules.PostOnlinePanel;

public class GenerateOnlinePanel implements Command {

    @Override
    public void executeAction(String[] args, Flogger logger) {
        try {
            
            File outputFile = new File("output_image.png");
            ImageIO.write(PostOnlinePanel.genPanel(), "png", outputFile);
    
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
