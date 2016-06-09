package com.example.anil.learnpaypal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {
    private Button mOrder;
    private TextView mAmount;
    private static final String CONFIG_CLIENT_ID = "AW-h3ZcYTTcwOYYHx0CNf-cpn0x9tzjhfmKvSbd2J4cTZuG1nevI6UxJZnhvz9gV7i3Tx_ZnTV7v1_28";
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static PayPalConfiguration config = new PayPalConfiguration()
    .environment(CONFIG_ENVIRONMENT)
    .clientId(CONFIG_CLIENT_ID)
// the following are only used in PayPalFuturePaymentActivity.
    .merchantName("Hipster Store")
    .merchantPrivacyPolicyUri(
            Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(
            Uri.parse("https://www.example.com/legal"));
    PayPalPayment thingToBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOrder = (Button) findViewById(R.id.order);
        mAmount = (TextView) findViewById(R.id.textView1);
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        mOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thingToBuy = new PayPalPayment(new BigDecimal("10"), "USD",
                        "HeadSet", PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(MainActivity.this,
                        PaymentActivity.class);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        System.out.println(confirm.toJSONObject().toString(4));
                        System.out.println(confirm.getPayment().toJSONObject()
                                .toString(4));
                        Toast.makeText(getApplicationContext(), "Order placed",
                                Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                System.out
                        .println("An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth = data
                        .getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject()
                                .toString(4));
                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("FuturePaymentExample", authorization_code);
                        sendAuthorizationToServer(auth);
                        Toast.makeText(getApplicationContext(),
                                "Future Payment code received from PayPal",
                                Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Log.e("FuturePaymentExample",
                                "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }
    }
    private void sendAuthorizationToServer(PayPalAuthorization authorization) {
    }

    public void onFuturePaymentPurchasePressed(View pressed) {
// Get the Application Correlation ID from the SDK
        String correlationId = PayPalConfiguration
                .getApplicationCorrelationId(this);
        Log.i("FuturePaymentExample", "Application Correlation ID: "
                + correlationId);
// TODO: Send correlationId and transaction details to your server for
// processing with
// PayPal...
        Toast.makeText(getApplicationContext(),
                "App Correlation ID received from SDK", Toast.LENGTH_LONG)
                .show();
    }
    @Override
    public void onDestroy() {
// Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

}
