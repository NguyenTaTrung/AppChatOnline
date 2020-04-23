package gst.trainingcourse.appchatonline.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.appchatonline.AddGroupActivity;
import gst.trainingcourse.appchatonline.GroupChatActivity;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.adapter.GroupFragmentAdapter;
import gst.trainingcourse.appchatonline.listener.ClickGroupView;

public class GroupFragment extends Fragment {

    private RecyclerView mRecyclerViewGroup;
    private FloatingActionButton mBtnAdd;
    private GroupFragmentAdapter mGroupFragmentAdapter;
    private ArrayList<String> mListGroup = new ArrayList<>();
    private String mImgUrl;
    private ClickGroupView mClick = new ClickGroupView() {
        @Override
        public void onClickView(int position) {
            Intent intent = new Intent(getActivity(), GroupChatActivity.class);
            intent.putExtra("groupName", mListGroup.get(position));
            intent.putExtra("imgGroupUrl", mImgUrl);
            startActivity(intent);
        }
    };

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerViewGroup = view.findViewById(R.id.recyclerViewGroup);
        mBtnAdd = view.findViewById(R.id.btnAdd);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListGroup.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String groupName = snapshot.child("groupname").getValue(String.class);
                    mImgUrl = snapshot.child("imgurl").getValue(String.class);
                    mListGroup.add(groupName);
                }
                mGroupFragmentAdapter = new GroupFragmentAdapter(getContext(), mListGroup, mClick, mImgUrl);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerViewGroup.setLayoutManager(linearLayoutManager);
                mRecyclerViewGroup.setAdapter(mGroupFragmentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddGroupActivity.class);
                intent.putExtra("check", 0);
                startActivity(intent);
            }
        });
    }
}
