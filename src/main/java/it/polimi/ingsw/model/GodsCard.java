package it.polimi.ingsw.model;

public class GodsCard implements Cloneable {

    private final String cardName;
    private String cardDescription;

    public GodsCard(String cardName){
        this.cardName = cardName;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardDescription() {
        return cardDescription;
    }

    public void setCardDescription(String cardDescription) {
        this.cardDescription = cardDescription;
    }

    @Override
    protected final GodsCard clone(){
        GodsCard clonedCard = new GodsCard(this.cardName);
        clonedCard.setCardDescription(this.cardDescription);

        return clonedCard;
    }
}
