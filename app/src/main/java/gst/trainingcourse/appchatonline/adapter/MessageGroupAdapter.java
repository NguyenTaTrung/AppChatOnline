package gst.trainingcourse.appchatonline.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.appchatonline.R;
import gst.trainingcourse.appchatonline.listener.ClickMessage;
import gst.trainingcourse.appchatonline.listener.LongClickMessage;
import gst.trainingcourse.appchatonline.model.GroupChat;

public class MessageGroupAdapter extends RecyclerView.Adapter<MessageGroupAdapter.ViewHolder> {

    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;

    private Context mContext;
    private ArrayList<GroupChat> mArrayList;
    private LongClickMessage mLongClickMes;
    private ClickMessage mClickMessage;
    private MediaPlayer mediaPlayer = null;
    private int currentPlayingPosition, durationAudio = 0;
    private SeekBarUpdater seekBarUpdater;
    private ViewHolder playingHolder;

    private FirebaseUser mFirebaseUser;

    public MessageGroupAdapter(Context mContext, ArrayList<GroupChat> mArrayList, LongClickMessage mLongClickMes, ClickMessage mClickMessage) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
        this.mLongClickMes = mLongClickMes;
        this.mClickMessage = mClickMessage;
        this.currentPlayingPosition = -1;
        seekBarUpdater = new SeekBarUpdater();
    }

    @NonNull
    @Override
    public MessageGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_message_right, parent, false);
            return new MessageGroupAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_message_left, parent, false);
            return new MessageGroupAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageGroupAdapter.ViewHolder holder, int position) {
        GroupChat groupChat = mArrayList.get(position);

        if (groupChat.getType().equals("text")) {
            holder.showMessage.setText(groupChat.getMessage());
            holder.rlAudio.setVisibility(View.GONE);
            holder.imgSend.setVisibility(View.GONE);
        } else if (groupChat.getType().equals("image")) {
            holder.showMessage.setVisibility(View.GONE);
            holder.rlAudio.setVisibility(View.GONE);
            Picasso.with(mContext).load(groupChat.getMessage()).into(holder.imgSend);
        } else if (groupChat.getType().equals("audio")) {
            holder.showMessage.setVisibility(View.GONE);
            holder.imgSend.setVisibility(View.GONE);
            Log.d("TAG", "onBindViewHolder: 1");
            if (position == currentPlayingPosition) {
                playingHolder = holder;
                updatePlayingView();
                Log.d("TAG", "onBindViewHolder: 2");
            } else {
                updateNonPlayingView(holder);
                Log.d("TAG", "onBindViewHolder: 3");
            }
        }
        Picasso.with(mContext).load(groupChat.getImgurl()).into(holder.imgProfile);

        if (position == mArrayList.size() - 1) {
            if (groupChat.isIsseen()) {
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
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (currentPlayingPosition == holder.getAdapterPosition()) {
            updateNonPlayingView(playingHolder);
            playingHolder = null;
            Log.d("TAG", "onViewRecycled: ");
        }
    }

    private void updateNonPlayingView(MessageGroupAdapter.ViewHolder viewHolder) {
        viewHolder.seekBar.removeCallbacks(seekBarUpdater);
        viewHolder.seekBar.setEnabled(false);
        viewHolder.seekBar.setProgress(0);
        viewHolder.playAndPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        viewHolder.duration.setText("");
        Log.d("TAG", "updateNonPlayingView: ");
    }

    private void updatePlayingView() {
        durationAudio = mediaPlayer.getDuration();
        playingHolder.seekBar.setMax(durationAudio);
        playingHolder.seekBar.setProgress(mediaPlayer.getCurrentPosition());
        playingHolder.seekBar.setEnabled(true);
        SimpleDateFormat sp = new SimpleDateFormat("m:ss");
        playingHolder.duration.setText(sp.format(durationAudio));
        if (mediaPlayer.isPlaying()) {
            playingHolder.seekBar.postDelayed(seekBarUpdater, 1000);
            playingHolder.playAndPause.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            Log.d("TAG", "updatePlayingView: 2");
        } else {
            Log.d("TAG", "updatePlayingView: 3");
            playingHolder.seekBar.removeCallbacks(seekBarUpdater);
            playingHolder.playAndPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        }
        Log.d("TAG", "updatePlayingView: 1");
    }

    public void stopPlayer() {
        if (mediaPlayer != null) {
            releaseMediaPlayer();
        }
    }

    public class SeekBarUpdater implements Runnable {

        @Override
        public void run() {
            if (playingHolder != null) {
                durationAudio -= 1000;
                SimpleDateFormat sp = new SimpleDateFormat("m:ss");
                playingHolder.duration.setText(sp.format(durationAudio));
                playingHolder.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                playingHolder.seekBar.postDelayed(this, 1000);
                Log.d("TAG", "run: ");
            }
        }
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
        private TextView showMessage, seen, duration;
        private CircleImageView imgProfile;
        private ImageView imgSend;
        private RelativeLayout rlMesRight, rlMesLeft, rlAudio;
        private ImageButton playAndPause;
        private SeekBar seekBar;

        View.OnLongClickListener longClickMes = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mLongClickMes.onLongClickMes(getAdapterPosition());
                return true;
            }
        };

        View.OnClickListener clickMes = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickMessage.onClickMes(getAdapterPosition());
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.showMessage);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            seen = itemView.findViewById(R.id.txtSeen);
            duration = itemView.findViewById(R.id.txtDuration);
            imgSend = itemView.findViewById(R.id.imgSend);
            playAndPause = itemView.findViewById(R.id.btnPlay);
            rlMesRight = itemView.findViewById(R.id.rlMesRight);
            rlMesLeft = itemView.findViewById(R.id.rlMesLeft);
            rlAudio = itemView.findViewById(R.id.rlAudio);
            seekBar = itemView.findViewById(R.id.skAudio);

            playAndPause.setOnClickListener(this);
            seekBar.setOnSeekBarChangeListener(this);

            if (rlMesRight != null) {
                rlMesRight.setOnLongClickListener(longClickMes);
                rlMesRight.setOnClickListener(clickMes);
            }

            if (rlMesLeft != null) rlMesLeft.setOnClickListener(clickMes);
        }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() == currentPlayingPosition) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
                Log.d("TAG", "onClick: 1");
            } else {
                currentPlayingPosition = getAdapterPosition();
                if (mediaPlayer != null) {
                    if (playingHolder != null) {
                        Log.d("TAG", "onClick: 3");
                        updateNonPlayingView(playingHolder);
                    }
                    mediaPlayer.release();
                }
                playingHolder = this;
                startMediaPlayer(currentPlayingPosition);
                Log.d("TAG", "onClick: 2");
            }
            updatePlayingView();
            Log.d("TAG", "onClick: 0");
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b) {
                mediaPlayer.seekTo(i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

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

    private void startMediaPlayer(int position) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mArrayList.get(position).getMessage());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                releaseMediaPlayer();
            }
        });
    }

    private void releaseMediaPlayer() {
        if (playingHolder != null) {
            updateNonPlayingView(playingHolder);
            Log.d("TAG", "releaseMediaPlayer: ");
        }
        mediaPlayer.release();
        mediaPlayer = null;
        currentPlayingPosition = -1;
    }
}
