package weatherwear.weatherwear;

import android.app.Fragment;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


import weatherwear.weatherwear.alarm.AlarmAlertManager;
import weatherwear.weatherwear.database.ClothingDatabaseHelper;
import weatherwear.weatherwear.database.ClothingItem;

/**
 * Created by Emma on 2/16/16.
 */
public class NewOutfitActivity extends AppCompatActivity {

    private ArrayList<String> mWeatherArray;
    private ArrayList<ClothingItem> mTops, mBottoms, mShoes, mOuterwear, mScarves, mGloves, mHats;
    private int mTopIndex = -1, mBottomIndex = -1, mShoesIndex = -1, mOuterwearIndex = -1,
            mGlovesIndex = -1, mScarvesIndex = -1, mHatsIndex = -1;
    ProgressDialog progDailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outfit_fragment);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.new_outfit);
        executeTestWeatherCode();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                executeTestWeatherCode();
                return true;
            default:
                return false;
        }
    }

    public void setOutfit(View v) {
        String mKey = getString(R.string.preference_name);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);

        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.clear();

        // store weather
        mEditor.putString("DATE_INDEX", ((TextView)findViewById(R.id.outfit_date)).getText().toString());
        mEditor.putString("LOCATION_INDEX", ((TextView) findViewById(R.id.location)).getText().toString());
        mEditor.putString("HIGH_INDEX", ((TextView)findViewById(R.id.high)).getText().toString());
        mEditor.putString("LOW_INDEX", ((TextView)findViewById(R.id.low)).getText().toString());
        mEditor.putString("CONDITION_INDEX", ((TextView)findViewById(R.id.condition)).getText().toString());

        // store outfit indices
        if (mTopIndex != -1) mEditor.putLong("TOP_INDEX", (mTops.get(mTopIndex)).getId());
        else mEditor.putLong("TOP_INDEX", -1);

        if (mBottomIndex != -1) mEditor.putLong("BOTTOM_INDEX", (mBottoms.get(mBottomIndex)).getId());
        else mEditor.putLong("BOTTOMS_INDEX", -1);

        if (mShoesIndex != -1) mEditor.putLong("SHOES_INDEX", (mShoes.get(mShoesIndex)).getId());
        else mEditor.putLong("SHOES_INDEX", -1);

        if (mOuterwearIndex != -1) mEditor.putLong("OUTERWEAR_INDEX", (mOuterwear.get(mOuterwearIndex)).getId());
        else mEditor.putLong("OUTERWEAR_INDEX", -1);

        if (mGlovesIndex != -1) mEditor.putLong("GLOVES_INDEX", (mGloves.get(mGlovesIndex)).getId());
        else mEditor.putLong("GLOVES_INDEX", -1);

        if (mScarvesIndex != -1) mEditor.putLong("SCARVES_INDEX", (mScarves.get(mScarvesIndex)).getId());
        else mEditor.putLong("SCARVES_INDEX", -1);

        if (mHatsIndex != -1) mEditor.putLong("HATS_INDEX", (mHats.get(mHatsIndex)).getId());
        else mEditor.putLong("HATS_INDEX", -1);

        mEditor.commit();
        Toast.makeText(getApplicationContext(), "Outfit set!", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void cancelOutfit(View v) {
        Toast.makeText(getApplicationContext(), "Outfit cancelled!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setWelcomeMessage(TextView welcomeText) {
        String welcomeMessage = "";
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            welcomeMessage += "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            welcomeMessage += "Good Afternoon";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            welcomeMessage += "Good Evening";
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            welcomeMessage += "Good Night";
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.getString("editTextPref_DisplayName", "-1").equals("-1")) {
            welcomeMessage += " " + sp.getString("editTextPref_DisplayName", "-1");
        }

        welcomeText.setText(welcomeMessage + "!");

    }

    private String getSeason() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);

        if (month >= 10 || month < 4) return "winter";
        if (month >= 8) return "fall";
        if (month >= 6) return "summer";
        return "spring";
    }

    private void getTop() {
        if (mTops == null) return;
        if (mTops.size() == 0) { ((findViewById(R.id.noTop))).setVisibility(View.VISIBLE); return; }
        ((findViewById(R.id.top))).setVisibility(View.VISIBLE);
        mTopIndex = (int) (Math.random() * mTops.size());
        ((ImageView) (findViewById(R.id.top_image))).setImageBitmap(mTops.get(mTopIndex).getImage());
        ((findViewById(R.id.top_group))).setVisibility(View.VISIBLE);
    }

    private void getBottoms() {
        if (mBottoms == null) return;
        if (mBottoms.size() == 0) { ((findViewById(R.id.noBottom))).setVisibility(View.VISIBLE); return; }
        ((findViewById(R.id.bottom))).setVisibility(View.VISIBLE);
        mBottomIndex = (int) (Math.random() * mBottoms.size());
        ((ImageView) (findViewById(R.id.bottom_image))).setImageBitmap(mBottoms.get(mBottomIndex).getImage());
        ((findViewById(R.id.bottom_group))).setVisibility(View.VISIBLE);
    }

    private void getShoes() {
        if (mShoes == null) return;
        if (mShoes.size() == 0) { ((findViewById(R.id.noShoes))).setVisibility(View.VISIBLE); return; }
        ((findViewById(R.id.shoes))).setVisibility(View.VISIBLE);
        mShoesIndex = (int) (Math.random() * mShoes.size());
        ((ImageView) (findViewById(R.id.shoes_image))).setImageBitmap(mShoes.get(mShoesIndex).getImage());
        ((findViewById(R.id.shoes_group))).setVisibility(View.VISIBLE);
    }

    private void getOuterwear() {
        if (mOuterwear == null || mOuterwear.size() == 0) return;
        if (mOuterwear.size() == 0) { ((findViewById(R.id.noOuterwear))).setVisibility(View.VISIBLE); return; }
        ((findViewById(R.id.outerwear))).setVisibility(View.VISIBLE);
        mOuterwearIndex = (int) (Math.random() * mOuterwear.size());
        ((ImageView) (findViewById(R.id.outerwear_image))).setImageBitmap(mOuterwear.get(mOuterwearIndex).getImage());
        ((findViewById(R.id.outerwear_group))).setVisibility(View.VISIBLE);
    }

    private void getScarves() {
        if (mScarves == null) return;
        if (mScarves.size() == 0) { ((findViewById(R.id.noScarves))).setVisibility(View.VISIBLE); return; }
        ((findViewById(R.id.accessories))).setVisibility(View.VISIBLE);
        mScarvesIndex = (int) (Math.random() * mScarves.size());
        ((ImageView) (findViewById(R.id.scarves_image))).setImageBitmap(mScarves.get(mScarvesIndex).getImage());
        ((findViewById(R.id.scarves_group))).setVisibility(View.VISIBLE);
    }

    private void getGloves() {
        if (mGloves == null) return;
        if (mGloves.size() == 0) { ((findViewById(R.id.noGloves))).setVisibility(View.VISIBLE); return; }
        ((findViewById(R.id.accessories))).setVisibility(View.VISIBLE);
        mGlovesIndex = (int) (Math.random() * mScarves.size());
        ((ImageView) (findViewById(R.id.gloves_image))).setImageBitmap(mGloves.get(mGlovesIndex).getImage());
        ((findViewById(R.id.gloves_group))).setVisibility(View.VISIBLE);
    }

    private void getHats() {
        if (mHats == null) return;
        if (mHats.size() == 0) { ((findViewById(R.id.noHats))).setVisibility(View.VISIBLE); return; }
        ((findViewById(R.id.accessories))).setVisibility(View.VISIBLE);
        mHatsIndex = (int) (Math.random() * mHats.size());
        ((ImageView) (findViewById(R.id.hats_image))).setImageBitmap(mHats.get(mHatsIndex).getImage());
        ((findViewById(R.id.hats_group))).setVisibility(View.VISIBLE);
    }

    private void executeTestWeatherCode() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String zipCode = sp.getString("editTextPref_SetLocation", "");
        boolean current = sp.getBoolean("checkboxPref_CurrentLocation", false);
        if (current || zipCode.equals("")) {
            callWithCurrentZipCode();
        } else {
            new WeatherAsyncTask().execute(zipCode);
        }
    }

    private void callWithCurrentZipCode() {
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .build();

        final Geocoder mGeocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                try {
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    List<Address> addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    mGoogleApiClient.disconnect();
                    if (addresses.size() == 0) {
                        // Failed to obtain zip code
                        new AlertDialog.Builder(getApplicationContext()).setMessage("Error obtaining zip code").show();
                    } else {
                        String zipCode = addresses.get(0).getPostalCode();
                        new WeatherAsyncTask().execute(zipCode);
                        // Do something with zip code
                    }
                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                // Whelp
            }
        });

        mGoogleApiClient.connect();
    }

    private class WeatherAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(NewOutfitActivity.this);
            progDailog.setMessage("Loading Your Outfit...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<String> weather) {
            if (weather == null) {
                progDailog.dismiss();

                new AlertDialog.Builder(getApplicationContext()).setMessage("Connect to the Internet to Generate Today's Outfit!").show();
            } else {
                mWeatherArray = weather;
                new ClothingAsyncTask().execute(mWeatherArray);
            }

        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            try {
                String zipcode = params[0];
                String start = "https://query.yahooapis.com/v1/public/yql?q=";
                String query = "SELECT * FROM weather.forecast WHERE woeid IN (SELECT woeid FROM geo.places(1) WHERE text=\"" + zipcode + ", USA\")";

                // Build URL weather request
                URL request = new URL(start + URLEncoder.encode(query, "UTF-8") + "&format=json");
                // Request content and convert to JSONObject
                JSONObject json = new JSONObject(IOUtils.toString(request, Charset.forName("UTF-8")));

                JSONObject data = json.getJSONObject("query").getJSONObject("results").getJSONObject("channel");

                // Extract useful information from raw JSON
                String windChillTemperature = data.getJSONObject("wind").getString("chill");
                String currentTemperature = data.getJSONObject("item").getJSONObject("condition").getString("temp");
                String currentCondition = data.getJSONObject("item").getJSONObject("condition").getString("text");
                String todayLow = ((JSONObject) data.getJSONObject("item").getJSONArray("forecast").get(0)).getString("low");
                String todayHigh = ((JSONObject) data.getJSONObject("item").getJSONArray("forecast").get(0)).getString("high");

                // Can get data for today, tomorrow, day after, next, next.  In total, 5 days including today with indices 1-4 for the four future days. (change in get parameter)
                String tomorrowLow = ((JSONObject) data.getJSONObject("item").getJSONArray("forecast").get(1)).getString("low");
                String tomorrowHigh = ((JSONObject) data.getJSONObject("item").getJSONArray("forecast").get(1)).getString("high");
                String tomorrowCondition = ((JSONObject) data.getJSONObject("item").getJSONArray("forecast").get(1)).getString("text");

                ArrayList<String> weatherData = new ArrayList<String>();

                String location = data.getJSONObject("item").getString("title").split("for ")[1];
                location = location.split(",")[0];
                weatherData.add(location);
                weatherData.add(todayHigh);
                weatherData.add(todayLow);
                weatherData.add(currentCondition);

                Log.d("MYZIP", data.getJSONObject("item").getString("title"));
                Log.d("Current Wind Chill", windChillTemperature);
                Log.d("Current Temperature", currentTemperature);
                Log.d("Current Condition", currentCondition);
                Log.d("Current Low", todayLow);
                Log.d("Current High", todayHigh);


                Log.d("Tomorrow Condition", tomorrowCondition);
                Log.d("Tomorrow Low", tomorrowLow);
                Log.d("Tomorrow High", tomorrowHigh);
                return weatherData;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class ClothingAsyncTask extends AsyncTask<ArrayList<String>, Void, ArrayList<ArrayList<ClothingItem>>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<ClothingItem>> clothes) {
            progDailog.dismiss();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d");
            setWelcomeMessage(((TextView) (findViewById(R.id.welcome))));
            ((TextView) (findViewById(R.id.outfit_date))).setText("Outfit Date: " + sdf.format(new Date()));
            ((TextView) (findViewById(R.id.location))).setText("Location: " + mWeatherArray.get(0));

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (!sp.getString("listPref_Temp","-1").equals("Celsius")) {
                ((TextView) (findViewById(R.id.high))).setText("High: " + mWeatherArray.get(1) + "°F");
                ((TextView) (findViewById(R.id.low))).setText("Low: " + mWeatherArray.get(2) + "°F");
            } else {
                ((TextView) (findViewById(R.id.high))).setText("High: " + String.valueOf(Math.round((((Double.valueOf(mWeatherArray.get(1))-32)*5/9)) * 10) / 10) + "°C");
                ((TextView) (findViewById(R.id.low))).setText("Low: " +  String.valueOf(Math.round((((Double.valueOf(mWeatherArray.get(2))-32)*5/9)) * 10) / 10) + "°C");
            }
            ((TextView) (findViewById(R.id.condition))).setText("Condition: " + mWeatherArray.get(3));
            if (clothes.size() == 0) {
                new AlertDialog.Builder(getApplicationContext()).setMessage("Error Generating Outfit!").show();
            } else {
                mTops = clothes.get(0);
                mBottoms = clothes.get(1);
                mShoes = clothes.get(2);
                mOuterwear = clothes.get(3);
                mScarves = clothes.get(4);
                mGloves = clothes.get(5);
                mHats = clothes.get(6);
                findViewById(R.id.outfit_description).setVisibility(View.VISIBLE);
                getTop();
                getBottoms();
                getShoes();
                getOuterwear();
                getScarves();
                getGloves();
                getHats();
                findViewById(R.id.saveOutfit).setEnabled(true);
            }

        }

        @Override
        protected ArrayList<ArrayList<ClothingItem>> doInBackground(ArrayList<String>... params) {

            ArrayList<ArrayList<ClothingItem>> clothes = new ArrayList<ArrayList<ClothingItem>>();
            ArrayList<String> weather = params[0];
            String season = getSeason();
            ClothingDatabaseHelper dbHelper = new ClothingDatabaseHelper(getApplicationContext());

            int avgTemp = ((Integer.valueOf(weather.get(1)) + Integer.valueOf(weather.get(2))) / 2);

            // top
            if (avgTemp >= 85) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Sleeveless Shirts", season));
            } else if (avgTemp >= 50) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Short Sleeve Shirts", season));
            } else {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Long Sleeve Shirts", season));
            }

            // bottom
            if (avgTemp >= 70) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Shorts", season));
            } else {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Pants", season));
            }

            // shoes
            if (weather.get(3).toLowerCase().contains("snow")) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Snow Boots", season));
            } else if (weather.get(3).toLowerCase().contains("rain") ||
                    weather.get(3).toLowerCase().contains("shower")) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Rain Boots", season));
            } else if (avgTemp <= 50) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Boots", season));
            } else if (avgTemp <= 75) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Sneakers", season));
            } else {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Sandals", season));
            }

            // outerwear
            if (avgTemp <= 50) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Coats", season));
            } else clothes.add(null);

            if (avgTemp <= 31) {
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Scarves", season));
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Gloves", season));
                clothes.add(dbHelper.fetchEntriesByCategoryAndSeason("Hats", season));
            } else {
                clothes.add(null);
                clothes.add(null);
                clothes.add(null);
            }

            return clothes;
        }
    }

    public void topBack(View v) {
        if (mTopIndex == 0) mTopIndex = mTops.size();
        ((ImageView) (findViewById(R.id.top_image))).setImageBitmap(mTops.get(--mTopIndex).getImage());
    }

    public void topForward(View v) {
        if (mTopIndex +  1 >= mTops.size()) mTopIndex = -1;
        ((ImageView) (findViewById(R.id.top_image))).setImageBitmap(mTops.get(++mTopIndex).getImage());
    }

    public void bottomBack(View v) {
        if (mBottomIndex == 0) mBottomIndex = mBottoms.size();
        ((ImageView) (findViewById(R.id.bottom_image))).setImageBitmap(mBottoms.get(--mBottomIndex).getImage());
    }

    public void bottomForward(View v) {
        if (mBottomIndex +  1 >= mBottoms.size()) mBottomIndex = -1;
        ((ImageView) (findViewById(R.id.bottom_image))).setImageBitmap(mBottoms.get(++mBottomIndex).getImage());
    }

    public void shoesBack(View v) {
        if (mShoesIndex == 0) mShoesIndex = mShoes.size();
        ((ImageView) (findViewById(R.id.shoes_image))).setImageBitmap(mShoes.get(--mShoesIndex).getImage());
    }

    public void shoesForward(View v) {
        if (mShoesIndex +  1 >= mShoes.size()) mShoesIndex = -1;
        ((ImageView) (findViewById(R.id.shoes_image))).setImageBitmap(mShoes.get(++mShoesIndex).getImage());
    }

    public void outerwearBack(View v) {
        if (mOuterwearIndex == 0) mOuterwearIndex = mOuterwear.size();
        ((ImageView) (findViewById(R.id.outerwear_image))).setImageBitmap(mOuterwear.get(--mOuterwearIndex).getImage());
    }

    public void outerwearForward(View v) {
        if (mOuterwearIndex +  1 >= mOuterwear.size()) mOuterwearIndex = -1;
        ((ImageView) (findViewById(R.id.outerwear_image))).setImageBitmap(mOuterwear.get(++mOuterwearIndex).getImage());
    }

    public void scarvesBack(View v) {
        if (mScarvesIndex == 0) mScarvesIndex = mScarves.size();
        ((ImageView) (findViewById(R.id.scarves_image))).setImageBitmap(mScarves.get(--mScarvesIndex).getImage());
    }

    public void scarvesForward(View v) {
        if (mScarvesIndex +  1 >= mScarves.size()) mScarvesIndex = -1;
        ((ImageView) (findViewById(R.id.scarves_image))).setImageBitmap(mScarves.get(++mScarvesIndex).getImage());
    }

    public void glovesBack(View v) {
        if (mGlovesIndex == 0) mGlovesIndex = mGloves.size();
        ((ImageView) (findViewById(R.id.gloves_image))).setImageBitmap(mGloves.get(--mGlovesIndex).getImage());
    }

    public void glovesForward(View v) {
        if (mGlovesIndex +  1 >= mGloves.size()) mGlovesIndex = -1;
        ((ImageView) (findViewById(R.id.gloves_image))).setImageBitmap(mGloves.get(++mGlovesIndex).getImage());
    }

    public void hatsBack(View v) {
        if (mHatsIndex == 0) mHatsIndex = mHats.size();
        ((ImageView) (findViewById(R.id.hats_image))).setImageBitmap(mHats.get(--mHatsIndex).getImage());
    }

    public void hatsForward(View v) {
        if (mHatsIndex +  1 >= mHats.size()) mHatsIndex = -1;
        ((ImageView) (findViewById(R.id.hats_image))).setImageBitmap(mHats.get(++mHatsIndex).getImage());
    }

    @Override
    protected void onResume() {
        KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        AlarmAlertManager mAAManager = new AlarmAlertManager();
        if(!myKM.inKeyguardRestrictedInputMode() && mAAManager.isPlaying()) { // if it's not locked, and it's resuming, kill the alarm
            mAAManager.stopAlerts();
        }
        super.onResume();
    }
}

