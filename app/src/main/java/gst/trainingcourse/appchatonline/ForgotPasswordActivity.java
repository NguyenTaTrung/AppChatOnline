package gst.trainingcourse.appchatonline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private EditText mEditTxtEmail;
    private Button mBtnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initView();
        initAction();
    }

    private void initAction() {
        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEditTxtEmail.getText().toString().trim();

                if (email.matches("")) {
                    Toast.makeText(getApplicationContext(), "You have not entered Email!", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Please check your Email!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.putExtra("email", email);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void initView() {
        mToolBar = findViewById(R.id.toolBar);
        setSupportActionBar(mToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mEditTxtEmail = findViewById(R.id.editTextEmail);
        mBtnReset = findViewById(R.id.btnReset);
    }
}
