package com.amstatbot.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.amstatbot.Models.Chat;
import com.amstatbot.NewsActivity;
import com.amstatbot.R;
import com.eyalbira.loadingdots.LoadingDots;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Chat> chats;
    private static int TYPE_STOCK = 1;
    private static int TYPE_MESSAGE = 2;
    private static int TYPE_LOADING = 3;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private DatabaseReference mChatDatabase, mFavouritesDatabase, mFollowingDatabase;

    public ChatAdapter(Context context, List<Chat>  chat) {
        mContext = context;
        chats = chat;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_STOCK) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.single_chat_message_graph, parent, false);
            return new StockHolder(view);

        }else if(viewType == TYPE_LOADING){
            View view = LayoutInflater.from(mContext).inflate(R.layout.single_chat_loading, parent, false);
            return new LoadingHolder(view);
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.single_chat_message, parent, false);
            return new MessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("chats");
        mChatDatabase.keepSynced(true);

        mFavouritesDatabase = FirebaseDatabase.getInstance().getReference().child("Favourites");
        mFavouritesDatabase.keepSynced(true);

        mFollowingDatabase = FirebaseDatabase.getInstance().getReference().child("Following").child(mCurrentUserId);
        mFollowingDatabase.keepSynced(true);


        if (getItemViewType(position) == TYPE_STOCK) {
            final Chat chat = chats.get(position);
            if (chat.getId()==0) {
                ((StockHolder) holder).Layout.setPadding(15, 5, 60, 5);
                ((StockHolder) holder).Layout.setGravity(Gravity.LEFT);
                ((StockHolder) holder).Name.setText(chat.getName()+" ("+chat.getSymbol()+")");
                ((StockHolder) holder).Time.setText(chat.getTime());
                ((StockHolder) holder).Close.setText(String.format("%.2f", chat.getClose()));
                ((StockHolder) holder).Open.setText(String.format("%.2f", chat.getOpen()));
                ((StockHolder) holder).High.setText(String.format("%.2f", chat.getHigh()));
                isFollow(chat.getSymbol(), ((StockHolder) holder).Follow);
                FollowsCount(chat.getSymbol(), ((StockHolder) holder).Follow_Count);
                ((StockHolder) holder).Follow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((StockHolder) holder).Follow.getTag().equals("follow")) {
                            mFavouritesDatabase.child(chat.getSymbol()).child(mCurrentUserId).setValue(true);
                            HashMap followMap = new HashMap<>();
                            followMap.put("website", "https://amstatbot.herokuapp.com/predict/_stock"+chat.getSymbol());
                            mFollowingDatabase.child(chat.getSymbol()).setValue(followMap);
                        } else {
                            mFavouritesDatabase.child(chat.getSymbol()).child(mCurrentUserId).removeValue();
                            mFollowingDatabase.child(chat.getSymbol()).removeValue();
                        }
                    }
                });

                if (chat.getPercent()>0){
                    ((StockHolder) holder).Percent.setText(String.format("%.2f", chat.getPercent()));
                    ((StockHolder) holder).Percent.setTextColor(Color.GREEN);
                    ((StockHolder) holder).Percent_Image.setImageResource(R.drawable.percent_positive);
                }else{
                    ((StockHolder) holder).Percent.setText(String.format("%.2f", chat.getPercent()));
                    ((StockHolder) holder).Percent.setTextColor(Color.RED);
                    ((StockHolder) holder).Percent_Image.setImageResource(R.drawable.percent_negative);
                }
                ((StockHolder) holder).Low.setText(String.format("%.2f", chat.getLow()));
                ((StockHolder) holder).Volume.setText(String.format("%.1f", chat.getHigh()));

                ((StockHolder) holder).Message_Layout.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {
                ((MessageHolder) holder).Layout.setPadding(100, 10, 15, 10);
                ((MessageHolder) holder).Layout.setGravity(Gravity.RIGHT);
                ((MessageHolder) holder).Message.setText(chat.getMessage());
                ((MessageHolder) holder).Time.setText(chat.getTime());
                ((MessageHolder) holder).Message_Layout.setCardBackgroundColor(Color.parseColor("#ff99cc00"));
            }
        }
        else if(getItemViewType(position) == TYPE_LOADING){
            final Chat chat = chats.get(position);
            if (chat.getId()==0) {
                ((LoadingHolder) holder).Layout.setPadding(15, 10, 100, 10);
                ((LoadingHolder) holder).Layout.setGravity(Gravity.LEFT);
                ((LoadingHolder) holder).Message_Layout.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        }else {
            final Chat chat = chats.get(position);
            if (chat.getId()==0) {
                if (chat.getMessage().contains("news")){
                    ((MessageHolder) holder).Message.setText("Click to NEWS");
                    ((MessageHolder) holder).Message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, NewsActivity.class);
                            intent.putExtra("link",chat.getMessage());
                            mContext.startActivity(intent);
                        }
                    });

                }else{
                    ((MessageHolder) holder).Message.setText(chat.getMessage());
                }
                ((MessageHolder) holder).Layout.setPadding(15, 10, 100, 10);
                ((MessageHolder) holder).Layout.setGravity(Gravity.LEFT);
                ((MessageHolder) holder).Time.setText(chat.getTime());
                ((MessageHolder) holder).Message_Layout.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {

                ((MessageHolder) holder).Layout.setPadding(100, 10, 15, 10);
                ((MessageHolder) holder).Layout.setGravity(Gravity.RIGHT);
                ((MessageHolder) holder).Message.setText(chat.getMessage());
                ((MessageHolder) holder).Time.setText(chat.getTime());
                ((MessageHolder) holder).Message_Layout.setCardBackgroundColor(Color.parseColor("#ff99cc00"));
            }
        }


    }

    @Override
    public int getItemViewType(int position) {
        final Chat chat = chats.get(position);
        if (chat.getType().equals("stock")) {
            return TYPE_STOCK;
        }else if(chat.getType().equals("loading")){
            return TYPE_LOADING;
        }else {
            return TYPE_MESSAGE;
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {

        public TextView Message, Time;
        public LinearLayout Layout;
        public CardView Message_Layout;

        public MessageHolder(View itemView) {
            super(itemView);

            Message = itemView.findViewById(R.id.message);
            Time = itemView.findViewById(R.id.time);
            Layout = itemView.findViewById(R.id.layout);
            Message_Layout = itemView.findViewById(R.id.layout_message);
        }
    }

    public class LoadingHolder extends RecyclerView.ViewHolder {

        public LoadingDots Dots;
        public LinearLayout Layout;
        public CardView Message_Layout;

        public LoadingHolder(View itemView) {
            super(itemView);

            Dots = itemView.findViewById(R.id.dots);
            Layout = itemView.findViewById(R.id.layout);
            Message_Layout = itemView.findViewById(R.id.layout_message);
        }
    }

    public class StockHolder extends RecyclerView.ViewHolder {

        public TextView Name, Close, Open, High, Low, Volume, Percent, Follow_Text, Follow_Count, Time;

        public ImageView Percent_Image, Follow;
        public LinearLayout Layout;
        public CardView Message_Layout;

        public StockHolder(View itemView) {
            super(itemView);

            Name = itemView.findViewById(R.id.name);
            Time = itemView.findViewById(R.id.time);
            Follow_Text = itemView.findViewById(R.id.follow_text);
            Follow = itemView.findViewById(R.id.follow);
            Follow_Count = itemView.findViewById(R.id.follow_count);

            Close = itemView.findViewById(R.id.close);
            Open = itemView.findViewById(R.id.open);
            Percent_Image=itemView.findViewById(R.id.percent_image);
            High = itemView.findViewById(R.id.high);
            Percent = itemView.findViewById(R.id.percent);
            Low = itemView.findViewById(R.id.low);
            Volume = itemView.findViewById(R.id.volume);

            Layout = itemView.findViewById(R.id.layout);
            Message_Layout = itemView.findViewById(R.id.layout_message);
        }

    }

    private void isFollow(String symbol, ImageView image) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mFavouritesDatabase.child(symbol).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    image.setImageResource(R.drawable.following);
                    image.setTag("following");
                } else {
                    image.setImageResource(R.drawable.follow);
                    image.setTag("follow");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void FollowsCount(String symbol, TextView count) {
        mFavouritesDatabase.child(symbol).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 1 || dataSnapshot.getChildrenCount() == 0) {
                    count.setText(dataSnapshot.getChildrenCount() + "");
                } else {
                    count.setText(dataSnapshot.getChildrenCount() + "");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

