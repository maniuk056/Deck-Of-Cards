package pl.mariuszkita.deckofcards;

/**
 * Created by Mariusz on 2017-08-05.
 */

public interface UrlDockOfCards {

    public static final String URL_DECK_COUNT = "https://deckofcardsapi.com/api/deck/new/shuffle/?deck_count=%d";
    public static final String URL_DRAW_CARD = "https://deckofcardsapi.com/api/deck/%s/draw/?count=%d";
    public static final String URL_DECK_RESHUFFLE = "https://deckofcardsapi.com/api/deck/%s/shuffle/";

}
