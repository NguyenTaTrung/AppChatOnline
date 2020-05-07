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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.listener.ClickGroupView;
import gst.trainingcourse.appchatonline.model.Account;
import gst.trainingcourse.appchatonline.model.GroupChat;
import gst.trainingcourse.appchatonline.model.Grouplist;

public class GroupFragmentAdapter extends RecyclerView.Adapter<GroupFragmentAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Grouplist> mListGroup;
    private ClickGroupView mClick;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;

    public GroupFragmentAdapter(Context mContext, ArrayList<Grouplist> mListGroup, ClickGroupView mClick) {
        this.mContext = mContext;
        this.mListGroup = mListGroup;
        this.mClick = mClick;
    }

    @NonNull
    @Override
    public GroupFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_fragment_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupFragmentAdapter.ViewHolder holder, int position) {
        Grouplist grouplist = mListGroup.get(position);

        lastMessage(holder.txtLastMessage, position);

        holder.txtGroupName.setText(grouplist.getGroupname());
        if (!grouplist.getImgurl().equals("")) {
            Picasso.with(mContext).load(grouplist.getImgurl()).into(holder.imgGroup);
        }

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference("Users");
        if (grouplist.getMember() != null) {
            for (String id : grouplist.getMember()) {
                if (id != null && !id.equals(mFirebaseUser.getUid())) {
                    mReference.child(id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Account account = dataSnapshot.getValue(Account.class);

                            if (account.getStatus().contains("online")) {
                                holder.imgOn.setVisibility(View.VISIBLE);
                                holder.imgOff.setVisibility(View.GONE);
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

    @Override
    public int getItemCount() {
        return mListGroup.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtGroupName, txtLastMessage;
        public CircleImageView imgGroup, imgOn, imgOff;
        public RelativeLayout itemGroup;

        View.OnClickListener clickView = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClick.onClickView(getAdapterPosition());
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);

            txtGroupName = itemView.findViewById(R.id.txtGroupName);
            txtLastMessage = itemView.findViewById(R.id.lastMessage);
            imgGroup = itemView.findViewById(R.id.imgGroup);
            itemGroup = itemView.findViewById(R.id.itemViewGroup);
            imgOn = itemView.findViewById(R.id.imgOn);
            imgOff = itemView.findViewById(R.id.imgOff);

            itemGroup.setOnClickListener(clickView);
        }
    }

    private void lastMessage(final TextView textView, final int position) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("GroupChatMessage");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GroupChat groupChat = snapshot.getValue(GroupChat.class);

                    if (groupChat.getGroupname().equals(mListGroup.get(position).getGroupname())) {
                        if (groupChat.getType().equals("text")) {
                            textView.setText(groupChat.getMessage());
                        } else if (groupChat.getType().equals("image")) {
                            textView.setText("New image");
                        } else if (groupChat.getType().equals("audio")) {
                            textView.setText("New audio");
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

