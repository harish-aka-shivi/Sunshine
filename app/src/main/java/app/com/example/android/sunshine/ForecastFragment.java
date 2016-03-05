package app.com.example.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import app.com.example.android.sunshine.data.WeatherContract;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS =
    {
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
        WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.LocationEntry.COLUMN_COORD_LAT,
        WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    private ForecastAdapter mForecastAdapter;
    List<String> arr;
    private static final String LOG_TAG = "sunshine";

    public ForecastFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mForecastAdapter = new ForecastAdapter(getActivity(),null,0);
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listview = (ListView) rootview.findViewById(R.id.listview_forcast);
        listview.setAdapter(mForecastAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class).
                            setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate
                                    (locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                    startActivity(intent);
                }
            }
        });
        return rootview;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        int id = item.getItemId();
        if(id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        if(id == R.id.view_map) {
            findLocation();
        }
        return super.onOptionsItemSelected(item);
    }

    public void findLocation() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = pref.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate
                (locationSetting, System.currentTimeMillis());
        CursorLoader cursorLoader = new CursorLoader(getActivity(),weatherForLocationUri,FORECAST_COLUMNS,null,null,sortOrder);
        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("Cursor values",data.toString());
        mForecastAdapter.swapCursor(data);
    }

    public void onLoaderReset (Loader<Cursor > loader) {
        mForecastAdapter.swapCursor(null);
    }
}


