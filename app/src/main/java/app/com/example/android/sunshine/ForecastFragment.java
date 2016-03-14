package app.com.example.android.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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
import app.com.example.android.sunshine.data.WeatherDbHelper;
import app.com.example.android.sunshine.data.WeatherProvider;
//import app.com.example.android.sunshine.service.SunshineService;
import app.com.example.android.sunshine.sync.SunshineSyncAdapter;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int FORECAST_LOADER = 0;
    private int mPosition = ListView.INVALID_POSITION;
    private ListView mListView;
    private static final String SELECTED_KEY = "selected position";
    private ForecastAdapter mForecastAdapter;
    private boolean mFlag = false;
    List<String> arr;
    private static final String LOG_TAG = "sunshine";
    private boolean mUseTodayLayout;
    private static final String[] FORECAST_COLUMNS =
    {   // In this case the id needs to be fully qualified with a table name, since
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

    public void setTodayView(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if(mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mForecastAdapter = new ForecastAdapter(getActivity(),null,0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listview_forcast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mPosition = position;
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri uri= WeatherContract.WeatherEntry.buildWeatherLocationWithDate
                                    (locationSetting, cursor.getLong(COL_WEATHER_DATE));
                    Callback callback = (Callback) getActivity();
                    callback.onItemSelected(uri);
                }
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        //
        return rootView;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mPosition != ListView.INVALID_POSITION) {
            savedInstanceState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(savedInstanceState);
    }
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
        public void displayTodayFragment(Uri dataUri);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            getLoaderManager().initLoader(FORECAST_LOADER, savedInstanceState, this);
        }
        else {
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        }
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
       // FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        //String location = Utility.getPreferredLocation(getActivity());
        //weatherTask.execute(location);*/

        /*Intent intent = new Intent(getActivity(), SunshineService.class);
        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,location);
        getActivity().startService(intent);*/

        /*AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(getActivity(),SunshineService.AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(),0,intent1,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+1000*5,alarmIntent);*/

        //Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        //alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));

        //Wrap in a pending intent which only fires once.
        //PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);

        //AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

        //Set the AlarmManager to wake up the system.
        //am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);

        SunshineSyncAdapter.syncImmediately(getActivity());

    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate
                (locationSetting, System.currentTimeMillis());
        CursorLoader cursorLoader = new CursorLoader(getActivity(),weatherForLocationUri,FORECAST_COLUMNS,null,null,sortOrder);
        return cursorLoader;
    }

    public void displayTask() {
        Cursor cursor = (Cursor) mListView.getItemAtPosition(0);
        long date = cursor.getLong(COL_WEATHER_DATE);
        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting,date);
        ((Callback) getActivity()).displayTodayFragment(uri);
    }
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Log.v("Cursor values",data.toString());
        mForecastAdapter.swapCursor(data);
        //displayTask();
        if(mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
        /*else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayTask();
                }
            });
        }*/
    }
    public void onLoaderReset (Loader<Cursor > loader) {
        mForecastAdapter.swapCursor(null);
    }
}


