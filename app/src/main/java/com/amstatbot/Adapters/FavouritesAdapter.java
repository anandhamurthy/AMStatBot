package com.amstatbot.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amstatbot.Models.Favourites;
import com.amstatbot.Models.Following;
import com.amstatbot.R;
import com.amstatbot.WebViewActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.FavouritesHolder> {

    private Context mContext;
    private List<Favourites> mFavourites;
    private DatabaseReference mFavouritesDatabase, mFollowingDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;

    public FavouritesAdapter(Context context, List<Favourites> list) {
        mContext = context;
        mFavourites = list;
    }


    @NonNull
    @Override
    public FavouritesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_favourite, parent, false);
        return new FavouritesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FavouritesHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        final Favourites favourites = mFavourites.get(position);

        mFollowingDatabase = FirebaseDatabase.getInstance().getReference().child("Following").child(mCurrentUserId);
        mFollowingDatabase.keepSynced(true);

        mFavouritesDatabase = FirebaseDatabase.getInstance().getReference().child("Favourites");
        mFavouritesDatabase.keepSynced(true);

        holder.Name.setText(favourites.getWebsite());
        holder.Close.setText(String.format("%.2f", favourites.getClose()));
        holder.Open.setText(String.format("%.2f", favourites.getOpen()));
        holder.High.setText(String.format("%.2f", favourites.getHigh()));
        if (favourites.getPercent()>0){
            holder.Percent.setText(String.format("%.2f", favourites.getPercent()));
            holder.Percent.setTextColor(Color.GREEN);
            holder.Percent_Image.setImageResource(R.drawable.percent_positive);
        }else{
            holder.Percent.setText(String.format("%.2f", favourites.getPercent()));
            holder.Percent.setTextColor(Color.RED);
            holder.Percent_Image.setImageResource(R.drawable.percent_negative);
        }
        holder.Low.setText(String.format("%.2f", favourites.getLow()));
        holder.Volume.setText(String.format("%.1f", favourites.getHigh()));
        holder.Graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra("link", favourites.getWebsite());
                mContext.startActivity(intent);
            }
        });
        isFollow(favourites.getSymbol(), holder.Follow);
        FollowsCount(favourites.getSymbol(), holder.Follow_Count);
        holder.Follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.Follow.getTag().equals("follow")) {
                    mFavouritesDatabase.child(favourites.getSymbol()).child(mCurrentUserId).setValue(true);
                    HashMap followMap = new HashMap<>();
                    followMap.put("website", "https://amstatbot.herokuapp.com/predict/_stock"+favourites.getSymbol());
                    mFollowingDatabase.child(favourites.getSymbol()).setValue(followMap);
                } else {
                    mFavouritesDatabase.child(favourites.getSymbol()).child(mCurrentUserId).removeValue();
                    mFollowingDatabase.child(favourites.getSymbol()).removeValue();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFavourites.size();
    }


    public class FavouritesHolder extends RecyclerView.ViewHolder {

        public TextView Name, Close, Open, High, Low, Volume, Percent, Follow_Count;

        public ImageView Percent_Image, Graph, Follow;

        public FavouritesHolder(View itemView) {
            super(itemView);

            Name = itemView.findViewById(R.id.name);
            Close = itemView.findViewById(R.id.close);
            Open = itemView.findViewById(R.id.open);
            Percent_Image=itemView.findViewById(R.id.percent_image);
            High = itemView.findViewById(R.id.high);
            Percent = itemView.findViewById(R.id.percent);
            Low = itemView.findViewById(R.id.low);
            Volume = itemView.findViewById(R.id.volume);
            Graph = itemView.findViewById(R.id.graph);
            Follow = itemView.findViewById(R.id.follow);
            Follow_Count = itemView.findViewById(R.id.follow_count);
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
                count.setText(dataSnapshot.getChildrenCount() + "");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}