package it.polimi.ingsw.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class BoardTest {

    @Test
    public void printGameBoard() {
        Board board = new Board();
        Position posizione1 = new Position(4,3);
        Slot slot1 = new Slot(posizione1);
        Building b1 = new Building(Building.BuildingLevel.LEVEL1);
        Building b2= new Building(Building.BuildingLevel.LEVEL2);
        Building b3= new Building(Building.BuildingLevel.LEVEL3);
        Building b4= new Building(Building.BuildingLevel.DOME);

        slot1.setBuilding(b1);
        slot1.setBuilding(b2);
        slot1.setBuilding(b3);
        slot1.setBuilding(b4);

        Position posizione2 = new Position(0,1);
        Slot slot2 = new Slot(posizione2);
        Building bb1 = new Building(Building.BuildingLevel.LEVEL1);
        Building bb2= new Building(Building.BuildingLevel.LEVEL2);
        Building bb4= new Building(Building.BuildingLevel.DOME);

        slot2.setBuilding(bb1);
        slot2.setBuilding(bb2);
        slot2.setBuilding(bb4);

        board.printGameBoard();
        board.updateBuildingOnBoard(slot1);
        board.updateBuildingOnBoard(slot2);
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        board.printGameBoard();

    }
}