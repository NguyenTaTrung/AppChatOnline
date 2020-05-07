package gst.trainingcourse.appchatonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.adapter.AddGroupAdapter;
import gst.trainingcourse.appchatonline.fragments.ChatsFragment;
import gst.trainingcourse.appchatonline.listener.ClickCheckInGroup;
import gst.trainingcourse.appchatonline.model.Account;
import gst.trainingcourse.appchatonline.model.Grouplist;

public class AddGroupActivity extends AppCompatActivity {

    private RecyclerView mRecyclerviewGroup;
    private Toolbar mToolBar;
    private EditText mEdtGroupName;

    private String mGroupName, mNameDisable, mImgUrl;
    private int mStatus;

    private ArrayList<String> mListID = new ArrayList<>();
    private ArrayList<Account> mListInGroup = new ArrayList<>();
    private ArrayList<Account> mListInGroupNew = new ArrayList<>();

    private Intent mIntent;
    private AddGroupAdapter mAddGroupAdapter;


    private ClickCheckInGroup clickCheckInGroup = new ClickCheckInGroup() {
        @Override
        public void onClickCheck(Account account) {
            mListInGroupNew.add(account);
        }

        @Override
        public void onClickUnCheck(Account account) {
            mListInGroupNew.remove(account);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        initView();
        initData();
        setAdapter();
    }

    private void initView() {
        mRecyclerviewGroup = findViewById(R.id.recyclerViewGroup);
        mToolBar = findViewById(R.id.toolBar);
        mEdtGroupName = findViewById(R.id.editTextNameGroup);

        setSupportActionBar(mToolBar);
        mToolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getSupportActionBar().setTitle("Add group");
    }

    private void initData() {
        mListInGroup.addAll(ChatsFragment.mListChat);
        mIntent = getIntent();
        mStatus = mIntent.getIntExtra("check", 0);
    }

    private void setAdapter() {
        mAddGroupAdapter = new AddGroupAdapter(mListInGroup, this, clickCheckInGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerviewGroup.setLayoutManager(linearLayoutManager);
        mRecyclerviewGroup.setAdapter(mAddGroupAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mStatus == 0) {
            getMenuInflater().inflate(R.menu.menu_add_group, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_add_member_group, menu);
            mNameDisable = mIntent.getStringExtra("groupname");
            mImgUrl = mIntent.getStringExtra("imgurl");
            mEdtGroupName.setText(mNameDisable);
            mEdtGroupName.setEnabled(false);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group");
            Query query = reference.orderByChild("groupname").equalTo(mNameDisable);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Grouplist grouplist = snapshot.getValue(Grouplist.class);

                        if (grouplist.getMember() != null) {
                            for (String id : grouplist.getMember()) {
                                for (int i = 0; i < mListInGroup.size(); i++) {
                                    if (mListInGroup.get(i).getId().equals(id)) {
                                        mListInGroup.remove(i);
                                    }
                                }
                            }
                            setAdapter();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create:
                mGroupName = mEdtGroupName.getText().toString();
                if (mGroupName.matches("")) {
                    Toast.makeText(getApplicationContext(), R.string.toast_no_information_entered, Toast.LENGTH_SHORT).show();
                } else if (mListInGroupNew.size() <= 1) {
                    Toast.makeText(getApplicationContext(), R.string.toast_select_more_friend, Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                    for (Account account : mListInGroupNew) {
                        mListID.add(account.getId());
                    }
                    mListID.add(firebaseUser.getUid());

                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
                    Query query = databaseReference.orderByChild("groupname").equalTo(mGroupName);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(getApplicationContext(), "Already exist " + mGroupName, Toast.LENGTH_SHORT).show();
                            } else {
                                final HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("groupname", mGroupName);
                                hashMap.put("imgurl", "https://firebasestorage.googleapis.com/v0/b/appchattest-51e06.appspot.com/o/imageUser%2FimageUser1588231082299.png?alt=media&token=866becac-f04e-44e6-b24b-2ad8497c4d9f");
                                hashMap.put("member", mListID);
                                databaseReference.push().setValue(hashMap);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                return true;

            //////////////////////////////////////
            case R.id.add:
                if (mListInGroupNew.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.toast_have_not_select_friend, Toast.LENGTH_SHORT).show();
                } else {
                    for (Account account : mListInGroupNew) {
                        mListID.add(account.getId());
                    }
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
                    Query query = databaseReference.orderByChild("groupname").equalTo(mNameDisable);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Grouplist grouplist = snapshot.getValue(Grouplist.class);
                                if (grouplist.getMember() != null) {
                                    for (String id : grouplist.getMember()) {
                                        mListID.add(id);
                                    }
                                    snapshot.getRef().child("member").setValue(mListID);
                                    Toast.makeText(getApplicationContext(), R.string.toast_add_succesfully, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                return true;
        }
        return false;
    }
}
