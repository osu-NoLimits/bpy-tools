package dev.osunolimits.Utils;

public class BanchoPlayer {
    private int id;
    private String username;
    private boolean isBot;

    public BanchoPlayer(int id, String username, boolean isBot) {
        this.id = id;
        this.username = username;
        this.isBot = isBot;
    }

    public BanchoPlayer(BanchoPlayer p) {
        id = p.id;
        username = p.username;
        isBot = p.isBot;
    }

    public BanchoPlayer() {
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(int userid) {
        id = userid;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", isBot=" + isBot +
                '}';
    }
}
