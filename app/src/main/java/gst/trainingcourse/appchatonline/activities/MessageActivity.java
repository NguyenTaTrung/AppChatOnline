package gst.trainingcourse.appchatonline.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.adapter.MessageAdapter;
import gst.trainingcourse.appchatonline.fragments.APIService;
import gst.trainingcourse.appchatonline.listener.ClickMessage;
import gst.trainingcourse.appchatonline.listener.LongClickMessage;
import gst.trainingcourse.appchatonline.model.Account;
import gst.trainingcourse.appchatonline.model.Chat;
import gst.trainingcourse.appchatonline.notification.Client;
import gst.trainingcourse.appchatonline.notification.Data;
import gst.trainingcourse.appchatonline.notification.MyResponse;
import gst.trainingcourse.appchatonline.notification.Sender;
import gst.trainingcourse.appchatonline.notification.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private CircleImageView mImgProfile, mImgOn, mImgOff;
    private TextView mTxtUsername, mTxtTyping;
    private Toolbar mToolbar;
    private ImageButton mImgBtnSend, mImgBtnUpImage, mImgBtnCamera, mImgBtnVoice, mImgBtnStopVoice;
    private EditText mEdtMessage;
    private RecyclerView mRecyclerViewMessage;
    private FloatingActionButton mBtnDownMessage;

    private Dialog mDialog;
    private MessageAdapter mMessageAdapter;

    private String mIdUsers, mNameAccount, mImgUrlAccount;
    private ArrayList<Chat> mArrayChats;

    private final static int REQUEST_CODE_UP_IMAGE = 20;
    private final static int REQUEST_CODE_CAMERA = 30;

    private static String fileName = null;
    private MediaRecorder recorder = null;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;
    private StorageReference mStorageReference;
    private ValueEventListener mValueEventListener;
    private APIService mApiService;
    private boolean mCheckNotify = false;

    private ClickMessage mClickMessage = new ClickMessage() {
        @Override
        public void onClickMes(int position) {
            Chat chat = mArrayChats.get(position);
            if (chat.getType().equals("image")) {
                Intent intent = new Intent(MessageActivity.this, ShowImageActivity.class);
                intent.putExtra("imgurl", chat.getMessage());
                intent.putExtra("name", mNameAccount);
                intent.putExtra("img", mImgUrlAccount);
                startActivity(intent);
            } else if (chat.getType().equals("audio")) {

            }
        }
    };

    private LongClickMessage mLongClickMes = new LongClickMessage() {
        @Override
        public void onLongClickMes(final int position) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
            builder.setTitle("You want to delete message?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Chat chat = mArrayChats.get(position);
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
                            Query query = reference.orderByChild("message").equalTo(chat.getMessage());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
        }
    };

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        setContentView(R.layout.activity_message);

        initView();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mApiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://appchattest-51e06.appspot.com");
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        initData();
        initAction();
        seenMessage(mIdUsers);
    }

    private void initView() {
        mImgProfile = findViewById(R.id.profileImg);
        mImgOn = findViewById(R.id.imgOn);
        mImgOff = findViewById(R.id.imgOff);
        mTxtUsername = findViewById(R.id.username);
        mTxtTyping = findViewById(R.id.typing);
        mBtnDownMessage = findViewById(R.id.btnDownMes);
        mToolbar = findViewById(R.id.toolBar);
        mEdtMessage = findViewById(R.id.editTextMessage);
        mImgBtnSend = findViewById(R.id.btnSend);
        mImgBtnUpImage = findViewById(R.id.btnUpImage);
        mImgBtnCamera = findViewById(R.id.btnCamera);
        mImgBtnVoice = findViewById(R.id.btnVoice);
        mImgBtnStopVoice = findViewById(R.id.btnStopVoice);
        mRecyclerViewMessage = findViewById(R.id.recyclerviewMessage);

        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_loading);
        TextView title = mDialog.findViewById(R.id.txtLogin);
        title.setText("Waiting...");
        mDialog.setCancelable(false);

        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getSupportActionBar().setTitle("");

        mRecyclerViewMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessage.setLayoutManager(linearLayoutManager);
    }

    private void initData() {
        Intent intent = getIntent();
        mIdUsers = intent.getStringExtra("idUsers");

        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mIdUsers);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Account account = dataSnapshot.getValue(Account.class);
                mNameAccount = account.getUsername();
                mImgUrlAccount = account.getImgUrl();

                mTxtUsername.setText(mNameAccount);
                Picasso.with(getApplicationContext()).load(mImgUrlAccount).into(mImgProfile);
                if (account.getStatus().equals("online")) {
                    mImgOn.setVisibility(View.VISIBLE);
                    mImgOff.setVisibility(View.INVISIBLE);
                } else if (account.getStatus().equals("offline")) {
                    mImgOn.setVisibility(View.INVISIBLE);
                    mImgOff.setVisibility(View.VISIBLE);
                }

                if (account.isTyping()) {
                    mTxtTyping.setVisibility(View.VISIBLE);
                } else {
                    mTxtTyping.setVisibility(View.INVISIBLE);
                }

                readMessage(mFirebaseUser.getUid(), mIdUsers, account.getImgUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initAction() {
        mImgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckNotify = true;
                String message = mEdtMessage.getText().toString();
                if (message.matches("")) {
                    Toast.makeText(getApplicationContext(), R.string.toast_enter_the_content_of_the_chat, Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(mFirebaseUser.getUid(), mIdUsers, message, "text");
                }
                mEdtMessage.setText("");
            }
        });

        mEdtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    //set typing = true
                    typing(true);
                } else {
                    //set typing = false
                    typing(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mImgBtnUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckNotify = true;
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_UP_IMAGE);
            }
        });

        mImgBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckNotify = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        });

        mImgBtnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
                mImgBtnVoice.setVisibility(View.INVISIBLE);
                mImgBtnStopVoice.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Start recoding", Toast.LENGTH_SHORT).show();
            }
        });

        mImgBtnStopVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
                mImgBtnVoice.setVisibility(View.VISIBLE);
                mImgBtnStopVoice.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Stop recoding", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnDownMessage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                mRecyclerViewMessage.scrollToPosition(mArrayChats.size() - 1);
                mBtnDownMessage.setVisibility(View.GONE);
            }
        });

        mRecyclerViewMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && mBtnDownMessage.getVisibility() == View.VISIBLE) {
                    mBtnDownMessage.hide();
                } else if (dy < 0 && mBtnDownMessage.getVisibility() != View.VISIBLE) {
                    mBtnDownMessage.show();
                }
            }
        });
    }

    private void seenMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        mValueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(mFirebaseUser.getUid()) && chat.getSender().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message, final String type) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("type", type);
        hashMap.put("isseen", false);

        //Tạo nhánh Chat gồm nội dung tin nhắn (message), người gửi (sender), người nhận (receiver)
        databaseReference.child("Chats").push().setValue(hashMap);

        final String msg = message;
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Account account = dataSnapshot.getValue(Account.class);
                if (mCheckNotify) {
                    if (type.equals("text")) {
                        sendNotification(receiver, account.getUsername(), msg);
                    } else if (type.equals("image")) {
                        sendNotification(receiver, account.getUsername(), "New image");
                    } else if (type.equals("audio")) {
                        sendNotification(receiver, account.getUsername(), "New Audio");
                    }
                }
                mCheckNotify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage(final String myId, final String userId, final String imgUrl) {
        mArrayChats = new ArrayList<>();

        mReference = FirebaseDatabase.getInstance().getReference("Chats");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mArrayChats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        mArrayChats.add(chat);
                    }

                    mMessageAdapter = new MessageAdapter(MessageActivity.this, mArrayChats, mLongClickMes, mClickMessage, imgUrl);
                    mRecyclerViewMessage.setAdapter(mMessageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = databaseReference.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(mFirebaseUser.getUid(), R.mipmap.ic_launcher, username + ": " + message, "New Message", mIdUsers, "");
                    Sender sender = new Sender(data, token.getToken());

                    mApiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(getApplicationContext(), R.string.toast_fail, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        uploadAudio();
    }

    private void uploadAudio() {
        mDialog.show();
        Calendar calendar = Calendar.getInstance();
        StorageReference storageReference = mStorageReference.child("audio/").child("audioMes" + calendar.getTimeInMillis() + ".3gp");

        final Uri uri = Uri.fromFile(new File(fileName));
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String audioUrl = uri.toString();
                        sendMessage(mFirebaseUser.getUid(), mIdUsers, audioUrl, "audio");
                        mDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_UP_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    mDialog.show();
                    Uri uri = data.getData();

                    Calendar calendar = Calendar.getInstance();
                    StorageReference storageReference = mStorageReference.child("imageChat/").child("imageSendMessage" + calendar.getTimeInMillis() + ".png");

                    UploadTask uploadTask = storageReference.putFile(uri);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> dowloadUri = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            dowloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imgUrl = uri.toString();

                                    sendMessage(mFirebaseUser.getUid(), mIdUsers, imgUrl, "image");
                                    mDialog.dismiss();
                                }
                            });
                        }
                    });
                }
                break;
            case REQUEST_CODE_CAMERA:
                if (resultCode == RESULT_OK && data != null) {
                    mDialog.show();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference storageReference = mStorageReference.child("imageChat/").child("imageSendMessageFromCamera" + calendar.getTimeInMillis() + ".png");

                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] dataImg = byteArrayOutputStream.toByteArray();

                    UploadTask uploadTask = storageReference.putBytes(dataImg);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> dowloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            dowloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imgUrl = uri.toString();

                                    sendMessage(mFirebaseUser.getUid(), mIdUsers, imgUrl, "image");
                                    mDialog.dismiss();
                                }
                            });
                        }
                    });
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void typing(boolean typing) {
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
        mReference.child("typing").setValue(typing);
    }

    private void status(String status) {
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        mReference.updateChildren(hashMap);
    }

    private void currentUser(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userId);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        status("online");
        currentUser(mIdUsers);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMessageAdapter != null) { mMessageAdapter.stopPlayer(); }
//        status("offline");
        typing(false);
        currentUser("none");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.removeEventListener(mValueEventListener);
    }
}
