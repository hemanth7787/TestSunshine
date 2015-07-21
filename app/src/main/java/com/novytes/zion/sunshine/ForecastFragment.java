package com.novytes.zion.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import android.widget.Toast;

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


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
     }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_refresh:
                FetchWeatherTask task = new FetchWeatherTask();
                task.execute("calicut,india");
                return true;
//            case R.id.action_settings:
//                startActivity(new Intent(getActivity(), SettingsActivityTwo.class));
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String placeHolder[] = {
                "Today - sunny - 88/63",
                "Tomorrow - Foggy - 70/60",
                "Weds - Cloudy - 70/65",
                "Thurs - Rainy - 50/40",
                "Fri - Foggy - 70/63",
                 "Sat - Sunny - 88/63",
                "Sunday - Sunny - 88/63"};
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(placeHolder));
        mForecastAdapter  = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, weekForecast );
        ListView listView = (ListView) rootView.findViewById(
                R.id.listview_forecast );
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public  void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                String forecast = mForecastAdapter.getItem(position);
                //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });



        return rootView;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private  final  String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] result) {
            if(result!=null){
                mForecastAdapter.clear();
                for(String dayForecastStr: result){
                    mForecastAdapter.add(dayForecastStr);
                }
            }
        }

        @Override
        protected String[] doInBackground(String... params){
            String format = "json";
            String units = "metric";
            int numDays = 7;
            final String FORCAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_param = "cnt";

            Uri builtUri = Uri.parse(FORCAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_param, Integer.toString(numDays))
                    .build();
            //Log.e(LOG_TAG, "URL: " + builtUri.toString());
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String[] forecastArray = {};
            try {

                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                //Log.v(LOG_TAG, "Forecast JSON: "+ forecastJsonStr);

                WhetherDataParser parser = new WhetherDataParser();
                try {
                    forecastArray = parser.getWeatherDataFromJson(forecastJsonStr, numDays);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            return forecastArray;
        }
        public  class WhetherDataParser{
            public double getMaxTempForDay(String weatherJsonString, int dayIndex) throws JSONException{
                JSONObject weather = new JSONObject(weatherJsonString);
                JSONArray days = weather.getJSONArray("list");
                JSONObject dayInfo = days.getJSONObject(dayIndex);
                JSONObject tempInfo = dayInfo.getJSONObject("temp");
                return tempInfo.getDouble("max");
            }

            private String getReadableDateString(long time){
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return  shortenedDateFormat.format(time);
            }

            private String formatHighLows(double high, double low){
                long roundedHigh = Math.round(high);
                long roundedLow = Math.round(low);

                String highLowStr = roundedHigh + "/" + roundedLow;
                return  highLowStr;
            }

            private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {
                final String OWM_LIST = "list";
                final String OWM_Weather = "weather";
                final String OWM_TEMPERATURE = "temp";
                final String OWM_MAX = "max";
                final String OWM_MIN = "min";
                final String OWM_DESCRIPTION = "main";

                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                android.text.format.Time dayTime = new android.text.format.Time();
                dayTime.setToNow();

                int julianDayStart = android.text.format.Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                dayTime = new android.text.format.Time();

                String[] resultStrs = new String[numDays];
                for (int i = 0; i < weatherArray.length(); i++) {
                    String day;
                    String description;
                    String highAndLow;

                    JSONObject dayForcast = weatherArray.getJSONObject(i);
                    long dateTime;
                    dateTime = dayTime.setJulianDay(julianDayStart + i);
                    day = getReadableDateString(dateTime);

                    JSONObject weatherObject = dayForcast.getJSONArray(OWM_Weather).getJSONObject(0);
                    description = weatherObject.getString(OWM_DESCRIPTION);

                    JSONObject tempratureObject = dayForcast.getJSONObject(OWM_TEMPERATURE);
                    double high = tempratureObject.getDouble(OWM_MAX);
                    double low = tempratureObject.getDouble(OWM_MIN);

                    highAndLow = formatHighLows(high, low);
                    resultStrs[i] = day + " - " + description + " - " + highAndLow;
                }

                for (String s : resultStrs) {
                    Log.v(LOG_TAG, "Forecast entry: " + s);
                }

                return resultStrs;
            }



        }
    }


}




