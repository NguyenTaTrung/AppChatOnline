package gst.trainingcourse.appchatonline;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.adapter.MessageGroupAdapter;
import gst.trainingcourse.appchatonline.model.Account;
import gst.trainingcourse.appchatonline.model.GroupChat;

public class GroupChatActivity extends AppCompatActivity {

    private CircleImageView mImgProfile;
    private TextView mTxtUsername;
    private Toolbar mToolbar;
    private ImageButton mImgBtnSend;
    private EditText mEdtMessage;
    private String mGroupName, mImgUrl, mImgGroupUrl;
    private MessageGroupAdapter mMessageAdapter;
    private ArrayList<GroupChat> mArrayChats;
    private RecyclerView mRecyclerViewMessage;
    private static final int REQUEST_CODE_IMG = 1;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;
    private StorageReference mStorageReference;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initView();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://appchattest-51e06.appspot.com");
        initData();
        initAction();
        seenMessage();
    }

    private void seenMessage() {
        mReference = FirebaseDatabase.getInstance().getReference("GroupChatMessage");
        mValueEventListener = mReference.addValueEventListener(new ValueEventListener() {
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

    private void sendMessage(final String id, String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        final HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message", message);
        hashMap.put("sender", id);
        hashMap.put("groupname", mGroupName);
        hashMap.put("imgurl", mImgUrl);
        hashMap.put("isseen", false);

        databaseReference.child("GroupChatMessage").push().setValue(hashMap);
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

                    mMessageAdapter = new MessageGroupAdapter(GroupChatActivity.this, mArrayChats);
                    mRecyclerViewMessage.setAdapter(mMessageAdapter);
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
        switch (item.getItemId()) {
            case R.id.updateImg:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_IMG);
                break;
            case R.id.addMember:
                Intent intent1 = new Intent(GroupChatActivity.this, AddGroupActivity.class);
                intent1.putExtra("check", 1);
                intent1.putExtra("groupname", mGroupName);
                intent1.putExtra("imgurl", mImgGroupUrl);
                startActivity(intent1);
                break;
            case R.id.leaveGroup:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMG && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mImgProfile.setImageBitmap(bitmap);

                Calendar calendar = Calendar.getInstance();
                StorageReference storageReference = mStorageReference.child("imageGroupUpdate" + calendar.getTimeInMillis() + ".png");

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
                                String imgUrl = uri.toString();

                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("imgurl", imgUrl);
                                hashMap.put("groupname", mGroupName);

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                databaseReference.child("Group").child(mFirebaseUser.getUid()).child(mGroupName).setValue(hashMap);
                            }
                        });
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initAction() {
        mImgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mEdtMessage.getText().toString();
                if (message.matches("")) {
                    Toast.makeText(getApplicationContext(), "Chưa nhập nội dung chat!", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(mFirebaseUser.getUid(), message);
                }
                mEdtMessage.setText("");
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

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        mReference.child("GroupChatMessage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GroupChat groupChat = dataSnapshot.getValue(GroupChat.class);
                if (groupChat != null)
                    readMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {
        mImgProfile = findViewById(R.id.profileImg);
        mTxtUsername = findViewById(R.id.username);
        mToolbar = findViewById(R.id.toolBar);
        mEdtMessage = findViewById(R.id.editTextMessage);
        mImgBtnSend = findViewById(R.id.btnSend);
        mRecyclerViewMessage = findViewById(R.id.recyclerviewMessage);

        mRecyclerViewMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessage.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReference.removeEventListener(mValueEventListener);
    }
}

