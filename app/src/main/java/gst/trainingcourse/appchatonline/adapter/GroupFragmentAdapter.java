package gst.trainingcourse.appchatonline.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import gst.trainingcourse.appchatonline.model.GroupChat;

public class GroupFragmentAdapter extends RecyclerView.Adapter<GroupFragmentAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> mListGroup;
    private ClickGroupView mClick;
    private String mImgUrl;

    public GroupFragmentAdapter(Context mContext, ArrayList<String> mListGroup, ClickGroupView mClick, String mImgUrl) {
        this.mContext = mContext;
        this.mListGroup = mListGroup;
        this.mClick = mClick;
        this.mImgUrl = mImgUrl;
    }

    @NonNull
    @Override
    public GroupFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_fragment_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupFragmentAdapter.ViewHolder holder, int position) {
        String s = mListGroup.get(position);

        lastMessage(holder.txtLastMessage, position);

        holder.txtGroupName.setText(s);
        if (!mImgUrl.equals("")) {
            Picasso.with(mContext).load(mImgUrl).into(holder.imgGroup);
        }
    }

    @Override
    public int getItemCount() {
        return mListGroup.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtGroupName, txtLastMessage;
        public CircleImageView imgGroup;
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

                    if (groupChat.getGroupname().equals(mListGroup.get(position))) {
                        textView.setText(groupChat.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

