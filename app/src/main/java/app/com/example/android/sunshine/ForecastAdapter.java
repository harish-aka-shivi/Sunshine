package app.com.example.android.sunshine;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import app.com.example.android.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout = true;
    private Cursor mCursor;
    final private Context mContext;
    final private ForecastAdapterOnClickHandler mForecastAdapterOnClickListener;
    final private View mEmptyView;
    final private ItemChoiceManager mICM;

    /*@Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }*/

    public ForecastAdapter(Context context,ForecastAdapterOnClickHandler forecastAdapterOnClickHandler ,
                           View emptyView, int choiceMode) {
        mContext = context;
        mForecastAdapterOnClickListener = forecastAdapterOnClickHandler;
        mEmptyView =  emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
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

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;


        public ForecastAdapterViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);

            mForecastAdapterOnClickListener.onClick(mCursor.getLong(dateColumnIndex),this);
        }
    }
    /*@Override
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
    }*/
    /*
        This is where we fill-in the views with the contents of the cursor.
     */

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                }
                case VIEW_TYPE_FUTURE_DAY: {
                    layoutId = R.layout.list_item_forecast;
                    break;
                }
            }
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    /*@Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;
        // Use placeholder image for now
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }

        if (Utility.usingLocalGraphics(mContext)) {
            viewHolder.mIconView.setImageResource(defaultImage);
        } else {
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(viewHolder.mIconView);
        }




        // TODO Read date from cursor
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = viewHolder.mDateView;
        dateView.setText(Utility.getFriendlyDayString(context,date));

        // TODO Read weather forecast from cursor
        String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView descView = viewHolder.mDescriptionView;
        descView.setText(desc);
        descView.setContentDescription(mContext.getString(R.string.a11y_forecast,desc));
        viewHolder.mIconView.setContentDescription(mContext.getString(R.string.a11y_forecast_icon,desc));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = viewHolder.mHighTempView;
                //(TextView) view.findViewById(R.id.list_item_high_textview);
        String temp_high = Utility.formatTemperature(mContext,high);
        highView.setText(temp_high);
        highView.setContentDescription(mContext.getString(R.string.a11y_high_temp,high));

        // TODO Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = viewHolder.mLowTempView;
                //(TextView) view.findViewById(R.id.list_item_low_textview);
        String temp_low = Utility.formatTemperature(mContext,low);
        lowView.setText(temp_low);
        lowView.setContentDescription(mContext.getString(R.string.a11y_low_temp,temp_low));
    }*/

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        Log.d("weather_Id ",String.valueOf(weatherId));
        int defaultImage;
        int viewType = getItemViewType(position);
        boolean useLongToday;

        switch (viewType) {
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                useLongToday = true;
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                useLongToday = false;
                break;
        }

        if (Utility.usingLocalGraphics(mContext)) {
            forecastAdapterViewHolder.mIconView.setImageResource(defaultImage);
        } else {
            Log.d("GLIDE_URL",Utility.getImageUrlForWeatherCondition(weatherId) );
            Glide.with(mContext)
                    .load(Utility.getImageUrlForWeatherCondition(weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(forecastAdapterViewHolder.mIconView);
        }

        // TODO Read date from cursor
        long date = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = forecastAdapterViewHolder.mDateView;
        dateView.setText(Utility.getFriendlyDayString(mContext,date,useLongToday));

        // TODO Read weather forecast from cursor
        String desc = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView descView = forecastAdapterViewHolder.mDescriptionView;
        descView.setText(desc);
        descView.setContentDescription(mContext.getString(R.string.a11y_forecast,desc));

        ImageView iconView = forecastAdapterViewHolder.mIconView;

        // we give aunique name to our view holder so the system
        // can find it when returning during transition
        ViewCompat.setTransitionName(iconView,"iconView_"+position);

        //my method to add transition name
        //setUniqueTransitionName(iconView,position);

        iconView.setContentDescription(mContext.getString(R.string.a11y_forecast_icon,desc));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(mContext);

        // Read high temperature from cursor
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = forecastAdapterViewHolder.mHighTempView;
        String temp_high = Utility.formatTemperature(mContext,high);
        highView.setText(temp_high);
        highView.setContentDescription(mContext.getString(R.string.a11y_high_temp,high));

        // TODO Read low temperature from cursor
        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView  lowView = forecastAdapterViewHolder.mLowTempView;
        //(TextView) view.findViewById(R.id.list_item_low_textview);
        String temp_low = Utility.formatTemperature(mContext,low);
        lowView.setText(temp_low);
        lowView.setContentDescription(mContext.getString(R.string.a11y_low_temp,temp_low));
    }

    @TargetApi(21)
    public void setUniqueTransitionName(ImageView iconView, int position) {
        String name  = "position_" + String.valueOf(position);
        iconView.setTransitionName(name);
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ?View.VISIBLE : View.INVISIBLE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void setUseTodayLayout(boolean useTodayLoyout) {
        mUseTodayLayout = useTodayLoyout;
    }


    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ForecastAdapterViewHolder ) {
            ForecastAdapterViewHolder vfh = (ForecastAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
    @Override
    public int getItemViewType(int position) {
        return position == 0 && mUseTodayLayout ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public interface ForecastAdapterOnClickHandler {
        void onClick(long date, ForecastAdapterViewHolder forecastAdapterViewHolder);
    }
}