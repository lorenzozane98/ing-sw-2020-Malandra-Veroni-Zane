package it.polimi.ingsw.model;

import it.polimi.ingsw.model.Color.PlayerColor;

import java.io.Serializable;
import java.util.ArrayList;

public class UpdateTurnMessage implements Serializable {

    private static final long serialVersionUID = 4116568860427433236L;
    private final Board boardCopy;
    private final String lastMovePerformedBy;
    private final TurnEvents.StartupActions nextStartupMove;
    private final TurnEvents.Actions nextMove;
    //TODO: Mosse precedenti (?)
    private final boolean startupPhase;
    private final Player currentPlayer;
    private final Worker currentWorker;
    private final ArrayList<PlayerColor> availableColor = new ArrayList<>(); //passiamo i colori disponibili in quel momento
    private final ArrayList<GodsCard> availableCards = new ArrayList<>();
//    private ArrayList<GodsCard> chosenGodsCards = new ArrayList<>(); //passiamo le carte fra cui scegliere


    public UpdateTurnMessage(TurnEvents.StartupActions nextStartupMove, Player currentPlayer, ArrayList<PlayerColor> playerColor) {
        this.startupPhase = true;
        this.nextStartupMove = nextStartupMove;
        this.currentPlayer = currentPlayer;
        this.availableColor.addAll(playerColor);
        this.currentWorker = null;
//        this.turn = turn;
        this.boardCopy = null;
        this.lastMovePerformedBy = null;
        this.nextMove = null;

    }

    public UpdateTurnMessage(Board boardCopy, String lastMovePerformedBy, TurnEvents.Actions nextMove, Player currentPlayer, Worker currentWorker) {
        this.startupPhase = false;
        this.nextMove = nextMove;
        this.currentPlayer = currentPlayer;
        this.currentWorker = currentWorker;
//        this.turn = turn;
        this.boardCopy = boardCopy;
        this.lastMovePerformedBy = lastMovePerformedBy;
        this.nextStartupMove = null;
    }

    public boolean isStartupPhase() {
        return startupPhase;
    }

    public TurnEvents.StartupActions getNextStartupMove() {
        return nextStartupMove;
    }

    public TurnEvents.Actions getNextMove() {
        return nextMove;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Worker getCurrentWorker() {
        return currentWorker;
    }

    public String getLastMovePerformedBy() {
        return lastMovePerformedBy;
    }

    public Board getBoardCopy() {
        return boardCopy;
    }

    public ArrayList<PlayerColor> getAvailableColor() {
        return availableColor;
    }

    public void setAvailableCards(ArrayList<GodsCard> godsCard) {
        this.availableCards.addAll(godsCard);
    }

    public ArrayList<GodsCard> getAvailableCards() {
        return availableCards;
    }
}
