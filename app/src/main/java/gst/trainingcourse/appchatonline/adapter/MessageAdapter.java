package gst.trainingcourse.appchatonline.adapter;

import android.content.Context;
import android.media.MediaPlayer;
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
import gst.trainingcourse.appchatonline.model.Chat;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;

    private Context mContext;
    private ArrayList<Chat> mArrayList;
    private String imgUrl;
    private LongClickMessage longClickMes;
    private ClickMessage clickMessage;

    private FirebaseUser mFirebaseUser;

    private MediaPlayer mediaPlayer = null;
    private ViewHolder playingHolder;
    private int currentPlayingPosition, durationAudio = 0;
    private SeekBarUpdater seekBarUpdater;

    public MessageAdapter(Context mContext, ArrayList<Chat> mArrayList, LongClickMessage longClickMes, ClickMessage clickMessage, String imgUrl) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
        this.longClickMes = longClickMes;
        this.clickMessage = clickMessage;
        this.imgUrl = imgUrl;
        this.seekBarUpdater = new SeekBarUpdater();
        this.currentPlayingPosition = -1;
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
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {
        Chat chat = mArrayList.get(position);

        if (chat.getType().equals("text")) {
            holder.imgSend.setVisibility(View.GONE);
            holder.rlAudio.setVisibility(View.GONE);
            holder.showMessage.setText(chat.getMessage());
        } else if (chat.getType().equals("image")) {
            Picasso.with(mContext).load(chat.getMessage()).into(holder.imgSend);
            holder.showMessage.setVisibility(View.GONE);
            holder.rlAudio.setVisibility(View.GONE);
        } else if (chat.getType().equals("audio")) {
            holder.showMessage.setVisibility(View.GONE);
            holder.imgSend.setVisibility(View.GONE);

            if (position == currentPlayingPosition) {
                playingHolder = holder;
                updatePlayingView();
            } else {
                updateNonPlayingView(holder);
            }
        }
        Picasso.with(mContext).load(imgUrl).into(holder.imgProfile);


        if (position == mArrayList.size() - 1) {
            if (chat.isIsseen()) {
                holder.seen.setText("Đã xem");
                holder.seen.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_24dp, 0);
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
        }
    }

    private void updateNonPlayingView(ViewHolder viewHolder) {
        viewHolder.seekBar.removeCallbacks(seekBarUpdater);
        viewHolder.seekBar.setEnabled(false);
        viewHolder.seekBar.setProgress(0);
        viewHolder.play.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        viewHolder.duration.setText("");
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
            playingHolder.play.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
        } else {
            playingHolder.seekBar.removeCallbacks(seekBarUpdater);
            playingHolder.play.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        }
    }

    public void stopPlayer() {
        if (null != mediaPlayer) {
            releaseMediaPlayer();
        }
    }

    private class SeekBarUpdater implements Runnable {
        @Override
        public void run() {
            if (null != playingHolder) {
                durationAudio -= 1000;
                SimpleDateFormat sp = new SimpleDateFormat("m:ss");
                playingHolder.duration.setText(sp.format(durationAudio));
                playingHolder.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                playingHolder.seekBar.postDelayed(this, 1000);
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
        private ImageButton play;
        private SeekBar seekBar;

        View.OnLongClickListener longClickMessage = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClickMes.onLongClickMes(getAdapterPosition());
                return true;
            }
        };

        View.OnClickListener clickMes = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickMessage.onClickMes(getAdapterPosition());
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.showMessage);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            seen = itemView.findViewById(R.id.txtSeen);
            duration = itemView.findViewById(R.id.txtDuration);
            imgSend = itemView.findViewById(R.id.imgSend);
            play = itemView.findViewById(R.id.btnPlay);
            rlMesRight = itemView.findViewById(R.id.rlMesRight);
            rlMesLeft = itemView.findViewById(R.id.rlMesLeft);
            rlAudio = itemView.findViewById(R.id.rlAudio);
            seekBar = itemView.findViewById(R.id.skAudio);

            seekBar.setOnSeekBarChangeListener(this);
            play.setOnClickListener(this);

            if (rlMesRight != null) {
                rlMesRight.setOnLongClickListener(longClickMessage);
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
            } else {
                currentPlayingPosition = getAdapterPosition();
                if (mediaPlayer != null) {
                    if (null != playingHolder) {
                        updateNonPlayingView(playingHolder);
                    }
                    mediaPlayer.release();
                }
                playingHolder = this;
                startMediaPlayer(currentPlayingPosition);
            }
            updatePlayingView();
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
        if (null != playingHolder) {
            updateNonPlayingView(playingHolder);
        }
        mediaPlayer.release();
        mediaPlayer = null;
        currentPlayingPosition = -1;
    }
}
