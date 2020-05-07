package gst.trainingcourse.appchatonline.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import gst.trainingcourse.appchatonline.main.MainActivity;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.lib.CheckConnectionInternet;

public class LoginActivity extends AppCompatActivity {

    private Button mButtonLogin;
    private CheckBox mCheckBox;
    private EditText mEditTextMail, mEditTextPass;
    private String mEmail, mPass;
    private TextView mTxtRegister, mTxtForgotPass;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Dialog mDialog;
    private CheckConnectionInternet mCheckConnectInternet = new CheckConnectionInternet(this);

    private static final int REQUEST_CODE_REGISTER = 1;
    private static final int REQUEST_CODE_FORGOTPASSS = 2;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onStart() {
        super.onStart();
        //auto đăng nhập..
        if (mCheckConnectInternet.isConnected()) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mUser != null) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            //
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSharedPreferences = getSharedPreferences("MyShare", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        initView();
        initAction();
    }

    private void initAction() {
        mTxtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REGISTER);
            }
        });

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCheckConnectInternet.isConnected()) {
                    mEmail = mEditTextMail.getText().toString();
                    mPass = mEditTextPass.getText().toString();
                    mDialog.show();

                    if (mEmail.matches("") || mPass.matches("")) {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), R.string.toast_have_not_enter_information, Toast.LENGTH_SHORT).show();
                    } else {
                        mAuth = FirebaseAuth.getInstance();
                        mAuth.signInWithEmailAndPassword(mEmail, mPass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            if (mCheckBox.isChecked()) {
                                                mEditor.putString("email", mEmail);
                                                mEditor.putString("password", mPass);
                                                mEditor.commit();
                                            }
                                            mDialog.dismiss();
                                            finish();
                                        } else {
                                            mDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), R.string.toast_login_fail, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_check_connect, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mTxtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivityForResult(intent, REQUEST_CODE_FORGOTPASSS);
            }
        });

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    mEditor.clear();
                }
                mEditor.putBoolean("check", b);
                mEditor.commit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_REGISTER:
                if (resultCode == RESULT_OK && data != null) {
                    mEmail = data.getStringExtra("email");
                    mPass = data.getStringExtra("password");
                    mEditTextMail.setText(mEmail);
                    mEditTextPass.setText(mPass);
                }
                break;
            case REQUEST_CODE_FORGOTPASSS:
                if (resultCode == RESULT_OK && data != null) {
                    mEmail = data.getStringExtra("email");
                    mEditTextMail.setText(mEmail);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        mEditTextMail = findViewById(R.id.editTextEmail);
        mEditTextPass = findViewById(R.id.editTextPass);
        mButtonLogin = findViewById(R.id.btnLogin);
        mCheckBox = findViewById(R.id.checkBoxInfo);
        mTxtRegister = findViewById(R.id.txtRegister);
        mTxtForgotPass = findViewById(R.id.txtForgotPassword);

        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_loading);
        TextView title = mDialog.findViewById(R.id.txtLogin);
        title.setText("Waiting for login...");
        mDialog.setCancelable(false);

        mEditTextMail.setText(mSharedPreferences.getString("email", ""));
        mEditTextPass.setText(mSharedPreferences.getString("password", ""));
        mCheckBox.setChecked(mSharedPreferences.getBoolean("check", false));
    }
}
