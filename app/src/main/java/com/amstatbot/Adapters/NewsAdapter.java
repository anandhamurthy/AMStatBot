package com.amstatbot.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amstatbot.Models.News;
import com.amstatbot.R;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ImageViewHolder> {

    private Context mContext;
    private List<News> mNews;

    public NewsAdapter(Context context, List<News> list) {
        mContext = context;
        mNews = list;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_news, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        final News news = mNews.get(position);

        holder.Title.setText(news.getTitle());
        holder.Text.setText(news.getText());
        holder.Link.setText(news.getLink());
    }

    @Override
    public int getItemCount() {
        return mNews.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView Title, Text, Link;
        public ImageViewHolder(View itemView) {
            super(itemView);

            Title = itemView.findViewById(R.id.title);
            Text = itemView.findViewById(R.id.text);
            Link = itemView.findViewById(R.id.link);
        }
    }

}