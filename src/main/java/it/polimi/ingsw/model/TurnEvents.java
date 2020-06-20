package it.polimi.ingsw.model;

import java.io.Serializable;

/**
 * Enums useful to describe the possible actions and win conditions available to players
 */
public class TurnEvents implements Serializable {

    public enum StartupActions {
        COLOR_REQUEST,
        PICK_LAST_COLOR,
        CHOOSE_CARD_REQUEST,
        PICK_UP_CARD_REQUEST,
        PICK_LAST_CARD,
        PLACE_WORKER_1,
        PLACE_WORKER_2;
    }

    /**
     * Set of possible actions that the player can perform (depending on which God owns)
     */
    public enum Actions {
        UNDO(ActionType.COMMAND),
        SKIP(ActionType.COMMAND),
        GAME(ActionType.COMMAND),
        QUIT(ActionType.COMMAND),
        CHOSE_WORKER(ActionType.SETUP),
        WAIT_FOR_UNDO(ActionType.SETUP),
        WIN(ActionType.END),
        LOSE(ActionType.END),
        GAME_END(ActionType.END),

        MOVE_STANDARD(ActionType.MOVEMENT),
        MOVE_NOT_INITIAL_POSITION(ActionType.MOVEMENT),
        MOVE_OPPONENT_SLOT_FLIP(ActionType.MOVEMENT),
        MOVE_OPPONENT_SLOT_PUSH(ActionType.MOVEMENT),
        MOVE_DISABLE_OPPONENT_UP(ActionType.MOVEMENT),

        BUILD_STANDARD(ActionType.BUILDING),
        BUILD_BEFORE(ActionType.BUILDING),
        BUILD_NOT_SAME_PLACE(ActionType.BUILDING),
        BUILD_SAME_PLACE_NOT_DOME(ActionType.BUILDING),
        BUILD_DOME_ANY_LEVEL(ActionType.BUILDING);

        public enum ActionType {
            COMMAND,
            SETUP,
            END,
            MOVEMENT,
            BUILDING;
        }

        private final ActionType type;

        Actions(ActionType type) {
            this.type = type;
        }

        public ActionType getActionType() {
            return this.type;
        }
    }

    /**
     * Set of possible win conditions for the players (depending of which God owns)
     */
    public enum WinConditions {
        WIN_STANDARD,
        WIN_DOUBLE_MOVE_DOWN;
    }
}
