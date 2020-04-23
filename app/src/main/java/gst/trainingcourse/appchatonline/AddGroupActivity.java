package gst.trainingcourse.appchatonline;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.appchatonline.adapter.AddGroupAdapter;
import gst.trainingcourse.appchatonline.fragment.ChatsFragment;
import gst.trainingcourse.appchatonline.listener.ClickCheckInGroup;
import gst.trainingcourse.appchatonline.model.Account;

public class AddGroupActivity extends AppCompatActivity {

    private RecyclerView mRecyclerviewGroup;
    private AddGroupAdapter mAddGroupAdapter;
    private Toolbar mToolBar;
    private EditText mEdtGroupName;
    private String mGroupName, mNameDisable, mImgUrl;
    private boolean mCheck = false;
    private int mStatus;
    private Intent mIntent;
    private ArrayList<String> mListID = new ArrayList<>();
    private ArrayList<Account> mListInGroup = new ArrayList<>();
    private ArrayList<Account> mListInGroupNew = new ArrayList<>();
    private ClickCheckInGroup clickCheckInGroup = new ClickCheckInGroup() {
        @Override
        public void onClickCheck(Account account) {
            mListInGroupNew.add(account);
            mCheck = true;
        }

        @Override
        public void onClickUnCheck(Account account) {
            mListInGroupNew.remove(account);
            if (mListInGroupNew.isEmpty()) mCheck = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        mListInGroup.addAll(ChatsFragment.mListChat);

        mRecyclerviewGroup = findViewById(R.id.recyclerViewGroup);
        mToolBar = findViewById(R.id.toolBar);
        mEdtGroupName = findViewById(R.id.editTextNameGroup);

        mIntent = getIntent();
        mStatus = mIntent.getIntExtra("check", 0);

        setSupportActionBar(mToolBar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        ab.setDisplayHomeAsUpEnabled(true);

        setAdapter();
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
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create:
                mGroupName = mEdtGroupName.getText().toString();
                if (mGroupName.matches("")) {
                    Toast.makeText(getApplicationContext(), "No information entered", Toast.LENGTH_SHORT).show();
                } else if (!mCheck) {
                    Toast.makeText(getApplicationContext(), "You haven't select friends", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
                    final HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("groupname", mGroupName);
                    hashMap.put("imgurl", "");
                    for (Account account : mListInGroupNew) {
                        mListID.add(account.getId());
                    }
                    databaseReference.child(firebaseUser.getUid()).child(mGroupName).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                for (int i = 0; i < mListID.size(); i++) {
                                    databaseReference.child(mListID.get(i)).child(mGroupName).setValue(hashMap);
                                }
                                finish();
                            }
                        }
                    });
                }
                return true;
            case R.id.add:
                if (!mCheck) {
                    Toast.makeText(getApplicationContext(), "You haven't select friends", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
                    final HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("groupname", mGroupName);
                    if (mImgUrl.equals("")) {
                        hashMap.put("imgurl", "");
                    } else {
                        hashMap.put("imgurl", mImgUrl);
                    }
                    for (Account account : mListInGroupNew) {
                        mListID.add(account.getId());
                    }

                }
                return true;
        }
        return false;
    }
}
