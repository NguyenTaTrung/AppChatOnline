package gst.trainingcourse.appchatonline.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.appchatonline.ProfileActivity;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.adapter.ControlAccountAdapter;
import gst.trainingcourse.appchatonline.listener.ClickChatUser;
import gst.trainingcourse.appchatonline.model.Account;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.widget.LinearLayoutManager;

public class UsersFragment extends Fragment {

    private RecyclerView mRecyclerViewAllAccounts;
    private SearchView mSearchUsers;
    private ControlAccountAdapter mControlAccountAdapter;
    private ArrayList<Account> mAccountsList;
    private ClickChatUser mChatUsers = new ClickChatUser() {
        @Override
        public void onClickUser(int position) {
            Account account = mAccountsList.get(position);
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra("id", account.getId());
            intent.putExtra("username", account.getUsername());
            intent.putExtra("imgUrl", account.getImgUrl());
            intent.putExtra("introduce", account.getIntroduce());
            startActivity(intent);
        }
    };

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        readAllUsers();
        initAction();
    }

    private void initAction() {
        mSearchUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mControlAccountAdapter.searchUser(s);
                return false;
            }
        });
    }

    private void initView(View view) {
        mRecyclerViewAllAccounts = view.findViewById(R.id.recyclerViewAllUsers);
        mSearchUsers = view.findViewById(R.id.searchUsers);
        mRecyclerViewAllAccounts.setHasFixedSize(true);
    }

    private void setupAdapter() {
        mControlAccountAdapter = new ControlAccountAdapter(getContext(), mAccountsList, mChatUsers, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewAllAccounts.setLayoutManager(linearLayoutManager);
        mRecyclerViewAllAccounts.setAdapter(mControlAccountAdapter);
    }

    private void readAllUsers() {
        mAccountsList = new ArrayList<>();

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mAccountsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Account account = snapshot.getValue(Account.class);
                    assert account != null;
                    assert firebaseUser != null;
                    if (!account.getId().equals(firebaseUser.getUid())) {
                        mAccountsList.add(account);
                    }
                }
                setupAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
