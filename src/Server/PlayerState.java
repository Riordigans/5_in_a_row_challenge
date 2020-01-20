package Server;

import java.util.ArrayList;
import java.util.List;

//Class to keep track of the active players. Server uses this class to add players, set their names,
//check for valid tokens and for seeing who the current valid player is.
public class PlayerState {
    private List <Player> playerList = new ArrayList<Player>();
    private int currentPlayer = 0;

    void addPlayer(Player player) {
        playerList.add(player);
    }

    void addPlayerName(String token, String name){
        for(Player p : playerList){
            if (p.getToken().equals(token)){
                p.setName(name);
            }
        }
    }

    public int getNumPlayers(){
        return playerList.size();
    }

    Player currentPlayer() {
        return playerList.get(currentPlayer);
    }

    Player nextPlayer() {
        if (currentPlayer == 0) {
            currentPlayer = 1;
        } else {
            currentPlayer = 0;
        }
        return playerList.get(currentPlayer);
    }

    Player otherPlayer() {
        int otherPlayer = 0;
        if (currentPlayer == 0) {
            otherPlayer = 1;
        }
        return playerList.get(otherPlayer);
    }

    ArrayList<String> getAllTokens(){
        ArrayList<String> tokens = new ArrayList();

        for(Player p : playerList){
            tokens.add(p.getToken());
        }
        return tokens;
    }
}