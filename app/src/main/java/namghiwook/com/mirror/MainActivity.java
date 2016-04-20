package namghiwook.com.mirror;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;

public class MainActivity extends AppCompatActivity implements IWitListener {

    Wit _wit;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        String accessToken = "GLHGMYIYA674BAIPCDUAHUALMXTQR5DW";
        _wit = new Wit(accessToken, this);
        _wit.enableContextLocation(getApplicationContext());

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        readySensors();

        readyTTS();

//        _wit.captureTextIntent("search youtube for bach");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        relaseSensors();

        releaseTTS();
    }



    TextToSpeech tts;
    boolean ttsEnabled = false;
    protected void readyTTS() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                ttsEnabled = (status == TextToSpeech.SUCCESS);
            }
        });
    }

    protected void releaseTTS() {
        if (tts != null) {
            tts.shutdown();
        }


    }

    protected void readySensors() {
        SensorManager sensorManager
                = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor
                = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(this,
                    "No Light Sensor! quit-",
                    Toast.LENGTH_LONG).show();
        } else {
            float max = lightSensor.getMaximumRange();

            sensorManager.registerListener(lightSensorEventListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        }
    }

    protected void relaseSensors() {
        SensorManager sensorManager
                = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor
                = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            sensorManager.unregisterListener(lightSensorEventListener);
        }
    }


    SensorEventListener lightSensorEventListener
            = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                final float current = event.values[0];
//                trace("light : " + current);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };





    protected void speek(String s) {
        if (tts != null && ttsEnabled) {
            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    protected void doPhotoPrint() {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        photoPrinter.printBitmap("droids.jpg - test print", bitmap);
    }

    protected void searchYoutube(String query) {
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query", query);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void toggle(View v) {
        try {
            _wit.toggleListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void witDidGraspIntent(ArrayList<WitOutcome> witOutcomes, String messageId, Error error) {
        TextView jsonView = (TextView) findViewById(R.id.jsonView);
        jsonView.setMovementMethod(new ScrollingMovementMethod());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (error != null) {
            jsonView.setText(error.getLocalizedMessage());
            return;
        }
        String jsonOutput = gson.toJson(witOutcomes);
        jsonView.setText(jsonOutput);
        ((TextView) findViewById(R.id.txtText)).setText("Done!");

        if (witOutcomes != null && witOutcomes.size() > 0) {
            WitOutcome outcome = witOutcomes.get(0);
            String intent = outcome.get_intent();
            trace("intent ? " + intent);

            String _text = outcome.get_text();
            if (!TextUtils.isEmpty(_text) && ttsEnabled) {
//                speek(_text);
            }

            HashMap<String, JsonElement> entities = outcome.get_entities();
            if ("play_youtube".equals(intent) && entities != null && entities.size() > 0) {

                JsonElement entity = entities.get("youtube_search_query");
                if (entity != null) {
                    try {
                        String youtube_search_query = entity.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                        if (!TextUtils.isEmpty(youtube_search_query)) {
                            trace("youtube_search_query " + youtube_search_query);
                            searchYoutube(youtube_search_query);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    trace("youtube_search_query null");
                }

                entity = entities.get("search_query");
                if (entity != null) {
                    try {
                        String search_query = entity.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                        if (!TextUtils.isEmpty(search_query)) {
                            trace("search_query " + search_query);
                            searchYoutube(search_query);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    trace("search_query null");
                }

            } else if ("print".equals(intent)) {
                doPhotoPrint();
            }
        }

    }

    protected void trace(String s) {
        Log.d("ghi", s);
    }

    @Override
    public void witDidStartListening() {
        ((TextView) findViewById(R.id.txtText)).setText("Witting...");
    }

    @Override
    public void witDidStopListening() {
        ((TextView) findViewById(R.id.txtText)).setText("Processing...");
    }

    @Override
    public void witActivityDetectorStarted() {
        ((TextView) findViewById(R.id.txtText)).setText("Listening");
    }

    @Override
    public String witGenerateMessageId() {
        return null;
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://ai.wit.eval.wit_eval/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://ai.wit.eval.wit_eval/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
