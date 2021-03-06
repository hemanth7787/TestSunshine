package com.novytes.zion.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(LOG_TAG, " OnCreate Triggered");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(LOG_TAG, " OnPause Triggered");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(LOG_TAG, " OnStop Triggered");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(LOG_TAG, " OnResume Triggered");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(LOG_TAG, " OnStart Triggered");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(LOG_TAG, " OnDestroy Triggered");
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
            startActivity(new Intent(this, SettingsActivityTwo.class));
            return true;
        }else if(id == R.id.action_map){
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void openPreferredLocationInMap(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location =sharedPrefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }else {
            Log.d(LOG_TAG, "Couldn't call "+ location +", no");
        }

    }
}
