package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import android.text.format.Time;

//import android.text.format.Time;

/**
 * Encapsulates fetching the forecast and displaying it as a ListView layout
 */
public class ForecastFragment extends Fragment {

    //instantiate an ArrayAdapter
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    //this add the refresh button
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //allow this fragment to handle menu events
        setHasOptionsMenu(true);
    }


    //inflate the forecastfragment menu in forecastfragment.xml when the options menu is created
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    //handle action bar item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if (id == R.id.action_refresh) {

            //create a FetchWeather task and execute it
            //this runs the code we copied from the website app
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("84123");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //create dummy data for the ListView
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy -21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };

        //put dummy data into a List<String>
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        //load the adapter
        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), //the current context (this activity)
                        R.layout.list_item_forecast, //the name of the layout ID
                        R.id.list_item_forecast_textview, //the ID of the textView to populate
                        weekForecast
                );

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //get a reference to the ListView and attach this adapter to it
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forcast);
        listView.setAdapter(mForecastAdapter);



        //add listener and toast
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            //get and show toast item which should be forecast
            //take a generic view from whatever view that was clicked
            // position is the position of the view that was clicked
            // l is the row id of item
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                //take the mForecaster item at the given position
                String forecast = mForecastAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);

//                //creates the toast
//                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
            }
        });



        return rootView;

    }//End of onCreateView

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            Log.w(LOG_TAG, "doInBackground ");

            //if there is no zip code, there's nothing to look up
            if (params.length == 0){
                return null;
            }

            //***************************text from cloud <code></code>

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            //url variables for data
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Construct variables to build uri
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                //build the URI object
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                //build URL object
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                //System.out.println("*************** URL : " + url);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // input string didn't get any information, so there's nothing to do
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;


                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    System.out.println(line);//print each line to the System monitor

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                //System.out.println("*************** forecastJsonStr:\n " + forecastJsonStr);
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                //System.out.println("There was a problem getting the weather data**********************");
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            //process JSON and return string
            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            }catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
            //return new String[0]; //not sure if this should be "return URL" instead
        }//end of doInBackground

        /**Take the string representing the complete forecast in JSON format and
         * pull out the data we need to construct the Strings needed for the wireframes
         *
         * parsing is easy - the constructor takes the JSON string and converts it into
         * an Object hiearchy for us
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

            //System.out.println("************** getWeatherDataFromJson has been called");

                //These are the names of the JSON objects that need to be extracted
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

//            System.out.println(OWM_LIST + "\n" + OWM_WEATHER + "\n" + OWM_TEMPERATURE + "\n" + OWM_MAX
//                    + "\n" + OWM_MIN + "\n" + OWM_DESCRIPTION);

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


            //OWM returns daily forecasts based upon the local time of the city that is being
            //asked for, which means that we need to know the GMT offset to translate this data
            //properly

            //Since this data is also sent in-order and the first day is always the current day
            //we're going to take advantage of that to get a nice
            //UTC  date for all our weather

            Time dayTime = new Time();
            dayTime.setToNow();
//            System.out.println("Daytime should go here");

            //we start at the day returned by local time, otherwise this won't work well
            int julanStartDay = Time.getJulianDay(System.currentTimeMillis(),dayTime.gmtoff);

            //now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++){
                // for now, using the format "day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                //get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                //the date/time is returned as a long. we need to convert that
                //into something human-readable
                long dateTime;
                //converting to UTC Time
                dateTime = dayTime.setJulianDay(julanStartDay+i);
                day = getReadableDateString(dateTime);

                //description is in a child array called "weather" which is one element long
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                //Temperatures are in a child object called "temp"
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs){
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            System.out.println("****************** resultStrs: " + resultStrs);
            return resultStrs;

        }//end of getWeatherDataFromJson

        private String getReadableDateString(long time){
            //convert Unix timestamp to milliseconds to be converted into a date
            SimpleDateFormat shortenedDataFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDataFormat.format(time);
        }

        //prepare the weather high/lows for presentation
        private String formatHighLows (double high, double low){
            //assume user doesn't care about tenths of a degree
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null){
                mForecastAdapter.clear();
                for (String dayForcastStr : result) {
                    mForecastAdapter.add(dayForcastStr);
                }
                // new data is back from the server
            }
        }



    }//end of FetchWeatherTask
}//end of ForecastFragment
