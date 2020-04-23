package gst.trainingcourse.appchatonline.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.listener.ClickCheckInGroup;
import gst.trainingcourse.appchatonline.model.Account;

public class AddGroupAdapter extends RecyclerView.Adapter<AddGroupAdapter.ViewHolder> {

    private ArrayList<Account> mListAccount;
    private Context mContext;
    private ClickCheckInGroup mClick;

    public AddGroupAdapter(ArrayList<Account> mListAccount, Context mContext, ClickCheckInGroup mClick) {
        this.mListAccount = mListAccount;
        this.mContext = mContext;
        this.mClick = mClick;
    }

    @NonNull
    @Override
    public AddGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddGroupAdapter.ViewHolder holder, int position) {
        final Account account = mListAccount.get(position);

        holder.txtUsername.setText(account.getUsername());
        Picasso.with(mContext).load(account.getImgUrl()).into(holder.imgProfile);

        CompoundButton.OnCheckedChangeListener check = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mClick.onClickCheck(account);
                } else {
                    mClick.onClickUnCheck(account);
                }
            }
        };

        holder.checkBox.setOnCheckedChangeListener(check);
    }

    @Override
    public int getItemCount() {
        return mListAccount.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtUsername;
        public CircleImageView imgProfile;
        public CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);

            txtUsername = itemView.findViewById(R.id.username);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            checkBox = itemView.findViewById(R.id.checkBox);

        }
    }
}
