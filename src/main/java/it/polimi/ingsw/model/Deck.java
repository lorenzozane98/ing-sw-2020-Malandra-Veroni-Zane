package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class Deck {

    private ArrayList<GodsCard> cardList = new ArrayList<>();
    private ArrayList<GodsCard> chosenCards = new ArrayList<>();

    public Deck() {
        buildDeck();
    }

    private void buildDeck() {
        try {
            //Apertura file xml GodsDescription.xml, ed inizializzazione documento
            File xmlGodsDescription = new File("src/GodsDescription.xml");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlGodsDescription);
            //Normalizza gli elementi del file nel caso ci fossero "problemi" di formattazione
            //document.getDocumentElement().normalize();
            document.normalizeDocument();

            //Salva il nodo root e cerca tutti i nodi figli (le divinità)
            Element rootElement = document.getDocumentElement();
            NodeList godsNameNodes = rootElement.getChildNodes();

            //Crea un nuovo nodo singolo per ogni divinità e istanzia una carta aggiungendola a cardList
            for (int i = 0; i < godsNameNodes.getLength(); i++) {
                Node godNode = godsNameNodes.item(i);
                if (godNode.getNodeType() == Node.ELEMENT_NODE) {
                    GodsCard card = new GodsCard(godNode.getNodeName());

                    Element godElement = (Element) godNode;
                    Node descriptionNode = godElement.getElementsByTagName("description").item(0);
                    String godDescription = descriptionNode.getFirstChild().getTextContent();
                    card.setCardDescription(godDescription);
                    cardList.add(card);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void chooseCards(String... godsCardName) throws IllegalArgumentException {
        try {
            //Se il numero di parametri passati alla funzione non è 2 o 3, viene lanciata un'eccezione IllegalArgumentException
            if (godsCardName.length < 2 || godsCardName.length > 3)
                throw new IllegalArgumentException();

            for(String cardName : godsCardName)
                addCardToChosen(cardName);

        } catch (IllegalArgumentException ex){ //TODO: Check
            throw ex;
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    //TODO: Gestire passaggio della stessa carta due volte
    private void addCardToChosen(String godCardName) throws IllegalArgumentException {
        try {
            if (cardList.size() != 0) {
                if (cardList.stream().anyMatch(x -> x.getCardName().equals(godCardName))) {
                    chosenCards.add(cardList.stream().filter(x -> x.getCardName().equals(godCardName)).findFirst().orElse(null));
                } else {
                    throw new IllegalArgumentException();
                }
            }
        } catch (IllegalArgumentException ex){
            throw ex;
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public GodsCard pickUpCard(String godCardName) throws IllegalArgumentException {
        try {
            if (chosenCards.size() != 0) {
                if (chosenCards.stream().anyMatch(x -> x.getCardName().equals(godCardName))) {
                    GodsCard detectedCard = chosenCards.stream().filter(x -> x.getCardName().equals(godCardName)).findFirst().orElse(null);
                    chosenCards.remove(detectedCard);
                    return detectedCard;
                } else {
                    throw new IllegalAccessException();
                }
            }
            return null;
        } catch (IllegalArgumentException ex){
            throw ex;
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }

    public ArrayList<GodsCard> getCardList() {
        return cardList;
    }

    public ArrayList<GodsCard> getChosenCards() {
        return chosenCards;
    }
}
