package gst.trainingcourse.appchatonline.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.adapter.MessageGroupAdapter;
import gst.trainingcourse.appchatonline.fragments.APIService;
import gst.trainingcourse.appchatonline.listener.ClickMessage;
import gst.trainingcourse.appchatonline.listener.LongClickMessage;
import gst.trainingcourse.appchatonline.model.Account;
import gst.trainingcourse.appchatonline.model.GroupChat;
import gst.trainingcourse.appchatonline.model.Grouplist;
import gst.trainingcourse.appchatonline.notification.Client;
import gst.trainingcourse.appchatonline.notification.Data;
import gst.trainingcourse.appchatonline.notification.MyResponse;
import gst.trainingcourse.appchatonline.notification.Sender;
import gst.trainingcourse.appchatonline.notification.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatActivity extends AppCompatActivity {

    private CircleImageView mImgProfile, mImgOn, mImgOff;
    private TextView mTxtUsername, mTxtTyping;
    private Toolbar mToolbar;
    private ImageButton mImgBtnSend, mImgBtnUpImage, mImgBtnCamera, mImgBtnVoice, mImgBtnStopVoice;
    private EditText mEdtMessage;
    private RecyclerView mRecyclerViewMessage;
    private FloatingActionButton mBtnDownMessage;

    private String mGroupName, mImgUrl, mImgGroupUrl, mIdUser;
    private ArrayList<GroupChat> mArrayChats;

    private static final int REQUEST_CODE_UPDATE_IMG = 1;
    private static final int REQUEST_CODE_SENT_IMG = 21;
    private static final int REQUEST_CODE_CAMERA = 31;

    private static String fileName = null;
    private MediaRecorder recorder = null;

    private Dialog mDialog;
    private MessageGroupAdapter mMessageGroupAdapter;

    private APIService mApiService;
    private boolean mNotify = false;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;
    private StorageReference mStorageReference;
    private ValueEventListener mValueEventListener;

    private ClickMessage mClickMes = new ClickMessage() {
        @Override
        public void onClickMes(int position) {
            GroupChat groupChat = mArrayChats.get(position);
            if (groupChat.getType().equals("image")) {
                Intent intent = new Intent(GroupChatActivity.this, ShowImageActivity.class);
                intent.putExtra("imgurl", groupChat.getMessage());
                intent.putExtra("name", mGroupName);
                intent.putExtra("img", mImgGroupUrl);
                startActivity(intent);
            } else if (groupChat.getType().equals("audio")) {

            }
        }
    };

    private LongClickMessage mLongClickMes = new LongClickMessage() {
        @Override
        public void onLongClickMes(final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
            builder.setTitle("You want to delete message?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            GroupChat groupChat = mArrayChats.get(position);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("GroupChatMessage");
                            Query query = databaseReference.orderByChild("message").equalTo(groupChat.getMessage());
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
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 201;

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
        mReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://appchattest-51e06.appspot.com");
        mApiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        initData();
        initAction();
        seenMessage();
    }

    private void seenMessage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupChatMessage");
        mValueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GroupChat groupChat = snapshot.getValue(GroupChat.class);
                    if (!groupChat.getSender().equals(mFirebaseUser.getUid())) {
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

    private void sendMessage(final String id, String message, String type) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        final HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message", message);
        hashMap.put("sender", id);
        hashMap.put("groupname", mGroupName);
        hashMap.put("imgurl", mImgUrl);
        hashMap.put("type", type);
        hashMap.put("isseen", false);

        databaseReference.child("GroupChatMessage").push().setValue(hashMap);
        if (mNotify) {
            if (type.equals("text")) {
                sendNotification(mGroupName, message);
            } else if (type.equals("image")) {
                sendNotification(mGroupName, "New image");
            } else if (type.equals("audio")) {
                sendNotification(mGroupName, "New audio");
            }
            mNotify = false;
        }
    }

    private void sendNotification(final String groupname, final String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("TokensGroup");
        Query query = databaseReference.orderByChild("groupname/"+groupname).equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    if (!token.getId().equals(mFirebaseUser.getUid())) {
                        mIdUser = token.getId();
                        Data data = new Data(mFirebaseUser.getUid(), R.mipmap.ic_launcher, "Group " + groupname + ": " + message, "New Message", mIdUser, groupname, mImgGroupUrl);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage() {
        mArrayChats = new ArrayList<>();

        mReference = FirebaseDatabase.getInstance().getReference().child("GroupChatMessage");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mArrayChats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GroupChat groupChat = snapshot.getValue(GroupChat.class);

                    if (groupChat.getGroupname().equals(mGroupName)) {
                        mArrayChats.add(groupChat);
                    }

                    mMessageGroupAdapter = new MessageGroupAdapter(GroupChatActivity.this, mArrayChats, mLongClickMes, mClickMes);
                    mRecyclerViewMessage.setAdapter(mMessageGroupAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        final Query query = databaseReference.orderByChild("groupname").equalTo(mGroupName);

        switch (item.getItemId()) {
            case R.id.updateName:
                final Dialog dialog = new Dialog(GroupChatActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_edit);

                Button btnOk = dialog.findViewById(R.id.btnOK);
                final EditText edtIntroduce = dialog.findViewById(R.id.editTextName);
                TextView textView = dialog.findViewById(R.id.txtTitle);
                textView.setText("Edit name group");
                edtIntroduce.setText(mGroupName);

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String edt = edtIntroduce.getText().toString();
                        if (edt.matches("")) {
                            Toast.makeText(getApplicationContext(), R.string.toast_have_not_enter_name_group, Toast.LENGTH_SHORT).show();
                        } else {
                            Query query2 = databaseReference.orderByChild("groupname").equalTo(edt);
                            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Toast.makeText(getApplicationContext(), "Already exist group name!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupChatMessage");
                                        Query query1 = reference.orderByChild("groupname").equalTo(mGroupName);
                                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().child("groupname").setValue(edt);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().child("groupname").setValue(edt);
                                                    Toast.makeText(getApplicationContext(), R.string.toast_update_name_group_success, Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                dialog.show();
                break;
            case R.id.updateImg:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_UPDATE_IMG);
                break;
            case R.id.addMember:
                Intent intent1 = new Intent(GroupChatActivity.this, AddGroupActivity.class);
                intent1.putExtra("check", 1);
                intent1.putExtra("groupname", mGroupName);
                intent1.putExtra("imgurl", mImgGroupUrl);
                startActivity(intent1);
                break;
            case R.id.leaveGroup:
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Grouplist grouplist = snapshot.getValue(Grouplist.class);
                            if (grouplist.getMember() != null) {
                                for (int i = 0; i < grouplist.getMember().size(); i++) {
                                    if (mFirebaseUser.getUid().equals(grouplist.getMember().get(i))) {
                                        snapshot.getRef().child("member").child(i + "").removeValue();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                finish();
                Toast.makeText(getApplicationContext(), "You have leave group: " + mGroupName, Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_UPDATE_IMG:
                if (resultCode == RESULT_OK && data != null) {
                    mDialog.show();
                    Uri uri = data.getData();

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        mImgProfile.setImageBitmap(bitmap);

                        Calendar calendar = Calendar.getInstance();
                        StorageReference storageReference = mStorageReference.child("imageGroup/").child("imageGroupUpdate" + calendar.getTimeInMillis() + ".png");

                        mImgProfile.setDrawingCacheEnabled(true);
                        mImgProfile.buildDrawingCache();
                        Bitmap newBitmap = mImgProfile.getDrawingCache();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        newBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] dataImg = byteArrayOutputStream.toByteArray();

                        UploadTask uploadTask = storageReference.putBytes(dataImg);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> downloadUri = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                downloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String imgUrl = uri.toString();

                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
                                        Query query = databaseReference.orderByChild("groupname").equalTo(mGroupName);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().child("imgurl").setValue(imgUrl);
                                                    mDialog.dismiss();
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });
                            }
                        });
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            //////////////////////////////////////////////////
            case REQUEST_CODE_SENT_IMG:
                if (resultCode == RESULT_OK && data != null) {
                    mDialog.show();
                    Uri uri = data.getData();

                    Calendar calendar = Calendar.getInstance();
                    StorageReference storageReference = mStorageReference.child("imageGroup/").child("imageSendMessageGroup" + calendar.getTimeInMillis() + ".png");

                    UploadTask uploadTask = storageReference.putFile(uri);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> dowloadUri = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            dowloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imgUrl = uri.toString();

                                    sendMessage(mFirebaseUser.getUid(), imgUrl, "image");
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
                    StorageReference storageReference = mStorageReference.child("imageGroup/").child("imgSendMessageGroupCamera" + calendar.getTimeInMillis() + ".png");

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

                                    sendMessage(mFirebaseUser.getUid(), imgUrl, "image");
                                    mDialog.dismiss();
                                }
                            });
                        }
                    });
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        StorageReference storageReference = mStorageReference.child("audio/").child("audioMesGroup" + calendar.getTimeInMillis() + ".3gp");

        final Uri uri = Uri.fromFile(new File(fileName));
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String audioUrl = uri.toString();
                        sendMessage(mFirebaseUser.getUid(), audioUrl, "audio");
                        mDialog.dismiss();
                    }
                });
            }
        });
    }

    private void initAction() {
        mImgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNotify = true;
                String message = mEdtMessage.getText().toString();
                if (message.matches("")) {
                    Toast.makeText(getApplicationContext(), R.string.toast_enter_the_content_of_the_chat, Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(mFirebaseUser.getUid(), message, "text");
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
                    typing(true);
                } else {
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
                mNotify = true;
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_SENT_IMG);
            }
        });

        mImgBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNotify = true;
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

    private void initData() {
        Intent intent = getIntent();
        mGroupName = intent.getStringExtra("groupName");
        mTxtUsername.setText(mGroupName);
        mImgGroupUrl = intent.getStringExtra("imgGroupUrl");
        if (!mImgGroupUrl.equals("")) {
            Picasso.with(this).load(mImgGroupUrl).into(mImgProfile);
        }

        mReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Account account = snapshot.getValue(Account.class);
                    if (account.getId().equals(mFirebaseUser.getUid())) {
                        mImgUrl = account.getImgUrl();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        readMessage();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group");
        Query query = reference.orderByChild("groupname").equalTo(mGroupName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Grouplist grouplist = snapshot.getValue(Grouplist.class);
                    if (grouplist.getGroupname().equals(mGroupName) && grouplist.getMember() != null) {
                        for (String id : grouplist.getMember()) {
                            if (id != null && !id.equals(mFirebaseUser.getUid())) {
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                                reference1.child(id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Account account = dataSnapshot.getValue(Account.class);
                                        if (account.getStatus().contains("online")) {
                                            mImgOn.setVisibility(View.VISIBLE);
                                            mImgOff.setVisibility(View.GONE);
                                        }

                                        if (account.isTyping()) {
                                            mTxtTyping.setVisibility(View.VISIBLE);
                                        } else {
                                            mTxtTyping.setVisibility(View.INVISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {
        mImgProfile = findViewById(R.id.profileImg);
        mImgOn = findViewById(R.id.imgOn);
        mImgOff = findViewById(R.id.imgOff);
        mBtnDownMessage = findViewById(R.id.btnDownMes);
        mTxtUsername = findViewById(R.id.username);
        mTxtTyping = findViewById(R.id.typing);
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

    private void status(String status) {
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        mReference.updateChildren(hashMap);
    }

    private void typing(boolean typing) {
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
        mReference.child("typing").setValue(typing);
    }

    private void currentUser(String groupname) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentgroup", groupname);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mIdUser != null) currentUser(mIdUser);
//        currentUser(mGroupName);
//        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        currentUser("none");
        typing(false);
//        status("offline");
        if (mMessageGroupAdapter != null) mMessageGroupAdapter.stopPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GroupChatMessage");
        reference.removeEventListener(mValueEventListener);
    }
}

