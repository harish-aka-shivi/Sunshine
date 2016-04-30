package app.com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import app.com.example.android.sunshine.data.WeatherContract;
import app.com.example.android.sunshine.R;
/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout;

    public void setUseTodayLayout(boolean useTodayLoyout) {
        mUseTodayLayout = useTodayLoyout;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && mUseTodayLayout ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }
    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext,high) + "/" +
                Utility.formatTemperature(mContext,low);
        return highLowStr;
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        }
        else {layoutId = R.layout.list_item_forecast;}

        // TODO: Determine layoutId from viewType
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }
    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int fallbackIconId;
        // Use placeholder image for now
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                fallbackIconId = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                fallbackIconId = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }

        Glide.with(mContext).load(Utility.getArtUrlForWeatherCondition(mContext,weatherId)).
                error(fallbackIconId).crossFade().into(viewHolder.iconView);
        // TODO Read date from cursor
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = viewHolder.dateView;
        dateView.setText(Utility.getFriendlyDayString(context,date));

        // TODO Read weather forecast from cursor
        String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView descView = viewHolder.descriptionView;
        descView.setText(desc);
        descView.setContentDescription(mContext.getString(R.string.a11y_forecast,desc));
        viewHolder.iconView.setContentDescription(mContext.getString(R.string.a11y_forecast_icon,desc));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = viewHolder.highTempView;
                //(TextView) view.findViewById(R.id.list_item_high_textview);
        String temp_high = Utility.formatTemperature(mContext,high);
        highView.setText(temp_high);
        highView.setContentDescription(mContext.getString(R.string.a11y_high_temp,high));

        // TODO Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = viewHolder.lowTempView;
                //(TextView) view.findViewById(R.id.list_item_low_textview);
        String temp_low = Utility.formatTemperature(mContext,low);
        lowView.setText(temp_low);
        lowView.setContentDescription(mContext.getString(R.string.a11y_low_temp,temp_low));
    }
}