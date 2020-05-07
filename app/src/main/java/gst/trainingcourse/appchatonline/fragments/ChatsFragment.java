package gst.trainingcourse.appchatonline.fragments;

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
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.appchatonline.activities.MessageActivity;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.adapter.ControlAccountAdapter;
import gst.trainingcourse.appchatonline.listener.ClickChatUser;
import gst.trainingcourse.appchatonline.model.Account;
import gst.trainingcourse.appchatonline.model.Chatlist;
import gst.trainingcourse.appchatonline.notification.Token;

public class ChatsFragment extends Fragment {

    private RecyclerView mRecyclerviewUserChat;
    public static ArrayList<Account> mListChat;
    private ArrayList<Chatlist> mListIdUserChat;
    private ControlAccountAdapter mControlAccountAdapter;
    private ClickChatUser mClickUserChat = new ClickChatUser() {
        @Override
        public void onClickUser(int position) {
            Account account = mListChat.get(position);
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            intent.putExtra("idUsers", account.getId());
            startActivity(intent);
        }
    };

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerviewUserChat = view.findViewById(R.id.recyclerViewUserChat);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mListIdUserChat = new ArrayList<>();

        mReference = FirebaseDatabase.getInstance().getReference("Chatlist").child(mFirebaseUser.getUid());
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListIdUserChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    mListIdUserChat.add(chatlist);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(mFirebaseUser.getUid()).setValue(token1);
    }

    private void chatList() {
        mListChat = new ArrayList<>();

        mReference = FirebaseDatabase.getInstance().getReference("Users");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListChat.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Account account = snapshot.getValue(Account.class);

                    for (Chatlist chatlist : mListIdUserChat) {
                        if (account.getId().equals(chatlist.getId())) {
                            mListChat.add(account);
                        }
                    }
                }

                mControlAccountAdapter = new ControlAccountAdapter(getContext(), mListChat, mClickUserChat, true);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerviewUserChat.setLayoutManager(linearLayoutManager);
                mRecyclerviewUserChat.setAdapter(mControlAccountAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
