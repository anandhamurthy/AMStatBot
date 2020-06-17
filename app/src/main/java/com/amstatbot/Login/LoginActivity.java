package com.amstatbot.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amstatbot.R;
import com.google.android.material.textfield.TextInputEditText;
import com.rilixtech.widget.countrycodepicker.Country;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class LoginActivity extends AppCompatActivity{

    private CountryCodePicker Login_Country_Code;
    private TextInputEditText Login_Name, Login_Phone_Number;
    private Button Login_Verify;
    private String Code="+91";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Login_Name = findViewById(R.id.login_name);
        Login_Phone_Number = findViewById(R.id.login_phone_number);
        Login_Verify= findViewById(R.id.login_verify);
        Login_Country_Code = findViewById(R.id.login_country_code);

        Login_Country_Code.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected(Country selectedCountry) {
                Code=Login_Country_Code.getSelectedCountryCodeWithPlus();

            }
        });

        Login_Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String number = Login_Phone_Number.getText().toString().trim();
                final String name = Login_Name.getText().toString();

                if (number.isEmpty() || number.length() < 10) {
                    Login_Phone_Number.setError("Valid number is required");
                    Login_Phone_Number.requestFocus();
                    return;
                }
                if (!name.isEmpty()){
                    Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("phonenumber", Code+number);
                    startActivity(intent);
                }else{
                    Toast.makeText(LoginActivity.this, "Enter your Name.", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

}

