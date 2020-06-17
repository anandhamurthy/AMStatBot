package com.amstatbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.amstatbot.Adapters.ChatAdapter;
import com.amstatbot.Adapters.SymbolsAdapter;
import com.amstatbot.Models.Chat;
import com.amstatbot.Models.Symbols;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SymbolsActivity extends AppCompatActivity implements SymbolsAdapter.SearchAdapterListener{

    private RecyclerView Symbols_List;
    private DatabaseReference mSymbolsDatabase;
    private SymbolsAdapter symbolsAdapter;
    private List<Symbols> symbolsList;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbols);

        Symbols_List = findViewById(R.id.symbols_list);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        Symbols_List.setLayoutManager(mLayoutManager);
        symbolsList = new ArrayList<>();
        symbolsAdapter = new SymbolsAdapter(this, symbolsList);
        Symbols_List.setAdapter(symbolsAdapter);

        mSymbolsDatabase = FirebaseDatabase.getInstance().getReference().child("Symbols").child("symbols");
        mSymbolsDatabase.keepSynced(true);

        getSymbols();

    }

    private void getSymbols() {
        mSymbolsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                symbolsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Symbols symbols = snapshot.getValue(Symbols.class);
                    symbolsList.add(symbols);
                }
                symbolsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                symbolsAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                symbolsAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onSearchSelected(Symbols symbols) {

    }
}
