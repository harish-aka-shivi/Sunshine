package app.com.example.android.sunshine;

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
import android.widget.Button;
import android.widget.EditText;

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
