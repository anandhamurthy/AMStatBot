package com.amstatbot.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amstatbot.MainActivity;
import com.amstatbot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {


    private String verificationId, Phone_Number, Name;
    private FirebaseAuth mAuth;
    private TextInputEditText Verify_Code;
    private TextView Verify_Timer;
    private TextView Description;
    private Button Verify_Done, Verify_Resend;
    private DatabaseReference mUsersDatabase;

    private CountDownTimer countDownTimer;

    private boolean startTimer = false;

    private final long startTime = (120 * 1000);
    private final long interval = 1 * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        Verify_Code = findViewById(R.id.verify_code);
        Verify_Done=findViewById(R.id.verify_done);
        Description=findViewById(R.id.verify_text);
        Verify_Timer=findViewById(R.id.verify_timer);
        Verify_Resend=findViewById(R.id.verify_resend);


        countDownTimer = new MyCountDownTimer(startTime, interval);

        Verify_Timer.setText(Verify_Timer.getText() + String.valueOf(startTime / (60 * 1000)));
        timerControl(true);


        Phone_Number = getIntent().getStringExtra("phonenumber");
        Name = getIntent().getStringExtra("name");
        Description.setText("Waiting to automatically detect a SMS sent to "+Phone_Number+".");
        sendVerificationCode(Phone_Number);

        Verify_Resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode(Phone_Number);
                timerControl(true);
            }
        });


        Verify_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=Verify_Code.getText().toString();
                if (s.isEmpty() || s.length() < 6) {
                    Verify_Code.setError("Enter code.");
                    Verify_Code.requestFocus();
                } else {
                    verifyCode(s.toString());
                }
            }
        });


    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        timerControl(false);
        final ProgressDialog pd = new ProgressDialog(VerifyActivity.this);
        pd.setMessage("Logging in.");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            final HashMap userMap = new HashMap<>();
                            userMap.put("phone_number", Phone_Number);
                            userMap.put("name", Name);
                            userMap.put("verify", true);
                            userMap.put("user_id", mAuth.getCurrentUser().getUid());

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            final String uid = current_user.getUid();

                            mUsersDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.hasChild(uid)) {
                                        Intent setupIntent = new Intent(VerifyActivity.this, MainActivity.class);
                                        setupIntent.putExtra("access","false");
                                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(setupIntent);
                                        pd.dismiss();
                                        finish();
                                    }else{
                                        mUsersDatabase.child(uid).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    Intent setupIntent = new Intent(VerifyActivity.this, MainActivity.class);
                                                    setupIntent.putExtra("access","false");
                                                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(setupIntent);
                                                    pd.dismiss();
                                                    finish();

                                                }

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }


                    }
                });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                Verify_Code.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    public void timerControl(Boolean startTimer) {
        if (startTimer) {
            countDownTimer.start();
            Verify_Resend.setVisibility(View.GONE);
            Verify_Done.setVisibility(View.VISIBLE);

        } else {
            countDownTimer.cancel();
            Verify_Resend.setVisibility(View.VISIBLE);
            Verify_Done.setVisibility(View.GONE);

        }

    }


    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }


        @Override
        public void onFinish() {
            Verify_Timer.setText("00 : 00");
            Verify_Resend.setVisibility(View.VISIBLE);
        }


        @Override
        public void onTick(long millisUntilFinished) {

            long currentTime = millisUntilFinished/1000 ;

            Verify_Timer.setText("" + currentTime/60 + " : " +((currentTime % 60)>=10 ? currentTime % 60:"0" +( currentTime % 60)));

        }

    }
}
