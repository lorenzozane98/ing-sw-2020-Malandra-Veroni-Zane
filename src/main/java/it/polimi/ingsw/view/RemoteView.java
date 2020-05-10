package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Gods;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.PlayerMove;
import it.polimi.ingsw.network.Connection;
import it.polimi.ingsw.observer.Observable;
import it.polimi.ingsw.observer.Observer;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;

public class RemoteView extends Observable<SimpleEntry<Class<?>, String>> implements Observer<SimpleEntry<Class<?>, String>> {

    private final Connection clientConnection;

    public RemoteView(Player player, String opponent, Connection c) {
        this.clientConnection = c;
//        c.addObserver(new MessageReceiver()); //??
//        c.asyncSend("Your opponent is: " + opponent);
    }

    private void handleString(String message) {

    }

    protected void sendMessage(Object message) {
        clientConnection.asyncSend(message);
    }

    public void errorMessage(String message) {
        sendMessage(message);
    }

    @Override
    public void update(SimpleEntry<Class<?>, String> message) {
        notifyAll(message);
    }
}
