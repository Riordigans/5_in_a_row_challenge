package Server;

//Player class to keep track of their name, token and number.
public class Player{

    private int playerNum;
    private String playerName = "";
    private String playerToken;

    public Player(int num, String token){
        this.playerNum = num;
        this.playerToken = token;
    }

    public int getNum(){
        return this.playerNum;
    }

    public String getToken(){
        return this.playerToken;
    }

    public void setToken(String token){
        this.playerToken = token;
    }

    public String getName(){
        return this.playerName;
    }

    public void setName(String name){
        this.playerName = name;
    }
}