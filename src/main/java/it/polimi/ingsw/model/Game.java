package it.polimi.ingsw.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class Game {

    private static Game instance;   //Singleton pattern
    private final Turn turn = new Turn();
    private ArrayList<Player> playerList = new ArrayList<>();
    private int playerNumber = 1;
    private Board board = new Board();
    private Deck godsDeck;
    private Player challengerPlayer;
    private ArrayList<Color> colorList = new ArrayList<Color>() {{
        add(Color.ANSI_BRIGHT_CYAN);
        add(Color.ANSI_RED);
        add(Color.ANSI_YELLOW);
    }};

    private Game() {

    }

    public Turn getTurn() {
        return turn;
    }

    public Board getBoard() {
        return board.clone();
    }

    public static Game getInstance() {
        if (instance == null)
            instance = new Game();
        return instance;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public String getAvailableColor() {
        StringBuilder temp = new StringBuilder();
        for (Color color : colorList) {
            //TODO: Sarà da spostare nella view
            temp.append(" ").append(color.getEscape()).append(color.getColorAsString(color)).append(Color.RESET).append(" or");
        }
        temp.replace(temp.length() - 2, temp.length(), "");
        return String.valueOf(temp);
    }

    public ArrayList<Color> getColorList() {
        return colorList;
    }

    public void removeColor(Color delete) {
        if (!colorList.isEmpty())
            colorList.remove(delete);
    }

    public void addPlayer(Player newPlayer) throws IllegalAccessException {
        if (checkPlayer(newPlayer) && playerList.size() < playerNumber) {
            playerList.add(newPlayer);
        } else
            throw new IllegalAccessException();
    }

    private boolean checkPlayer(Player newPlayer) {
        return !playerList.contains(newPlayer);
    }

    //TODO: Sfruttare il metodo in turn
    private void setYoungestPlayer() {
        this.playerList.sort(Comparator.comparing(Player::getBirthday).reversed());   //mette già in ordine di età
        this.challengerPlayer = playerList.get(0);
    }

    protected void removePlayer(final Player playerToDelete) {
        playerList.remove(playerToDelete);
        throw new IllegalArgumentException(); //TODO: Check: ??
    }

    public ArrayList<Player> getPlayerList() {
        return this.playerList;
    }

    public void challenge() throws IOException {
        //mostrare alla view del challenger tutti gli dei
        setYoungestPlayer();
        godsDeck = new Deck();
        challengerPlayer = playerList.get(0);
        godsDeck.printAllDeck();
    }
}


