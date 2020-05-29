package it.polimi.ingsw.view;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.TurnEvents.Actions;
import it.polimi.ingsw.model.TurnEvents.StartupActions;
import it.polimi.ingsw.network.Client.UserInterface;
import it.polimi.ingsw.network.Message;
import it.polimi.ingsw.observer.MessageForwarder;
import it.polimi.ingsw.observer.Observer;
import it.polimi.ingsw.view.cli.Cli;
import it.polimi.ingsw.view.gui.Gui;
import java.util.ArrayList;

public class View extends MessageForwarder {

    private String playerOwnerNickname;
    private final UserInterface chosenUserInterface;
    private RemoteView remoteView;
    private final Cli playerCli;
    private final Gui playerGui;
    private UpdateTurnMessage currentMove = null;
    private final UpdateTurnMessageReceiver updateTurnMessageReceiver = new UpdateTurnMessageReceiver();
    private final PlayerMoveSender playerMoveSender = new PlayerMoveSender();
    private final PlayerMoveStartupSender playerMoveStartupSender = new PlayerMoveStartupSender();
    //private final StringSender stringSender = new StringSender();

    //TODO: Mich completa tu i costruttori in base a quello che ti serve
    //Ne ho fatti due in modo da averne uno in caso di creazione CLI e uno GUI (se non serve risistema tu)
    public View(Cli playerCli) {
        this.chosenUserInterface = UserInterface.CLI;
        this.playerCli = playerCli;
        this.playerCli.setViewOwner(this);
        this.playerGui = null;
    }

    public View(Gui playerGui) {
        this.chosenUserInterface = UserInterface.GUI;
        this.playerGui = playerGui;
        this.playerCli = null;
    }

    public void setRemoteView(RemoteView remoteView) {
        if (this.remoteView == null)
            this.remoteView = remoteView;
    }

    public void setPlayerOwnerNickname(String playerOwnerNickname) {
        if (this.playerOwnerNickname == null)
            this.playerOwnerNickname = playerOwnerNickname;
    }

    public void showErrorMessage(String errorToShow) {
        showMessage(errorToShow);
    }

    public void showMessage(String messageToShow) {
        if (chosenUserInterface == UserInterface.CLI && playerCli != null) {
            playerCli.showMessage(messageToShow);
        } else if (chosenUserInterface == UserInterface.GUI && playerGui != null) {
            //TODO: Gui
        }

        if (messageToShow.contains("Error: "))
            repeatCurrentMove(currentMove);
    }

    private void repeatCurrentMove(UpdateTurnMessage currentMove) {
        handleUpdateTurn(currentMove);

//        if (lastMove.getCurrentPlayer().getNickname().equals(playerOwnerNickname))
//            handleMessageForMe(currentMove);
//        else
//            handleMessageForOthers(currentMove);
    }

    public void readResponse(String response) {
        if (currentMove.getCurrentPlayer().getNickname().equals(playerOwnerNickname)) {
            if (currentMove.isStartupPhase()) {
//                if (currentMove.getNextStartupMove() == StartupActions.COLOR_REQUEST){
//
//                else if (currentMove.getNextStartupMove() == StartupActions.PICK_LAST_COLOR)
//
//                else if (currentMove.getNextStartupMove() == StartupActions.CHOOSE_CARD_REQUEST)
//
//                else if (currentMove.getNextStartupMove() == StartupActions.PICK_UP_CARD_REQUEST)
//
//                else if (currentMove.getNextStartupMove() == StartupActions.PICK_LAST_CARD)
//
//                else if (currentMove.getNextStartupMove() == StartupActions.PLACE_WORKER)

            } else {
                convertStringPositionToSlot(response);
            }
        } else
            showMessage(Message.wrongTurnMessage);
    }

    private Position convertStringPositionToSlot(String coordinates) {
//        int x = -1, y = -1;

        if (coordinates.length() > 2) {
            showErrorMessage(ViewMessage.wrongInputCoordinates);
            return null;
        }

        int coordinateX = coordinates.charAt(0);
        int coordinateY = coordinates.charAt(1);
        if (coordinateY >= 1 && coordinateY <= 5)
            if (coordinateX >= 65 && coordinateX <= 69)
                return new Position(coordinateX - 65, coordinateY - 1);
            else if (coordinateX >= 97 && coordinateX <= 101)
                return new Position(coordinateX - 97, coordinateY - 1);

        showErrorMessage(ViewMessage.wrongInputCoordinates);
        return null;

//        for (int i = 0; i < 5; i++) {
//            if ((int) coordinates.charAt(0) == (i+65)) {
//                x=i;
//            }
//            if ((int) coordinates.charAt(1) == (i+1)) {
//                y=i;
//            }
//        }
//        return new Slot(new Position(x, y));
    }

    protected void createPlayerMove(Slot targetSlot, Turn turn) {
        playerMoveSender.notifyAll(
                new PlayerMove(
                        currentMove.getCurrentWorker(),
                        currentMove.getNextMove(),
                        targetSlot,
                        turn));
    }

    protected void createPlayerMoveStartup(Turn turn) {
        playerMoveStartupSender.notifyAll(
                new PlayerMoveStartup(
                        currentMove.getCurrentPlayer(),
                        currentMove.getNextStartupMove()/*,
                        turn)*/));

        //TODO: Logica set proprietà
    }

    private void handleMessageForMe(UpdateTurnMessage message) {
        if (message.isStartupPhase()) {
            if (message.getNextStartupMove() == StartupActions.COLOR_REQUEST)
                showMessage(ViewMessage.colorRequest + getAvailableColorBuilder(message.getAvailableColor()));
            else if (message.getNextStartupMove() == StartupActions.PICK_LAST_COLOR)
                showMessage(ViewMessage.pickLastColor);
            else if (message.getNextStartupMove() == StartupActions.CHOOSE_CARD_REQUEST)
                showMessage(ViewMessage.chooseCardRequest);
            else if (message.getNextStartupMove() == StartupActions.PICK_UP_CARD_REQUEST)
                showMessage(ViewMessage.pickUpCardRequest);
            else if (message.getNextStartupMove() == StartupActions.PICK_LAST_CARD)
                showMessage(ViewMessage.pickLastCard);
            else if (message.getNextStartupMove() == StartupActions.PLACE_WORKER)
                showMessage(ViewMessage.placeWorker);
        } else {
            if (!message.getLastMovePerformedBy().equals(playerOwnerNickname))
                refreshView(message.getBoardCopy(), chosenUserInterface);
            if (message.getNextMove() == Actions.MOVE_STANDARD)
                showMessage(ViewMessage.moveStandard);
            else if (message.getNextMove() == Actions.MOVE_NOT_INITIAL_POSITION)
                showMessage(ViewMessage.moveNotInitialPosition);
            else if (message.getNextMove() == Actions.MOVE_OPPONENT_SLOT_FLIP)
                showMessage(ViewMessage.moveOpponentSlotFlip);
            else if (message.getNextMove() == Actions.MOVE_OPPONENT_SLOT_PUSH)
                showMessage(ViewMessage.moveOpponentSlotPush);
            else if (message.getNextMove() == Actions.MOVE_DISABLE_OPPONENT_UP)
                showMessage(ViewMessage.moveDisableOpponentUp);
            else if (message.getNextMove() == Actions.BUILD_STANDARD)
                showMessage(ViewMessage.buildStandard);
            else if (message.getNextMove() == Actions.BUILD_BEFORE)
                showMessage(ViewMessage.buildBefore);
            else if (message.getNextMove() == Actions.BUILD_NOT_SAME_PLACE)
                showMessage(ViewMessage.buildNotSamePlace);
            else if (message.getNextMove() == Actions.BUILD_SAME_PLACE_NOT_DOME)
                showMessage(ViewMessage.buildSamePlaceNotDome);
            else if (message.getNextMove() == Actions.BUILD_DOME_ANY_LEVEL)
                showMessage(ViewMessage.buildDomeAnyLevel);
        }
    }

    //TODO: Nel caso la lastMovePerformedBy non sia stata fatta dal playerOwner della view deve anche essere refreshata la board
    //Bisogna anche gestire tutti gli aggiornamenti grafici oltre ai messaggi che possono funzionare da log
    private void handleMessageForOthers(UpdateTurnMessage message) {
        if (!message.getLastMovePerformedBy().equals(playerOwnerNickname))
            refreshView(message.getBoardCopy(), chosenUserInterface);
        if (message.getNextMove() == Actions.MOVE_STANDARD)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.moveStandardOthers);
        else if (message.getNextMove() == Actions.MOVE_NOT_INITIAL_POSITION)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.moveNotInitialPositionOthers);
        else if (message.getNextMove() == Actions.MOVE_OPPONENT_SLOT_FLIP)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.moveOpponentSlotFlipOthers);
        else if (message.getNextMove() == Actions.MOVE_OPPONENT_SLOT_PUSH)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.moveOpponentSlotPushOthers);
        else if (message.getNextMove() == Actions.MOVE_DISABLE_OPPONENT_UP)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.moveDisableOpponentUpOthers);
        else if (message.getNextMove() == Actions.BUILD_STANDARD)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.buildStandardOthers);
        else if (message.getNextMove() == Actions.BUILD_BEFORE)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.buildBeforeOthers);
        else if (message.getNextMove() == Actions.BUILD_NOT_SAME_PLACE)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.buildNotSamePlaceOthers);
        else if (message.getNextMove() == Actions.BUILD_SAME_PLACE_NOT_DOME)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.buildSamePlaceNotDomeOthers);
        else if (message.getNextMove() == Actions.BUILD_DOME_ANY_LEVEL)
            showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.buildDomeAnyLevelOthers);
    }

    @Override
    protected void handleUpdateTurn(UpdateTurnMessage message) {
        this.currentMove = message;

        if (message.getCurrentPlayer().getNickname().equals(playerOwnerNickname))
            handleMessageForMe(message);
        else
            handleMessageForOthers(message);
    }

    public UpdateTurnMessageReceiver getUpdateTurnMessageReceiver() {
        return updateTurnMessageReceiver;
    }

    public void addPlayerMoveObserver(Observer<PlayerMove> observer) {
        playerMoveSender.addObserver(observer);
    }

    public void addPlayerMoveStartupObserver(Observer<PlayerMoveStartup> observer) {
        playerMoveStartupSender.addObserver(observer);
    }


    public void refreshView(Board newBoard, UserInterface userInterface) {
        if (userInterface == UserInterface.CLI && playerCli != null)
            playerCli.refreshBoard(newBoard);
        else if (userInterface == UserInterface.GUI && playerGui != null) {
            //TODO: Gui
            //playerGui.refreshBoard()
        }
    }

    /*
    public void addStringObserver(Observer<String> observer){
        stringSender.addObserver(observer);
    }
    public void testPlayerMoveStartup(Color.PlayerColor playerColor){
        PlayerMoveStartup pms = new PlayerMoveStartup(playerOwnerNickname, currentMove.getNextStartupMove());
        pms.setChosenColor(playerColor);
        playerMoveStartupSender.notifyAll();
    }

*/


    private String getAvailableColorBuilder(ArrayList<Color.PlayerColor> availableColor){
        StringBuilder stringBuilder = new StringBuilder();
        for (Color.PlayerColor playerColor : availableColor) {
            stringBuilder.append(" ").append(playerColor.getEscape()).append(playerColor.getColorAsString(playerColor)).append(Color.RESET).append(" or");
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "");
        return String.valueOf(stringBuilder);
    }

    protected Color.PlayerColor getColorFromString(String string){
        switch (string){
            case "red":
                return Color.PlayerColor.RED;
            case "yellow":
                return Color.PlayerColor.YELLOW;
            case "cyan":
                return Color.PlayerColor.CYAN;
            default:
                return null;
        }
    }

    protected boolean colorChecker(Color.PlayerColor playerColor){
        return playerColor != null;
    }
}

