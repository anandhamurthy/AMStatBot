package com.amstatbot.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amstatbot.Models.News;
import com.amstatbot.Models.Symbols;
import com.amstatbot.R;

import java.util.ArrayList;
import java.util.List;

public class SymbolsAdapter extends RecyclerView.Adapter<SymbolsAdapter.ImageViewHolder> implements Filterable {

    private Context mContext;
    private List<Symbols> mSymbols;

    private List<Symbols> DefaultSymbolList;

    public SymbolsAdapter(Context context, List<Symbols> list) {
        mContext = context;
        mSymbols = list;
        DefaultSymbolList = list;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_symbols, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        final Symbols symbols = mSymbols.get(position);

        holder.Company.setText(symbols.getCompany());
        holder.Symbol.setText(symbols.getSymbol());
    }

    @Override
    public int getItemCount() {
        return mSymbols.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView Company, Symbol;
        public ImageViewHolder(View itemView) {
            super(itemView);

            Company = itemView.findViewById(R.id.company);
            Symbol = itemView.findViewById(R.id.symbol);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();
                if (charString.isEmpty()) {
                    mSymbols = DefaultSymbolList;
                } else {
                    List<Symbols> filteredList = new ArrayList<>();
                    for (Symbols row : DefaultSymbolList) {

                        if (row.getCompany().toLowerCase().contains(charString) ||
                                row.getSymbol().toLowerCase().contains(charSequence)
                        ) {
                            filteredList.add(row);
                        }
                    }

                    mSymbols = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mSymbols;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mSymbols = (ArrayList<Symbols>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface SearchAdapterListener {
        void onSearchSelected(Symbols symbols);
    }

}