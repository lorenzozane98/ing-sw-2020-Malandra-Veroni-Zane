package it.polimi.ingsw.view;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.Color.PlayerColor;
import it.polimi.ingsw.model.TurnEvents.Actions;
import it.polimi.ingsw.model.TurnEvents.StartupActions;
import it.polimi.ingsw.network.Client.UserInterface;
import it.polimi.ingsw.observer.MessageForwarder;
import it.polimi.ingsw.observer.Observer;
import it.polimi.ingsw.view.cli.Cli;
import it.polimi.ingsw.view.gui.Gui;

import java.util.ArrayList;
import java.util.Objects;

public class View extends MessageForwarder {

    private String playerOwnerNickname;
    private final UserInterface chosenUserInterface;
    private RemoteView remoteView;
    private final Cli playerCli;
    private final Gui playerGui;
    private UpdateTurnMessage currentMove = null;
    private boolean activeReadResponse = false;
    private final UpdateTurnMessageReceiver updateTurnMessageReceiver = new UpdateTurnMessageReceiver();
    private final PlayerMoveSender playerMoveSender = new PlayerMoveSender();
    private final PlayerMoveStartupSender playerMoveStartupSender = new PlayerMoveStartupSender();
    private final StringSender stringSender = new StringSender();
    private final StringReceiver stringReceiver = new StringReceiver();

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

    //TODO: Non l'abbiamo collegato?
    public void setRemoteView(RemoteView remoteView) {
        if (this.remoteView == null)
            this.remoteView = remoteView;
    }

    public void setPlayerOwnerNickname(String playerOwnerNickname) {
        if (this.playerOwnerNickname == null)
            this.playerOwnerNickname = playerOwnerNickname;
    }

    //TODO: Agli errori che passano di qua viene aggiunto un a capo. Verificare
    public void showErrorMessage(String errorToShow) {
        showMessage(errorToShow);
    }

    public void showMessage(String messageToShow) {
        if (chosenUserInterface == UserInterface.CLI && playerCli != null) {
            if(currentMove.isStartupPhase()){
                playerCli.showMessage(messageToShow);
            }
            else if(!(currentMove.getCurrentPlayer().getNickname().equals(playerOwnerNickname))){
                playerCli.showMessage(messageToShow);
            }
            else
                playerCli.showMessage(messageToShow, currentMove.getCurrentPlayer().getPlayerColor());

        } else if (chosenUserInterface == UserInterface.GUI && playerGui != null) {
            //TODO: Gui
        }

        if (messageToShow.contains("Error: "))
            repeatCurrentMove(currentMove);
    }

    public void showSimultaneousMessage(String messageToShow) {
        if (chosenUserInterface == UserInterface.CLI && playerCli != null) {
            playerCli.showSimultaneousMessage(messageToShow);
        } else if (chosenUserInterface == UserInterface.GUI && playerGui != null) {
            //TODO: Gui
        }

        if (messageToShow.contains("Error: "))
            repeatCurrentMove(currentMove);
    }

    private void repeatCurrentMove(UpdateTurnMessage currentMove) {
        handleUpdateTurnFromSocket(currentMove);

//        if (lastMove.getCurrentPlayer().getNickname().equals(playerOwnerNickname))
//            handleMessageForMe(currentMove);
//        else
//            handleMessageForOthers(currentMove);
    }

    public void handleResponse(String response) {
        if (currentMove == null) {
            //TODO: Implementare gestione risposte pre partita (a posto così?)
            stringSender.notifyAll(response);

        } else if (currentMove.getCurrentPlayer().getNickname().equals(playerOwnerNickname)) {
            if (currentMove.isStartupPhase()) {
                PlayerMoveStartup moveStartupToSend = createPlayerMoveStartup();

                if (currentMove.getNextStartupMove() == StartupActions.COLOR_REQUEST ||
                        currentMove.getNextStartupMove() == StartupActions.PICK_LAST_COLOR) {
                    PlayerColor chosenPlayerColor = convertStringToPlayerColor(response);
                    if (chosenPlayerColor == null) {
                        showErrorMessage(ViewMessage.wrongColorChose);
                        return;
                    } else {
                        moveStartupToSend.setChosenColor(chosenPlayerColor);
                    }
                } else if (currentMove.getNextStartupMove() == StartupActions.CHOOSE_CARD_REQUEST ||
                        currentMove.getNextStartupMove() == StartupActions.PICK_UP_CARD_REQUEST ||
                        currentMove.getNextStartupMove() == StartupActions.PICK_LAST_CARD) {
                    moveStartupToSend.setChosenCard(response);
                } else if (currentMove.getNextStartupMove() == StartupActions.PLACE_WORKER_1 ||
                        currentMove.getNextStartupMove() == StartupActions.PLACE_WORKER_2) {
                    if (convertStringToPosition(response) != null)
                        moveStartupToSend.setWorkerPosition(convertStringToPosition(response));
                    else {
                        showErrorMessage(ViewMessage.wrongInputCoordinates);
                        return;
                    }
                }

                sendPlayerMoveStartup(moveStartupToSend);
            } else {
                if (response.equalsIgnoreCase("undo")) {
                    if (currentMove.getNextMove() != Actions.CHOSE_WORKER) {
                        PlayerMove playerMoveToSend = createPlayerMoveUndo();
                        sendPlayerMove(playerMoveToSend);
                    } else {
                        showErrorMessage(ViewMessage.cannotUndo);
                        return;
                    }
                } else if (response.equalsIgnoreCase("skip")) {
                    if (currentMove.getNextMove() == Actions.BUILD_BEFORE) {
                        PlayerMove playerMoveToSend = createPlayerMoveSkip();
                        sendPlayerMove(playerMoveToSend);
                    } else {
                        showErrorMessage(ViewMessage.cannotSkipThisMove);
                        return;
                    }
                } else {
                    Worker workerInSlot;

                    //TODO: Si può fare diversamente?
                    if (convertStringToPosition(response) == null) {
                        showErrorMessage(ViewMessage.wrongInputCoordinates);
                    } else {
                        if (currentMove.getNextMove() == Actions.CHOSE_WORKER) {
                            workerInSlot = currentMove.getBoardCopy().getSlot(Objects.requireNonNull(convertStringToPosition(response))).getWorkerInSlot();
                            if (workerInSlot != null) {
                                if (currentMove.getCurrentPlayer().getWorkers().stream().map(Worker::getIdWorker).anyMatch(workerInSlot.getIdWorker()::equalsIgnoreCase)) {
                                    Worker finalWorkerInSlot = workerInSlot;
                                    workerInSlot = currentMove.getCurrentPlayer().getWorkers().stream().filter(worker -> worker.getIdWorker().equals(finalWorkerInSlot.getIdWorker())).findFirst().orElse(null);
                                    assert workerInSlot != null;
                                    PlayerMove playerMoveToSend = createPlayerMove(convertStringToPosition(response), workerInSlot.getIdWorker());
                                    sendPlayerMove(playerMoveToSend);
                                } else {
                                    showErrorMessage(ViewMessage.choseNotYourWorker);
                                    return;
                                }
                            } else {
                                showErrorMessage(ViewMessage.noWorkerInSlot);
                                return;
                            }
                        } else {
                            PlayerMove playerMoveToSend = createPlayerMove(convertStringToPosition(response));
                            sendPlayerMove(playerMoveToSend);
                        }
                    }
                }
            }
        } else {
            showMessage(ViewMessage.wrongTurnMessage);
            return;
        }
    }

    private void sendPlayerMove(PlayerMove playerMove) {
        playerMoveSender.notifyAll(playerMove);
    }

    private void sendPlayerMoveStartup(PlayerMoveStartup playerMoveStartup) {
        playerMoveStartupSender.notifyAll(playerMoveStartup);
    }

    private Position convertStringToPosition(String coordinates) {
        if (coordinates.length() != 2) {
            showErrorMessage(ViewMessage.wrongInputCoordinates);
            return null;
        }

        int coordinateX = coordinates.charAt(1);
        int coordinateY = coordinates.charAt(0);
        if (coordinateX >= 49 && coordinateX <= 53)
            if (coordinateY >= 65 && coordinateY <= 69)
                return new Position(coordinateX - 49, coordinateY - 65);
            else if (coordinateY >= 97 && coordinateY <= 101)
                return new Position(coordinateX - 49, coordinateY - 97);

        return null;
    }

    private PlayerColor convertStringToPlayerColor(String playerColor) {
        switch (playerColor.toLowerCase()) {
            case "red":
                if (currentMove.getAvailableColor().contains(PlayerColor.RED))
                    return PlayerColor.RED;
                break;
            case "cyan":
                if (currentMove.getAvailableColor().contains(PlayerColor.CYAN))
                    return PlayerColor.CYAN;
                break;
            case "yellow":
                if (currentMove.getAvailableColor().contains(PlayerColor.YELLOW))
                    return PlayerColor.YELLOW;
                break;
        }

        return null;
    }

    private String getAvailableColorBuilder(ArrayList<PlayerColor> availableColor) {
        StringBuilder stringBuilder = new StringBuilder();
        for (PlayerColor playerColor : availableColor) {
            stringBuilder.append(" ").append(playerColor.getEscape()).append(playerColor.getColorAsString()).append(Color.RESET).append(" or");
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "");
        return String.valueOf(stringBuilder);
    }

    private String getAvailableCardsBuilder(ArrayList<GodsCard> availableCards) {
        StringBuilder stringBuilder = new StringBuilder();
        for (GodsCard godsCard : availableCards) {
            stringBuilder.append("\n").append(godsCard.getCardName().toUpperCase()).append(": ").append(godsCard.getCardDescription());
        }

        return String.valueOf(stringBuilder);
    }

    //TODO: ??
    protected boolean colorChecker(PlayerColor playerColor) {
        return playerColor != null;
    }

    protected PlayerMove createPlayerMove(Position targetSlotPosition) {
        return new PlayerMove(
                currentMove.getCurrentWorker().getIdWorker(),
                currentMove.getNextMove(),
                targetSlotPosition,
                currentMove.getCurrentPlayer().getNickname());
    }

    protected PlayerMove createPlayerMove(Position targetSlotPosition, String currentWorker) {
        return new PlayerMove(
                currentWorker,
                currentMove.getNextMove(),
                targetSlotPosition,
                currentMove.getCurrentPlayer().getNickname());
    }

    protected PlayerMove createPlayerMoveUndo() {
        return new PlayerMove(
                currentMove.getCurrentWorker().getIdWorker(),
                Actions.UNDO,
                currentMove.getCurrentWorker().getWorkerPosition(),
                currentMove.getCurrentPlayer().getNickname()
                );
    }

    protected PlayerMove createPlayerMoveSkip() {
        return new PlayerMove(
                currentMove.getCurrentWorker().getIdWorker(),
                Actions.SKIP,
                currentMove.getCurrentWorker().getWorkerPosition(),
                currentMove.getCurrentPlayer().getNickname()
        );
    }

    protected PlayerMoveStartup createPlayerMoveStartup() {
        return new PlayerMoveStartup(currentMove.getNextStartupMove());

        //TODO: Logica set proprietà
    }

    private void handleMessageForMe(UpdateTurnMessage message) {
        if (message.isStartupPhase()) {
            if (message.getNextStartupMove() == StartupActions.COLOR_REQUEST)
                showMessage(ViewMessage.colorRequest + getAvailableColorBuilder(message.getAvailableColor()));
            else if (message.getNextStartupMove() == StartupActions.PICK_LAST_COLOR) {
                showMessage(ViewMessage.pickLastColor + message.getAvailableColor().get(0).getColorAsString());
                new Thread(() -> handleResponse(message.getAvailableColor().get(0).getColorAsString())).start();
            } else if (message.getNextStartupMove() == StartupActions.CHOOSE_CARD_REQUEST)
                showMessage(ViewMessage.chooseCardRequest + getAvailableCardsBuilder(message.getAvailableCards()));
            else if (message.getNextStartupMove() == StartupActions.PICK_UP_CARD_REQUEST)
                showMessage(ViewMessage.pickUpCardRequest + getAvailableCardsBuilder(message.getAvailableCards()));
            else if (message.getNextStartupMove() == StartupActions.PICK_LAST_CARD) {
                showMessage(ViewMessage.pickLastCard + message.getAvailableCards().get(0).getCardName().toUpperCase());
                new Thread(() -> handleResponse(message.getAvailableCards().get(0).getCardName())).start();
            } else if (message.getNextStartupMove() == StartupActions.PLACE_WORKER_1 ||
                    message.getNextStartupMove() == StartupActions.PLACE_WORKER_2) {
                refreshView(message.getBoardCopy(), chosenUserInterface);
                showMessage(ViewMessage.placeWorker);
            }
        } else {
            refreshView(message.getBoardCopy(), chosenUserInterface);

            if (message.getNextMove() == Actions.CHOSE_WORKER)
                showMessage(ViewMessage.choseYourWorker);
            else if (message.getNextMove() == Actions.MOVE_STANDARD)
                showMessage(ViewMessage.moveStandard);
            else if (message.getNextMove() == Actions.MOVE_NOT_INITIAL_POSITION)
                showMessage(ViewMessage.moveNotInitialPosition);
            else if (message.getNextMove() == Actions.MOVE_OPPONENT_SLOT_FLIP)
                showMessage(ViewMessage.moveOpponentSlotFlip);
            else if (message.getNextMove() == Actions.MOVE_OPPONENT_SLOT_PUSH)
                showMessage(ViewMessage.moveOpponentSlotPush);
            else if (message.getNextMove() == Actions.MOVE_DISABLE_OPPONENT_UP) //TODO: se ho atena questo messaggio non devve essere stampatp a me
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
        if (message.isStartupPhase()) {
            if (message.getNextStartupMove() == StartupActions.COLOR_REQUEST)
                showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.colorRequestOthers + getAvailableColorBuilder(message.getAvailableColor()));
//            else if (message.getNextStartupMove() == StartupActions.PICK_LAST_COLOR)
//                showMessage(ViewMessage.pickLastCard);
            else if (message.getNextStartupMove() == StartupActions.CHOOSE_CARD_REQUEST)
                showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.chooseCardRequestOthers);
            else if (message.getNextStartupMove() == StartupActions.PICK_UP_CARD_REQUEST)
                showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.pickUpCardRequestOthers);
//            else if (message.getNextStartupMove() == StartupActions.PICK_LAST_CARD)
//                showMessage(ViewMessage.pickLastCard);
            else if (message.getNextStartupMove() == StartupActions.PLACE_WORKER_1 ||
                    message.getNextStartupMove() == StartupActions.PLACE_WORKER_2) {
                refreshView(message.getBoardCopy(), chosenUserInterface);
                showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.placeWorkerOthers);
            }
        } else {
            refreshView(message.getBoardCopy(), chosenUserInterface);
            if (message.getNextMove() == Actions.CHOSE_WORKER)
                showMessage(message.getCurrentPlayer().getNickname() + ViewMessage.choseYourWorkerOthers);
            else if (message.getNextMove() == Actions.MOVE_STANDARD)
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
    }

    public void refreshView(Board newBoard, UserInterface userInterface) {
        if (userInterface == UserInterface.CLI && playerCli != null){
            clearConsole(); //funziona solo fuori da IntelliJ
            playerCli.refreshBoard(newBoard);
        }
        else if (userInterface == UserInterface.GUI && playerGui != null) {
            //TODO: Gui
            //playerGui.refreshBoard()
        }
    }

    public void addPlayerMoveObserver(Observer<PlayerMove> observer) {
        playerMoveSender.addObserver(observer);
    }

    public void addPlayerMoveStartupObserver(Observer<PlayerMoveStartup> observer) {
        playerMoveStartupSender.addObserver(observer);
    }

    public void addStringObserver(Observer<String> observer) {
        stringSender.addObserver(observer);
    }


    @Override
    protected void handleUpdateTurnFromSocket(UpdateTurnMessage message) {
        if (!activeReadResponse)
            activateReadResponse();

        this.currentMove = message;

        if (message.getCurrentPlayer().getNickname().equals(playerOwnerNickname))
            handleMessageForMe(message);
        else
            handleMessageForOthers(message);
    }

    private void activateReadResponse() {
        if (chosenUserInterface == UserInterface.CLI && playerCli != null)
            playerCli.activateAsyncReadResponse();
        else if (chosenUserInterface == UserInterface.GUI && playerGui != null) {
            //TODO: Gui
        }

        activeReadResponse = true;
    }

    @Override
    public void handleString(String messageString) {
        showSimultaneousMessage(messageString);
    }

    public UpdateTurnMessageReceiver getUpdateTurnMessageReceiver() {
        return updateTurnMessageReceiver;
    }

    public StringReceiver getStringReceiver() {
        return stringReceiver;
    }


    public static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            }
            else {
                Runtime.getRuntime().exec("clear");
            }
        }
        catch (final Exception ignored) {
        }
    }
}

