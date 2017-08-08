package pl.mariuszkita.deckofcards;

import android.app.Dialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String deckId;
    private RelativeLayout relativeLayout;
    private Dialog dialog;
    int i=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isOnline()){

            Toast.makeText(this, "Wymagane jest połącznie z internetem", Toast.LENGTH_LONG).show();
        }else {
            dialog = new Dialog(this);
            showDialog();
        }

    }

    public void showDialog(){
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.downloading_data);
    }



    public void onClickRelative(View v){


        if(isOnline()){

            int ile = 0;
            switch (v.getId()){
                case R.id.one:
                    ile = 1;
                    break;
                case R.id.two:
                    ile = 2;
                    break;
                case R.id.three:
                    ile = 3;
                    break;
                case R.id.four:
                    ile = 4;
                    break;
                case R.id.five:
                    ile = 5;
                    break;
            }

            LoadDeck task = new LoadDeck();
            task.execute(ile);
        } else {
            Toast.makeText(this, "Wymagane jest połącznie z internetem", Toast.LENGTH_LONG).show();
        }


    }



    private void showData(String data){
        try {
            JSONObject deck = new JSONObject(data);
            deckId = deck.optString("deck_id");
            Intent cardsActivity = new Intent(this, CardsActivity.class);
            cardsActivity.putExtra("DECK_ID", deckId);
            startActivity(cardsActivity);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean isOnline(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return  (networkInfo !=null && networkInfo.isConnected());
    }

/*    private void sprawdzenieKart() {
        if (!editText.getText().equals("") && !(editText.getText() == null)) {
            textView.setText(textView.getText() + "\n" + editText.getText());
            if (map.get(editText.getText().toString()) != null) {
                i = map.get(editText.getText().toString());
                i++;
                map.put(editText.getText().toString(), (Integer) i);
                if (i >= 3) {
                    textView.setText(textView.getText() + "\npięknie");
                }
            } else {
                map.put(editText.getText().toString(), 1);
            }
        }
    }*/

    private class LoadDeck extends AsyncTask<Integer, Void, String >{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();

        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            dialog.cancel();

            if(string != null){
                showData(string);
            }else {
               // showData("Lipa nas odwiedziła");
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Integer... integers) {

            HttpURLConnection connection = null;
            try {
                URL deckUrl = new URL(String.format(UrlDockOfCards.URL_DECK_COUNT, integers ));
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
                    String deckData = sb.toString();
                    Log.d(TAG, "DOCK: " + deckData);
                    return deckData;
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


}
