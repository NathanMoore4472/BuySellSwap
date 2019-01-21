package com.mooresedge.buysellswap;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Locale;


/**
 * Created by Nathan on 01/05/2017.
 */

public class CustomEditText extends EditText
{
    int PriceCount = 0;

    public CustomEditText(Context context) {
        this(context, null);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void Init()
    {
        setCursorVisible(false);
        setOnClickListener();
        setOnFocusChangeListener();
        addTextChangedListener();
    }


    public void setOnClickListener() {
        OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveCaret();
            }
        };

        super.setOnClickListener(l);
    }

    public void addTextChangedListener() {

        TextWatcher watcher = new TextWatcher() {
            String current = "£0.00";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[£,.]", "");

                    double parsed = Double.parseDouble(cleanString);
                    Locale.setDefault(Locale.UK);
                    String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));


                    current = formatted;
                    setText(formatted);
                    setSelection(formatted.length());

                    addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        super.addTextChangedListener(watcher);
    }

    public void setOnFocusChangeListener() {

        OnFocusChangeListener l = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && PriceCount == 0){
                    setText("£0.00");
                    PriceCount++;
                }
                moveCaret();
            }
        };


        super.setOnFocusChangeListener(l);
    }

    public void moveCaret()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               setSelection(getText().length());
            }
        }, 10);
    }
}
