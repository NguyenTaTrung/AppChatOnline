package gst.trainingcourse.appchatonline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.adapter.MessageAdapter;
import gst.trainingcourse.appchatonline.fragment.APIService;
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

    private CircleImageView mImgProfile;
    private TextView mTxtUsername;
    private Toolbar mToolbar;
    private ImageButton mImgBtnSend;
    private EditText mEdtMessage;
    private MessageAdapter mMessageAdapter;
    private ArrayList<Chat> mArrayChats;
    private RecyclerView mRecyclerViewMessage;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;
    private String mIdUsers;
    private ValueEventListener mValueEventListener;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initView();
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        initData();
        seenMessage(mIdUsers);
    }

    private void initAction() {
        mImgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String message = mEdtMessage.getText().toString();
                if (message.matches("")) {
                    Toast.makeText(getApplicationContext(), "Chưa nhập nội dung chat!", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(mFirebaseUser.getUid(), mIdUsers, message);
                }
                mEdtMessage.setText("");
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        mIdUsers = intent.getStringExtra("idUsers");

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        initAction();

        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mIdUsers);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Account account = dataSnapshot.getValue(Account.class);

                mTxtUsername.setText(account.getUsername());
                Picasso.with(getApplicationContext()).load(account.getImgUrl()).into(mImgProfile);

                readMessage(mFirebaseUser.getUid(), mIdUsers, account.getImgUrl());
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

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerViewMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessage.setLayoutManager(linearLayoutManager);
    }

    private void seenMessage(final String userId) {
        mReference = FirebaseDatabase.getInstance().getReference("Chats");
        mValueEventListener = mReference.addValueEventListener(new ValueEventListener() {
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

    private void sendMessage(String sender, final String receiver, String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        //Tạo nhánh Chat gồm nội dung tin nhắn (message), người gửi (sender), người nhận (receiver)
        databaseReference.child("Chats").push().setValue(hashMap);

        final String msg = message;
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Account account = dataSnapshot.getValue(Account.class);
                if (notify) {
                    sendNotification(receiver, account.getUsername(), msg);
                }
                notify = false;
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
                    Data data = new Data(mFirebaseUser.getUid(), R.mipmap.ic_launcher, username + ": " + message, "New Message", mIdUsers);
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(getApplicationContext(), "Fail!", Toast.LENGTH_LONG).show();
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

                    mMessageAdapter = new MessageAdapter(MessageActivity.this, mArrayChats, imgUrl);
                    mRecyclerViewMessage.setAdapter(mMessageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        status("online");
        currentUser(mIdUsers);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReference.removeEventListener(mValueEventListener);
        status("offline");
        currentUser("none");
    }
}
