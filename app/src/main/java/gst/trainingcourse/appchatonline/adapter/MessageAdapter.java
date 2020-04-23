package gst.trainingcourse.appchatonline.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.model.Chat;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;

    private Context mContext;
    private ArrayList<Chat> mArrayList;
    private String imgUrl;

    private FirebaseUser mFirebaseUser;

    public MessageAdapter(Context mContext, ArrayList<Chat> mArrayList, String imgUrl) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
        this.imgUrl = imgUrl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_message_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_message_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mArrayList.get(position);

        holder.showMessage.setText(chat.getMessage());
        Picasso.with(mContext).load(imgUrl).into(holder.imgProfile);

        if (position == mArrayList.size() - 1) {
            if (chat.isIsseen()) {
                holder.seen.setText("Đã xem");
                holder.seen.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_24dp, 0);
            } else {
                holder.seen.setText("Đã gửi");
            }
        } else {
            holder.seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView showMessage, seen;
        public CircleImageView imgProfile;

        public ViewHolder(View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.showMessage);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            seen = itemView.findViewById(R.id.txtSeen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mArrayList.get(position).getSender().equals(mFirebaseUser.getUid())) {
            return MSG_RIGHT;
        } else {
            return MSG_LEFT;
        }
    }
}
