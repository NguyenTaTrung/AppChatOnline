package gst.trainingcourse.appchatonline.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.listener.ClickChatUser;
import gst.trainingcourse.appchatonline.model.Account;
import gst.trainingcourse.appchatonline.model.Chat;

public class ControlAccountAdapter extends RecyclerView.Adapter<ControlAccountAdapter.Viewholder> {

    private List<Account> mAccountArrayList;
    private List<Account> mList;
    private Context mContext;
    private ClickChatUser mClickChatUser;
    private Boolean mIsChat;

    public ControlAccountAdapter(Context context, List<Account> mAccountArrayList, ClickChatUser clickChatUser, Boolean b) {
        this.mAccountArrayList = mAccountArrayList;
        this.mContext = context;
        this.mClickChatUser = clickChatUser;
        this.mIsChat = b;

        mList = new ArrayList<>();
        mList.addAll(mAccountArrayList);
    }

    @NonNull
    @Override
    public ControlAccountAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_account, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ControlAccountAdapter.Viewholder holder, int position) {
        Account account = mAccountArrayList.get(position);

        if (mIsChat) {
            lastMessage(account.getId(), holder.txtLastMessage);
        } else {
            holder.txtLastMessage.setVisibility(View.GONE);
        }

        if (mIsChat) {
            //cap nhap bi loi~
            if (account.getStatus() != null) {
                if (account.getStatus().equals("online")) {
                    holder.imgOn.setVisibility(View.VISIBLE);
                    holder.imgOff.setVisibility(View.GONE);
                } else {
                    holder.imgOn.setVisibility(View.GONE);
                    holder.imgOff.setVisibility(View.VISIBLE);
                }
            }
        } else {
            holder.imgOn.setVisibility(View.GONE);
            holder.imgOff.setVisibility(View.GONE);
        }

        holder.txtUsername.setText(account.getUsername());
        Picasso.with(mContext).load(account.getImgUrl()).into(holder.imgProfile);
    }

    @Override
    public int getItemCount() {
        return mAccountArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        public TextView txtUsername, txtLastMessage;
        public CircleImageView imgProfile, imgOn, imgOff;
        public RelativeLayout rlUsers;

        View.OnClickListener onClickUsers = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickChatUser.onClickUser(getAdapterPosition());
            }
        };

        public Viewholder(View itemView) {
            super(itemView);

            rlUsers = itemView.findViewById(R.id.rlUsers);
            txtUsername = itemView.findViewById(R.id.username);
            txtLastMessage = itemView.findViewById(R.id.lastMessage);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgOn = itemView.findViewById(R.id.imgOn);
            imgOff = itemView.findViewById(R.id.imgOff);

            rlUsers.setOnClickListener(onClickUsers);
        }
    }

    private void lastMessage(final String userId, final TextView last) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        if (firebaseUser != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);

                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) ||
                                chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
                            if (chat.getType().equals("text")) {
                                last.setText(chat.getMessage());
                            } else if (chat.getType().equals("image")){
                                last.setText("New Image");
                            } else if (chat.getType().equals("audio")) {
                                last.setText("New Audio");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void searchUser(String s){
        s = s.toLowerCase(Locale.getDefault());
        mAccountArrayList.clear();
        if (s.length() == 0) {
            mAccountArrayList.addAll(mList);
        } else {
            for (Account account : mList) {
                if (account.getUsername().toLowerCase(Locale.getDefault()).contains(s)) {
                    mAccountArrayList.add(account);
                }
            }
        }
        notifyDataSetChanged();
    }
}
