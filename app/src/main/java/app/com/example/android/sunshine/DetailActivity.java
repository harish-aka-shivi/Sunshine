package app.com.example.android.sunshine;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.com.example.android.sunshine.data.WeatherContract;

/* Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DetailActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    //@Override
    //@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    /*public Loader<Cursor> onCreate(int id,Bundle args) {
        String locationSetting = Utility.getPreferredLocation(this);
        Uri weather
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
           // WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
        };
        /*WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.LocationEntry.COLUMN_COORD_LAT,
        WeatherContract.LocationEntry.COLUMN_COORD_LONG*/
        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        static final int COL_WEATHER_ID = 0;
        static final int COL_WEATHER_DATE = 1;
        static final int COL_WEATHER_DESC = 2;
        static final int COL_WEATHER_MAX_TEMP = 3;
        static final int COL_WEATHER_MIN_TEMP = 4;
        //static final int COL_LOCATION_SETTING = 5;
        //static final int COL_WEATHER_CONDITION_ID = 6;
        //static final int COL_COORD_LAT = 7;
        //static final int COL_COORD_LONG = 8;
        private String mForecast;
        private String  mForecastStr;
        ForecastAdapter mForecastAdapter;
        TextView mTextView;
        ShareActionProvider mShareActionProvider;
        public PlaceholderFragment() {}

        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            super.onCreateOptionsMenu(menu,inflater);
            inflater.inflate(R.menu.detail, menu);
            MenuItem item = menu.findItem(R.id.menu_share);
            ShareActionProvider mshareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            if (mshareActionProvider != null) {
                mshareActionProvider.setShareIntent(createShareForecastIntent());
            }
            //return true;
        }

        public Intent createShareForecastIntent() {
            Intent intent_share = getActivity().getIntent();
            String new_msg = intent_share.getStringExtra(MainActivity.EXTRA_MESSAGE);
            Intent intent = new Intent(Intent.ACTION_SEND);
            new_msg =  new_msg + "SunshineApp";
            intent.putExtra(Intent.EXTRA_TEXT,new_msg);
            intent.setType("text/plain");
            return intent;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            setHasOptionsMenu(true);
            //String msg = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
            if (intent != null) {
                mForecastStr = intent.getDataString();
            }
            if(null  != mForecastStr) {
                mTextView = ((TextView) rootView.findViewById(R.id.text_detail));
            } return rootView;
        }
        public void onActivityCreated(Bundle  savedInstanceState) {
            getLoaderManager().initLoader(0,null,this);
            super.onActivityCreated(savedInstanceState);
        }
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = Uri.parse(mForecastStr);
            CursorLoader cursorLoader = new CursorLoader(getActivity(),uri,FORECAST_COLUMNS,null,null,null);
            return cursorLoader;
        }
        public void onLoadFinished(Loader<Cursor> loader,Cursor data) {
            if (!data.moveToFirst()) {return;}

            String dateString = Utility.getFriendlyDayString(getActivity(),data.getLong(COL_WEATHER_DATE));

            String weatherDescription = data.getString(COL_WEATHER_DESC);

            boolean isMetric = Utility.isMetric(getActivity());
            String high = Utility.formatTemperature(getActivity(),data.getDouble(COL_WEATHER_MAX_TEMP),isMetric);

            String low = Utility.formatTemperature(getActivity(),data.getDouble(COL_WEATHER_MIN_TEMP),isMetric);
            mForecast = String.format("%s - %s - %s - %s",dateString,weatherDescription,high, low);

            mTextView.setText(mForecast);
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}