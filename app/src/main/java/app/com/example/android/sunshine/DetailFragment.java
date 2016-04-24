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
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.com.example.android.sunshine.data.WeatherContract;

/**
 * Created by root on 5/3/16.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI= "URI";
    private static final int DETAIL_LOADER = 0;
    private String mForecast;
    private String mForecastStr;
    //ForecastAdapter mForecastAdapter;
    private Uri mUri;
    private TextView mTextView;
    private ShareActionProvider mShareActionProvider;
    private View rootView;
    private String FORECAST_SHARE_HASHTAG = "Sunshine App";

    public static class ViewHolderDetail {
        //public final ImageView iconView;
        public final TextView dayView;
        public final TextView dateView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final ImageView iconView;
        public final TextView descView;
        public final TextView humidityView;
        public final TextView windInfo;
        public final TextView pressure;
        public ViewHolderDetail(View view) {
            //iconView  = (ImageView) view.findViewById(R.id.image_icon);
            dayView = (TextView) view.findViewById(R.id.item_day);
            dateView = (TextView) view.findViewById(R.id.item_date);
            highTempView = (TextView) view.findViewById(R.id.item_high_temp);
            lowTempView = (TextView) view.findViewById(R.id.item_low_temp);
            iconView = (ImageView) view.findViewById(R.id.image_icon);
            descView = (TextView) view.findViewById(R.id.item_desc);
            humidityView = (TextView) view.findViewById(R.id.item_humidity);
            windInfo = (TextView) view.findViewById(R.id.item_wind);
            pressure = (TextView) view.findViewById(R.id.item_pressure);
        }
    }
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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
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
    static final int COL_LOCATION_SETTING = 5;
    //static final int COL_COORD_LAT = 7;
    //static final int COL_COORD_LONG = 8;
    static final int COL_WEATHER_HUMIDITY = 6;
    static final int COL_WEATHER_WIND_SPEED = 7;
    static final int COL_WEATHER_PRESSURE = 8;
    static final int COL_WEATHER_DEGREES = 9;
    static final int COL_WEATHER_CONDITION_ID = 10;

    public DetailFragment() {
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail, menu);
        MenuItem item = menu.findItem(R.id.menu_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
        //return true;
    }

    public Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.detail_forecast, container, false);
        //Intent intent = getActivity().getIntent();
        setHasOptionsMenu(true);
        Bundle arguments = getArguments();
        if( arguments != null ) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        ViewHolderDetail viewHolderDetail = new ViewHolderDetail(rootView);
        rootView.setTag(viewHolderDetail);
        return rootView;

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    public void onLocationChanged(String newLocation) {
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
        //getLoaderManager().restartLoader(DETAIL_LOADER,null,this);
    }


    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Uri uri = Uri.parse(mForecastStr);
        if (mUri != null) {
            CursorLoader cursorLoader = new CursorLoader(getActivity(), mUri, FORECAST_COLUMNS, null, null, null);
            return cursorLoader;
        }
        return null;
    }


    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        String dateString = Utility.getDayName(getActivity(), data.getLong(COL_WEATHER_DATE));
        ViewHolderDetail viewHolderDetail = (ViewHolderDetail) rootView.getTag();
        viewHolderDetail.dateView.setText(dateString);

        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        viewHolderDetail.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        String dayString = Utility.getFormattedMonthDay(getActivity(),data.getLong(COL_WEATHER_DATE));
        viewHolderDetail.dayView.setText(dayString);

        // set content Description for icon and description view
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        viewHolderDetail.descView.setText(weatherDescription);
        viewHolderDetail.descView.setContentDescription(getString
                (R.string.a11y_forecast,weatherDescription));
        viewHolderDetail.iconView.setContentDescription(getString
                (R.string.a11y_forecast_icon,weatherDescription));

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP));
        viewHolderDetail.highTempView.setText(high);
        viewHolderDetail.highTempView.setContentDescription(getString(R.string.a11y_high_temp,high));

        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP));
        viewHolderDetail.lowTempView.setText(low);
        viewHolderDetail.lowTempView.setContentDescription(getString(R.string.a11y_low_temp,low));

        String humidity =  String.valueOf(data.getDouble(COL_WEATHER_HUMIDITY));
        viewHolderDetail.humidityView.setText(" Humidity: " + humidity + "%");
        viewHolderDetail.humidityView.setContentDescription(humidity);

        float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
        float degrees = data.getFloat(COL_WEATHER_DEGREES);
        String windWithDirection = Utility.getFormattedWind(getActivity(),windSpeed,degrees);
        viewHolderDetail.windInfo.setText(windWithDirection);
        viewHolderDetail.windInfo.setContentDescription(windWithDirection);


        String pressure = String.valueOf(data.getLong(COL_WEATHER_PRESSURE));
        viewHolderDetail.pressure.setText(" Pressure: "+pressure+" hPa");
        viewHolderDetail.pressure.setContentDescription(pressure);

        //sharing of data
        mForecast = String.format("%s - %s - %s - %s ", dateString, weatherDescription, high, low);
        //mTextView.setText(mForecast);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
