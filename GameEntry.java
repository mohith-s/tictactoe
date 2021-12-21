package androidsamples.java.tictactoe;

public class GameEntry {
    private String gameKey;
    private String email;

    GameEntry() {
    }

    GameEntry(String gameKey, String email) {
        this.gameKey = gameKey;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGameKey() {
        return gameKey;
    }

    public void setGameKey(String gameKey) {
        this.gameKey = gameKey;
    }
}
