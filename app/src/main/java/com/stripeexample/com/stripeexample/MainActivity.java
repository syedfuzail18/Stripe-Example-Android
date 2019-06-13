package com.stripeexample.com.stripeexample;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.APIConnectionException;
import com.stripe.android.exception.APIException;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.exception.InvalidRequestException;
import com.stripe.android.model.Card;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentIntentParams;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardMultilineWidget;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText number, month, year, cvc, name;
    private String KEY = "Your Key";
    private Button pay;
    private CardMultilineWidget cardMultilineWidget;
    boolean lenNumber = false, lenMonth = false, lenYear = false, lenCVC = false,lenName=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        number = findViewById(R.id.num);
        month = findViewById(R.id.month);
        year = findViewById(R.id.year);
        cvc = findViewById(R.id.cvc);
        name = findViewById(R.id.card_name);
        pay = findViewById(R.id.pay);

        number.addTextChangedListener(new FourDigitCardFormatWatcher());

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hides keyboard
                hideKeyboard(MainActivity.this);

                if (validateInputData())
                    payViaStripe();
            }
        });
    }

    private boolean validateInputData() {
        if (number.getText().toString().length() == 19) {
            lenNumber = true;
        } else
            number.setError("Invalid Card Number");

        if (month.getText().toString().length() > 0) {
            lenMonth = true;
        } else
            month.setError("Invalid Month");

        if (year.getText().toString().length() == 2) {
            lenYear = true;
        } else
            year.setError("Invalid Year");

        if (cvc.getText().toString().length() == 3) {
            lenCVC = true;
        } else
            cvc.setError("Invalid CVC");

        if(name.getText().toString().length()>0){
            lenName=true;
        }else
            name.setError("Invalid Name");

        if (lenCVC && lenYear && lenMonth && lenNumber) {
            return true;
        }
        return false;
    }

    private void payViaStripe() {

        String num = number.getText().toString().replaceAll("-", "");
        Card.Builder build = new Card.Builder(
                num,
                Integer.valueOf(month.getText().toString()),
                Integer.valueOf(year.getText().toString()),
                cvc.getText().toString()
        );
        build.name(name.getText().toString());
        Card card = build.build();

        if (!card.validateCVC()) {
            cvc.setError("Invalid Card CVC");
            return;
        }
        if (!card.validateExpMonth()) {
            month.setError("Invalid Card Month");
            return;
        }
        if (!card.validateNumber()) {
            number.setError("Invalid Card Number");
            return;
        }
        if(!card.validateExpiryDate()){
            year.setError("Invalid Card Year");
            return;
        }

        if (card.validateCard()) { //if card is valid then stripe payment is called
            callApi(card);
        } else {
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }

    private void callApi(Card card) {

        try {
            Stripe stripe = new Stripe(MainActivity.this, KEY);
            stripe.createToken(
                    card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                            Toast.makeText(MainActivity.this,
                                    "Successfull payment",
                                    Toast.LENGTH_SHORT
                            ).show();

                            //here you can call your own api from your server and send the token
                        }

                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(MainActivity.this,
                                    "error in payment",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        } catch (Exception e) {

        }

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
