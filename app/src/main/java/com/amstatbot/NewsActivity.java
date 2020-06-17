package com.amstatbot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amstatbot.Adapters.NewsAdapter;
import com.amstatbot.Models.News;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<News> viewItems;
    private NewsAdapter mAdapter;
    private ProgressDialog pd;

    private ImageView Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Intent intent = getIntent();
        String url = intent.getStringExtra("link");

        Back = findViewById(R.id.toolbar_icon);
        mRecyclerView = findViewById(R.id.news_list);
        mRecyclerView.setHasFixedSize(true);
        viewItems = new ArrayList<>();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NewsAdapter(NewsActivity.this, viewItems);
        mRecyclerView.setAdapter(mAdapter);

        pd = new ProgressDialog(NewsActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setTitle("Loading..");
        pd.show();

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        parseJSON(url);
    }

    private void parseJSON(String url) {
        viewItems.clear();
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("news");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject hit = jsonArray.getJSONObject(i);
                                String link = hit.getString("link");
                                String text = hit.getString("text");
                                String title = hit.getString("title");

                                viewItems.add(new News(link, title, text));
                                mAdapter.notifyDataSetChanged();
                            }

                            mAdapter = new NewsAdapter(NewsActivity.this, viewItems);
                            mRecyclerView.setAdapter(mAdapter);
                            pd.dismiss();
                            mAdapter.notifyDataSetChanged();

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

        ApplicationController.getInstance().addToRequestQueue(request);


    }


}
