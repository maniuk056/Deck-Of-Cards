package pl.mariuszkita.deckofcards;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CardsActivity extends AppCompatActivity {

    public final String DECK_ID = "DECK_ID";
    private final String TAG = "CardsActivity";
    private String id;
    private int id_images;
    private int whichCard = 0;
    private Dialog dialog;
    private Dialog shuffledDialog;
    private int figury = 0;
    private int schodki = 1;
    private int lastCard = 0;

    private int takeCard = 0;
    private int upDown = 0;
    private ArrayList<Integer> schodkiArrayList = new ArrayList<>();
    private ArrayList<Integer> figuryArrayList = new ArrayList<>();
    private boolean nextCardStart = true;
    private Map<String, Integer> mapColor = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);
        Bundle extras = getIntent().getExtras();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new Dialog(this);
        shuffledDialog = new Dialog(this);
        showDialog();

        if (extras != null) {
           id = extras.getString(DECK_ID);
        }

        if (!isOnline()){
            Toast.makeText(this, "Wymagane jest połącznie z internetem", Toast.LENGTH_LONG).show();
        }

    }

    private boolean isOnline(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return  (networkInfo !=null && networkInfo.isConnected());
    }



    private void showDialog(){
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.downloading_data);
        shuffledDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        shuffledDialog.setContentView(R.layout.reshuffle_cards);
    }

    public void nextCard(View view) {

        if(nextCardStart) {
            if (whichCard == 5) {
                Intent intent = getIntent();
                intent.putExtra("DECK_ID", id);
                finish();
                startActivity(intent);
            } else {
                nextCardStart = false;
                CardsActivity.LoadCard task = new CardsActivity.LoadCard();
                task.execute();
            }
        }

    }

    private void showCard(String cardJson) {
        whichCard++;
        try {
            JSONObject data = new JSONObject(cardJson);
            if(data.optInt("remaining") == 0){
                CardsActivity.ReshuffleCards task = new CardsActivity.ReshuffleCards();
                task.execute();
            }
            JSONArray cardsArray = data.getJSONArray("cards");
            JSONObject card = (JSONObject) cardsArray.get(0);

                switch (whichCard%5) {
                    case 1:
                        id_images = R.id.first_card;
                        break;
                    case 2:
                        id_images = R.id.second_card;
                        break;
                    case 3:
                        id_images = R.id.third_card;
                        break;
                    case 4:
                        id_images = R.id.fourth_card;
                        break;
                    case 0:
                        id_images = R.id.fifth_card;
                        ((TextView) findViewById(R.id.text_in_card)).setText("Nowe\nrozdanie");
                        break;
                }

                Picasso.with(this).load(card.optString("image")).into((ImageView) findViewById(id_images));
                sprawdzenieKart(card,id_images);



        } catch (JSONException e) {
            e.printStackTrace();


        }
    }

        private void sprawdzenieKart(JSONObject card, int id_imagesSchodki) {
            int i;


            if (mapColor.get(card.optString("suit")) != null) {
                i = mapColor.get(card.optString("suit"));
                i++;
                mapColor.put(card.optString("suit"), (Integer) i);
                if (i == 3) {
                    Toast.makeText(this,"KOLOR", Toast.LENGTH_LONG).show();
                }
            } else {
                mapColor.put(card.optString("suit"), 1);
            }

            if (mapColor.get(card.optString("value")) != null) {
                i = mapColor.get(card.optString("value"));
                i++;
                mapColor.put(card.optString("value"), (Integer) i);
                if (i == 3) {
                    Toast.makeText(this,"BLIŹNIAKI", Toast.LENGTH_LONG).show();
                }
            } else {
                mapColor.put(card.optString("value"), 1);
            }

            if(card.optString("value").equals("JACK") || card.optString("value").equals("QUEEN") || card.optString("value").equals("KING") ){
                figury++;

                figuryArrayList.add(id_imagesSchodki);

                if(figury == 3){
                    Animation hyperspaceJump = AnimationUtils.loadAnimation(this, R.anim.anim_card);
                    ((ImageView) findViewById(figuryArrayList.get(0))).startAnimation(hyperspaceJump);
                    ((ImageView) findViewById(figuryArrayList.get(1))).startAnimation(hyperspaceJump);
                    ((ImageView) findViewById(figuryArrayList.get(2))).startAnimation(hyperspaceJump);
                    Toast.makeText(this,"FIGURY", Toast.LENGTH_LONG).show();
                }

                switch (card.optString("value")){
                    case "JACK":
                        takeCard = 11;
                        break;
                    case  "QUEEN":
                        takeCard = 12;
                        break;
                    case "KING":
                        takeCard =13;
                        break;
                    default:
                        break;
                }


            }

            if (card.optString("value").equals("ACE")){
                takeCard = 1;
            }else if (takeCard == 0){
                takeCard = Integer.parseInt(card.optString("value"));
            }

            if(lastCard == 0){
            }else if(lastCard == takeCard - 1 && (upDown == 0 || upDown == -1)){
                upDown = -1;
                schodkiArrayList.add(id_imagesSchodki);
                schodki++;
            }else if (lastCard == takeCard + 1 && (upDown == 0 || upDown == 1)){
                upDown = 1;
                schodkiArrayList.add(id_imagesSchodki);
                schodki++;
            }else {
                upDown = 0;
                schodkiArrayList.clear();
                schodki = 1;
            }

            if(schodki == 3){
                Animation hyperspaceJump = AnimationUtils.loadAnimation(this, R.anim.anim_card);
                ((ImageView) findViewById(schodkiArrayList.get(0))).startAnimation(hyperspaceJump);
                ((ImageView) findViewById(schodkiArrayList.get(1))).startAnimation(hyperspaceJump);
                ((ImageView) findViewById(schodkiArrayList.get(2))).startAnimation(hyperspaceJump);
                Toast.makeText(this,"SCHODKI", Toast.LENGTH_LONG).show();
            }

            lastCard = takeCard;
            takeCard = 0;

            nextCardStart = true;

    }


    private class LoadCard extends AsyncTask<Void, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // dialog.show();

        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            if(string != null){
            showCard(string);
            }else {
                nextCardStart =true;
                // showData("Lipa nas odwiedziła");
            }

           // dialog.cancel();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Void... values) {

            HttpURLConnection connection = null;
            try {
                URL deckUrl = new URL(String.format(UrlDockOfCards.URL_DRAW_CARD, id, 1));
                connection = (HttpURLConnection) deckUrl.openConnection();
                connection.connect();
                int status = connection.getResponseCode();

                if(status==200){
                    InputStream is =connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();
                    while((responseString = reader.readLine()) != null){
                        sb =  sb.append(responseString);
                    }
                    String cardData = sb.toString();
                    Log.d(TAG, "CARD: " + cardData);
                    return cardData;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }

            return null;
        }
    }

    private class ReshuffleCards extends AsyncTask<Void, Void, String > {



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            shuffledDialog.show();

        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            shuffledDialog.cancel();
            }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Void... values) {

            HttpURLConnection connection = null;
            try {
                URL deckUrl = new URL(String.format(UrlDockOfCards.URL_DECK_RESHUFFLE, id));
                connection = (HttpURLConnection) deckUrl.openConnection();
                connection.connect();
                int status = connection.getResponseCode();

                if(status==200){
                    InputStream is =connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();
                    while((responseString = reader.readLine()) != null){
                        sb =  sb.append(responseString);
                    }
                    String ReshuffleData = sb.toString();
                    Log.d(TAG, "Reshuffle: " + ReshuffleData);
                    return ReshuffleData;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }

            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}
