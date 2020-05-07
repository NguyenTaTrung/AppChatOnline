package gst.trainingcourse.appchatonline.fragments;


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
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.appchatonline.activities.AddGroupActivity;
import gst.trainingcourse.appchatonline.activities.GroupChatActivity;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.adapter.GroupFragmentAdapter;
import gst.trainingcourse.appchatonline.listener.ClickGroupView;
import gst.trainingcourse.appchatonline.model.Grouplist;
import gst.trainingcourse.appchatonline.notification.Token;

public class GroupFragment extends Fragment {

    private RecyclerView mRecyclerViewGroup;
    private FloatingActionButton mBtnAdd;
    private GroupFragmentAdapter mGroupFragmentAdapter;
    public static ArrayList<Grouplist> mList = new ArrayList<>();
    private ArrayList<String> mListGroupName = new ArrayList<>();
    private ClickGroupView mClick = new ClickGroupView() {
        @Override
        public void onClickView(int position) {
            Grouplist grouplist = mList.get(position);
            Intent intent = new Intent(getActivity(), GroupChatActivity.class);
            intent.putExtra("groupName", grouplist.getGroupname());
            intent.putExtra("imgGroupUrl", grouplist.getImgurl());
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

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mList.clear();
                mListGroupName.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Grouplist grouplist = snapshot.getValue(Grouplist.class);
                    if (grouplist.getMember() != null) {
                        for (String id : grouplist.getMember()) {
                            if (id != null && id.equals(firebaseUser.getUid())) {
                                mList.add(grouplist);
                                mListGroupName.add(grouplist.getGroupname());
                            }
                        }
                    }
                }
                mGroupFragmentAdapter = new GroupFragmentAdapter(getContext(), mList, mClick);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerViewGroup.setLayoutManager(linearLayoutManager);
                mRecyclerViewGroup.setAdapter(mGroupFragmentAdapter);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TokensGroup");
                HashMap<String, Boolean> hashMap = new HashMap<>();
                for (String id : mListGroupName) {
                    if (id != null) {
                        hashMap.put(id, true);
                    }
                }
                Token token = new Token(FirebaseInstanceId.getInstance().getToken(), hashMap, FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
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
