package com.amstatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.amstatbot.Adapters.ChatAdapter;
import com.amstatbot.Login.LoginActivity;
import com.amstatbot.Models.Chat;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.android.volley.Request.Method.GET;

public class MainActivity extends AppCompatActivity {

    private RecyclerView Chat_List;
    private AutoCompleteTextView Chat_Message;
    private ImageView Chat_Send, Toolbar_Symbols, Toolbar_More, Toolbar_Favourites;
    private DatabaseReference mChatDatabase;
    private ChatAdapter chatAdapter;
    List<Chat> chatList;
    private FirebaseAuth mAuth;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Chat_List = findViewById(R.id.chat_list);
        Chat_Message = findViewById(R.id.chat_message);
        Chat_Send = findViewById(R.id.chat_send);
        Toolbar_Favourites = findViewById(R.id.toolbar_favourites);
        Toolbar_More = findViewById(R.id.toolbar_more);
        Toolbar_Symbols = findViewById(R.id.toolbar_symbols);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        Chat_List.setLayoutManager(mLayoutManager);
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList);
        Chat_List.setAdapter(chatAdapter);

        if (mAuth.getCurrentUser()!=null){
            FirebaseUser mCurrentUser = mAuth.getCurrentUser();
            String mCurrentUserId = mCurrentUser.getUid();
            mChatDatabase = FirebaseDatabase.getInstance().getReference().child("chats").child(mCurrentUserId);
            mChatDatabase.keepSynced(true);
            Chat_List.setHasFixedSize(true);


            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        textToSpeech.setLanguage(Locale.UK);
                    }
                }
            });

            getChatMessages();

            enableswipe();

            Toolbar_Symbols.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SymbolsActivity.class);
                    startActivity(intent);
                }
            });

            Toolbar_Favourites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, FavouritesActivity.class);
                    startActivity(intent);
                }
            });

            Toolbar_More.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.delete:
                                    mChatDatabase.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(MainActivity.this, "Deleted All.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return true;
                                case R.id.logout:
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    finish();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.inflate(R.menu.chat_menu);
                    popupMenu.show();
                }
            });

            Chat_Send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final String message = Chat_Message.getText().toString();
                    if (!message.isEmpty()) {
                        String key = mChatDatabase.push().getKey();
                        HashMap sendMap = new HashMap<>();
                        sendMap.put("id", 1);
                        sendMap.put("message", message);
                        String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
                        sendMap.put("time", time);
                        sendMap.put("type", "text");
                        Chat_Message.setText("");

                        mChatDatabase.child(key).setValue(sendMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete()) {
                                    ReadJson("https://amstatbot.herokuapp.com/predict/", message);
                                }
                            }
                        });


                    } else {
                        Toast.makeText(MainActivity.this, "Ask Something..", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

    private void enableswipe() {

        SwipeGraph swipeReplyController = new SwipeGraph(MainActivity.this, chatList, position -> {
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeReplyController);
        itemTouchHelper.attachToRecyclerView(Chat_List);
    }


    private void getChatMessages() {
        mChatDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    chatList.add(chat);
                }
                    chatAdapter.notifyDataSetChanged();
                Chat_List.smoothScrollToPosition(chatAdapter.getItemCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ReadJson(String msg, final String ur) {

        final String url=msg+ur;

        HashMap receiveMap = new HashMap<>();
        receiveMap.put("id", 0);
        receiveMap.put("message", "");
        receiveMap.put("type", "loading");
        mChatDatabase.child("loading").setValue(receiveMap);

        StringRequest stringRequest = new StringRequest(GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (ur.contains("_stock")){
                                if (!response.equals("Invalid")) {
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
                                    String key = mChatDatabase.push().getKey();
                                    HashMap receiveMap = new HashMap<>();
                                    receiveMap.put("id", 0);
                                    receiveMap.put("name", name);
                                    receiveMap.put("message", "");
                                    receiveMap.put("website", website);
                                    receiveMap.put("symbol", symbol);
                                    receiveMap.put("close", close);
                                    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
                                    receiveMap.put("time", time);
                                    receiveMap.put("high", high);
                                    receiveMap.put("low", low);
                                    receiveMap.put("open", open);
                                    receiveMap.put("volume", volume);
                                    receiveMap.put("percent", percent);
                                    receiveMap.put("type", "stock");
                                    mChatDatabase.child(key).setValue(receiveMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            mChatDatabase.child("loading").removeValue();
                                        }
                                    });
                                    if (!textToSpeech.isSpeaking())
                                        textToSpeech.speak(name + "Stock", TextToSpeech.QUEUE_FLUSH, null);
                                }else{
                                    String key = mChatDatabase.push().getKey();
                                    HashMap receiveMap = new HashMap<>();
                                    receiveMap.put("id", 0);
                                    receiveMap.put("message", "Sorry I Cannot Get You, Check Symbol of Company");
                                    String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
                                    receiveMap.put("time", time);
                                    receiveMap.put("type", "chat");
                                    mChatDatabase.child(key).setValue(receiveMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            mChatDatabase.child("loading").removeValue();
                                        }
                                    });
                                    if(!textToSpeech.isSpeaking())
                                        textToSpeech.speak("Sorry I Cannot Get You, Check Symbol of Company", TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }else{
                                String key = mChatDatabase.push().getKey();
                                HashMap receiveMap = new HashMap<>();
                                receiveMap.put("id", 0);
                                receiveMap.put("message", response);
                                String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
                                receiveMap.put("time", time);
                                receiveMap.put("type", "chat");
                                mChatDatabase.child(key).setValue(receiveMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mChatDatabase.child("loading").removeValue();
                                    }
                                });
                                if(!textToSpeech.isSpeaking())
                                    textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null);
                            }


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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();

        if (mCurrentUser == null) {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }
    }

    public void onPause(){
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }

}
