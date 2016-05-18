package app.com.example.android.sunshine;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by root on 22/4/16.
 */
public class LocationEditTextPreference extends EditTextPreference {
    public final String LOG_TAG = "LocationEditTextPref";
    public final int DEFAULT_MINIMUM_LOCATION_LENGTH = 4;
    private int mMinLength;
    private EditText mEditText;
    private Dialog mDialog;
    private Button mButton;

    public LocationEditTextPreference(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes
                (attributeSet,R.styleable.LocationEditTextPreference,0,0);
        try {
            // set the min length
            mMinLength = typedArray.getInteger(R.styleable.LocationEditTextPreference_minLength,
                    DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            // recycle the typed array to enable it for garbage collection
            typedArray.recycle();
        }

        // Check to see if Google Play services is available. The Place Picker API is available
        // through Google Play services, so if this is false, we'll just carry on as though this
        // feature does not exist. If it is true, however, we can add a widget to our preference.
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (resultCode == ConnectionResult.SUCCESS) {
            // Add the get current location widget to our location preference
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        final View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        if ( currentLocation != null) {

            currentLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    //Toast.makeText(getContext(), "Hello", Toast.LENGTH_LONG).show();

                    //create a PlacePicker.IntentBuilder object here
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                    Activity settingsActivity = (SettingsActivity) context;

                    try {

                        //launch the intent using your settings Activity
                        settingsActivity.startActivityForResult(builder.build(settingsActivity),
                                SettingsActivity.PLACE_PICKER_REQUEST);
                    }
                    catch (GooglePlayServicesNotAvailableException |
                            GooglePlayServicesRepairableException e) {

                        // What did you do?? This is why we check Google Play services in onResume!!!
                        // The difference in these exception types is the difference between pausing
                        // for a moment to prompt the user to update/install/enable Play services vs
                        // complete and utter failure.
                        // If you prefer to manage Google Play services dynamically, then you can do so
                        // by responding to these exceptions in the right moment. But I prefer a cleaner
                        // user experience, which is why you check all of this when the app resumes,
                        // and then disable/enable features based on that availability.

                    }
                }
            });
        }
        return view;
    }

    @Override
    protected void showDialog(Bundle state) {

        //first let the dialog formed
        super.showDialog(state);

        mEditText = getEditText();
        setTextChangedListener();

        mDialog = getDialog();
        android.app.AlertDialog alertDialog = (android.app.AlertDialog) mDialog;
        mButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        if (mEditText.getText().toString().length() >= DEFAULT_MINIMUM_LOCATION_LENGTH) {
            //mButton.setClickable(true);
            mButton.setEnabled(true);
        } else {
            //mButton.setClickable(false);
            mButton.setEnabled(false);
        }
    }

    // method to set listener on edit text
    public void setTextChangedListener() {

        mEditText.addTextChangedListener(new TextWatcher() {

            // don't need these method right now
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(mDialog instanceof android.app.AlertDialog) {

                    Editable editable = mEditText.getText();
                    String loc = editable.toString();

                    // if length is greater than  3
                    if(loc.length() >= DEFAULT_MINIMUM_LOCATION_LENGTH) {
                        //mButton.setClickable(true);
                        mButton.setEnabled(true);
                    }
                    else {
                        //mButton.setClickable(false);
                        mButton.setEnabled(false);
                    }
                }
            }
        });
    }
}
