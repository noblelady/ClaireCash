package com.noble.claire.clairecash;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";

    EditText etDollar;
    EditText etName;
    EditText etComment;
    Button btnPay;

    ScrollView svPayments;
    LinearLayout llPayments;

    DatabaseReference db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //connect each of the things
        etDollar = (EditText) findViewById(R.id.et_dollar);
        etName = (EditText) findViewById(R.id.et_name);
        etComment = (EditText) findViewById(R.id.et_comment);

        btnPay = (Button) findViewById(R.id.btn_pay);

        svPayments = (ScrollView) findViewById(R.id.sv_payments);
        llPayments = (LinearLayout) findViewById(R.id.ll_payments);

        db = FirebaseDatabase.getInstance().getReference();
        DatabaseReference paymentRef = db.child("payments");

        // Read from the database
        paymentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                llPayments.removeAllViews();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String name = (String) postSnapshot.child("name").getValue();
                    double amount = 0;
                    if(postSnapshot.child("amount").getValue() instanceof Long){
                        amount = ((Long) postSnapshot.child("amount").getValue()).doubleValue();
                    }else{
                        amount = (double) postSnapshot.child("amount").getValue();
                    }
                    String comment = (String) postSnapshot.child("comment").getValue();
                    String amountS = amount+"";
                    if(BigDecimal.valueOf(amount).scale() < 2){
                        amountS = amount + "0";
                    }

                    String postStr = name + " paid Claire $" + amountS + "\n" + comment;
                    TextView tvPayment = new TextView(MainActivity.this,null,R.style.PaymentItems);
                    tvPayment.setText(postStr);
                    tvPayment.setTextSize(20);
                    tvPayment.setPadding(10,10,10,10);
                    llPayments.addView(tvPayment);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting payments failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    public void createPayment(View view){
        Context context = getApplicationContext();

        //TODO: Check if Dollar amount was entered
        if(etDollar.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(context, "Please enter an amount!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if(etName.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(context, "Please enter your name!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if(etComment.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(context, "Please enter a comment!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if(BigDecimal.valueOf(Double.parseDouble(etDollar.getText().toString())).scale() > 2){
            Toast toast = Toast.makeText(context, "Please enter a valid amount!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Map<String, Object> payment = new HashMap<>();
        payment.put("name", etName.getText().toString());
        payment.put("amount", Double.parseDouble(etDollar.getText().toString()));
        payment.put("comment", etComment.getText().toString());

        DatabaseReference paymentRef = db.child("payments");
        paymentRef.push().setValue(payment);
        clearFields();
    }

    //TODO: Add menu!

    //TODO: Figure out the total that people have donated?

    public void clearFields(){
        etDollar.setText("");
        etComment.setText("");
        etName.setText("");
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
