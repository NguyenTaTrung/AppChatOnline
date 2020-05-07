package gst.trainingcourse.appchatonline.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.model.Account;

public class RegisterActivity extends AppCompatActivity {

    private Button mButtonRegister, mButtonBack;
    private EditText mEditTextName, mEditTextPass, mEditTextEmail;
    private String mName, mPass, mEmail;
    private CircleImageView mCircleImageView;
    private Dialog mDialog;

    private static final int REQUEST_CODE_IMAGE = 111;

    private FirebaseAuth mAuth;
    private DatabaseReference mReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReferenceFromUrl("gs://appchattest-51e06.appspot.com");
        initAction();
    }

    private void register(final String username, String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Calendar calendar = Calendar.getInstance();
                            StorageReference storageReference = mStorageReference.child("imageUser/").child("imageUser" + calendar.getTimeInMillis() + ".png");

                            mCircleImageView.setDrawingCacheEnabled(true);
                            mCircleImageView.buildDrawingCache();
                            Bitmap newBitmap = mCircleImageView.getDrawingCache();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            byte[] dataImg = baos.toByteArray();

                            UploadTask uploadTask = storageReference.putBytes(dataImg);
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> downloadUri = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                    downloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imgUrl = uri.toString();

                                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                            assert firebaseUser != null;
                                            String userId = firebaseUser.getUid();

                                            //nhánh tên Users có nốt con là userId (key auto tạo trong firebase)
                                            mReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                                            //đẩy dữ liệu vào nhánh con userId có các thuộc tính id, username, imageUrl, status và values tương ứng (Hàm setValue)
                                            Account account = new Account(userId, username, imgUrl, "offline", false,"no introduce");

                                            mReference.setValue(account).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), R.string.txt_register_success, Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent();
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        intent.putExtra("email", mEmail);
                                                        intent.putExtra("password", mPass);
                                                        setResult(RESULT_OK, intent);
                                                        mDialog.dismiss();
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.toast_enter_email_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initView() {
        mEditTextName = findViewById(R.id.editTextName);
        mEditTextPass = findViewById(R.id.editTextPass);
        mEditTextEmail = findViewById(R.id.editTextEmail);
        mButtonRegister = findViewById(R.id.btnRegister);
        mButtonBack = findViewById(R.id.btnBack);
        mCircleImageView = findViewById(R.id.imgUser);

        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_loading);
        TextView title = mDialog.findViewById(R.id.txtLogin);
        title.setText("Waiting for register...");
        mDialog.setCancelable(false);
    }

    private void initAction() {
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mName = mEditTextName.getText().toString().trim();
                mPass = mEditTextPass.getText().toString().trim();
                mEmail = mEditTextEmail.getText().toString().trim();

                if (mName.matches("") || mPass.matches("") || mEmail.matches("")) {
                    Toast.makeText(getApplicationContext(), R.string.toast_have_not_enter_information, Toast.LENGTH_SHORT).show();
                } else if (mPass.length() < 6) {
                    Toast.makeText(getApplicationContext(), R.string.toast_check_password, Toast.LENGTH_SHORT).show();
                } else {
                    mDialog.show();
                    mReference = FirebaseDatabase.getInstance().getReference("Users");
                    Query query = mReference.orderByChild("username").equalTo(mName);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mDialog.dismiss();
                                Toast.makeText(getApplicationContext(), R.string.toast_alredy_exist_username, Toast.LENGTH_SHORT).show();
                            } else {
                                register(mName, mEmail, mPass);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
            }
        });

        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mCircleImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
