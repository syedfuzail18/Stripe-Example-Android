package com.stripeexample.com.stripeexample;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

/**
 * Created by Syed Fuzail
 * contact:
 * byhaqi.fuzail@yahoo.com
 * byhaqi.fuzail@gmail.com
 */

public class MainActivity extends AppCompatActivity {

    private EditText mNumber, mMonth, mYear, mCVC, mName;
    private boolean mLenNumber = false, mLenMonth = false, mLenYear = false, mLenCVC = false, mLenName = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mNumber = findViewById(R.id.num);
        mMonth = findViewById(R.id.month);
        mYear = findViewById(R.id.year);
        mCVC = findViewById(R.id.cvc);
        mName = findViewById(R.id.card_name);
        Button pay = findViewById(R.id.pay);

        mNumber.addTextChangedListener(new FourDigitCardFormatWatcher());

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hides keyboard
                hideKeyboard(MainActivity.this);

                if (validateInputData())
                    cardDetails();
            }
        });
    }

    // This validation only checks that the editTexts have the exact lenth we wanted
    private boolean validateInputData() {
        if (mNumber.getText().toString().length() == 19) {
            mLenNumber = true;
        } else
            mNumber.setError("Invalid Card Number");

        if (mMonth.getText().toString().length() > 0) {
            mLenMonth = true;
        } else
            mMonth.setError("Invalid Month");

        if (mYear.getText().toString().length() == 2) {
            mLenYear = true;
        } else
            mYear.setError("Invalid Year");

        if (mCVC.getText().toString().length() == 3) {
            mLenCVC = true;
        } else
            mCVC.setError("Invalid CVC");

        if (mName.getText().toString().length() > 0) {
            mLenName = true;
        } else
            mName.setError("Invalid Name");

        if (mLenCVC && mLenYear && mLenMonth && mLenNumber) {
            return true;
        }
        return false;
    }

    // This methods Creates a card which holds our edittext info
    private void cardDetails() {

        String num = mNumber.getText().toString().replaceAll("-", "");
        Card.Builder build = new Card.Builder(
                num,
                Integer.valueOf(mMonth.getText().toString()),
                Integer.valueOf(mYear.getText().toString()),
                mCVC.getText().toString()
        );
        build.name(mName.getText().toString());
        //You can add many more fields like address, pin, etc
        Card card = build.build();

        if (!card.validateCVC()) {
            mCVC.setError("Invalid Card CVC");
            return;
        }
        if (!card.validateExpMonth()) {
            mMonth.setError("Invalid Card Month");
            return;
        }
        if (!card.validateNumber()) {
            mNumber.setError("Invalid Card Number");
            return;
        }
        if (!card.validateExpiryDate()) {
            mYear.setError("Invalid Card Year");
            return;
        }

        if (card.validateCard()) { //if card is valid then stripe payment is called
            stripeCall(card);
        } else {
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }

    // This methods hits the Stripe Api to get token
    private void stripeCall(Card card) {

        try {
            String KEY = "your key";
            Stripe stripe = new Stripe(MainActivity.this, KEY);
            stripe.createToken(
                    card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server that will manage the further payment handling
                            Toast.makeText(MainActivity.this,
                                    "Successfull payment",
                                    Toast.LENGTH_SHORT
                            ).show();
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

    //hides the keyboard
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
