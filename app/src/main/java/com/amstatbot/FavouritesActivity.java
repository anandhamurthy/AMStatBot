package com.amstatbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amstatbot.Adapters.FavouritesAdapter;
import com.amstatbot.Models.Favourites;
import com.amstatbot.Models.Following;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.Method.GET;

public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView Favourites_List;
    private FloatingActionButton mRefresh;
    private FavouritesAdapter favouritesAdapter;
    private RelativeLayout mNoFavourites;

    private DatabaseReference mFollowingDatabase;
    List<Following> followingList;
    List<Favourites> favouritesList;
    private FirebaseAuth mAuth;

    private ImageView Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        mAuth = FirebaseAuth.getInstance();

        Back = findViewById(R.id.toolbar_icon);
        Favourites_List = findViewById(R.id.favourites_list);
        mRefresh = findViewById(R.id.refresh);
        mNoFavourites=findViewById(R.id.no_favourites);

        mFollowingDatabase = FirebaseDatabase.getInstance().getReference().child("Following").child(mAuth.getCurrentUser().getUid());
        mFollowingDatabase.keepSynced(true);
        Favourites_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        Favourites_List.setLayoutManager(mLayoutManager);
        favouritesList = new ArrayList<>();
        followingList = new ArrayList<>();
        favouritesAdapter = new FavouritesAdapter(this, favouritesList);
        Favourites_List.setAdapter(favouritesAdapter);

        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFavourites();
            }
        });

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getFavourites();
    }

    private void getFavourites() {
        mFollowingDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    followingList.clear();
                    favouritesList.clear();
                    Favourites_List.setVisibility(View.VISIBLE);
                    mRefresh.setVisibility(View.VISIBLE);
                    mNoFavourites.setVisibility(View.GONE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Following following = snapshot.getValue(Following.class);
                        getDetails(following.getWebsite());
                        followingList.add(following);
                        favouritesAdapter.notifyDataSetChanged();
                    }
                    favouritesAdapter.notifyDataSetChanged();
                }else{
                    mNoFavourites.setVisibility(View.VISIBLE);
                    mRefresh.setVisibility(View.GONE);
                    Favourites_List.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getDetails(String website) {

        StringRequest stringRequest = new StringRequest(GET, website,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            String name = json.getString("name");
                            String website = json.getString("website");
                            Double close = json.getDouble("close");
                            Double open = json.getDouble("open");
                            Double high = json.getDouble("high");
                            Double low = json.getDouble("low");
                            Double volume = json.getDouble("volume");
                            Double percent = json.getDouble("percent");
                            String symbol = json.getString("symbol");

                            favouritesList.add(new Favourites(name, website, symbol, close, open, high, volume, low, percent));
                            favouritesAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        ApplicationController.getInstance().addToRequestQueue(stringRequest);
    }

}
