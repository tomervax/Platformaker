package com.tomer.platformaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tomer.platformaker.about.AboutActivity;
import com.tomer.platformaker.editor.EditorActivity;
import com.tomer.platformaker.game.GameActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static DocumentReference user;
    public static String username;
    public static String activeLevelId;

    private boolean initialized = false;
    private HashMap<String, Object> activeLevel;

    private ImageButton editorButton;
    private LevelAdapter levelAdapter;

    private LocalBroadcastManager localBroadcastManager;

    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tile.loadImages(getResources());
        setContentView(R.layout.activity_main);

        editorButton = findViewById(R.id.button_editor);
        editorButton.setImageDrawable(AppCompatResources.getDrawable(this, android.R.drawable.ic_popup_sync));

        //Encrypts the device id to create the user id
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID).getBytes(StandardCharsets.UTF_8));
        String id = bytesToHex(hash);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        user = db.collection("users").document(id);
        user.get().addOnSuccessListener(userSnap -> {
            if (!userSnap.exists()) {
                showSetNameMenu();
            } else if (!userSnap.contains("activeLevel")) {
                setAsInitialized();
            }
        }).addOnFailureListener(e -> setAsInitialized());

        user.addSnapshotListener((userSnap, e) -> {
            if (e != null) {
                return;
            }
            if (userSnap.exists()) {
                username = userSnap.getString("name");
                if (userSnap.contains("activeLevel")) {
                    activeLevelId = userSnap.getDocumentReference("activeLevel").getId();
                    request(LevelService.LOAD_ACTIVE_REQUEST);
                }
            }
        });

        //Sets up th recyclerView
        RecyclerView levelRecycler = findViewById(R.id.recycler_levels);
        levelAdapter = new LevelAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        levelRecycler.setAdapter(levelAdapter);
        levelRecycler.setLayoutManager(linearLayoutManager);
        levelRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    request(LevelService.LOAD_NEXT_REQUEST);
                }
            }
        });

        //Sets up swipe refresh
        swipeContainer = findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(this::refresh);

        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                swipeContainer.setRefreshing(false);
                HashMap<String, Object> level = (HashMap<String, Object>) intent.getSerializableExtra("level");
                if (level.get("id").equals(activeLevelId)) {
                    activeLevel = level;
                    setAsInitialized();
                }
                levelAdapter.putLevel(level);
            }
        }, new IntentFilter(LevelService.LEVEL_UPDATE_ACTION));

        //Starts the level service
        Intent intent = new Intent(this, LevelService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        refresh();
        super.onResume();
    }

    private void refresh(){
        levelAdapter.clear();
        request(LevelService.SEND_ALL_REQUEST);
    }

    private void request(String request) {
        Intent intent = new Intent(LevelService.LEVEL_REQUEST_ACTION);
        intent.putExtra("request", request);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void setAsInitialized() {
        initialized = true;
        editorButton.setImageDrawable(AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_edit));
    }

    public void playLevel(HashMap<String, Object> level){
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("level", level);
        startActivity(intent);
    }

    public void makeLevel(View view){
        if (initialized) {
            Intent intent = new Intent(this, EditorActivity.class);
            if (activeLevel != null) {
                intent.putExtra("level", activeLevel);
            }
            startActivity(intent);
        } else {
            Toast.makeText(this,"Loading...", Toast.LENGTH_SHORT).show();
        }
    }

    public synchronized void showSetNameMenu() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View menu = getLayoutInflater().inflate(R.layout.menu_set_name, null);
            builder.setView(menu);
            AlertDialog dialog = builder.show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            menu.findViewById(R.id.button_set_name).setOnClickListener(v -> {
                EditText text = menu.findViewById(R.id.edit_name);
                HashMap<String, Object> userData = new HashMap<>();
                username = text.getText().toString();
                userData.put("name", username);
                user.set(userData).addOnSuccessListener(snapshot -> setAsInitialized()).addOnFailureListener(e -> setAsInitialized());
                dialog.dismiss();
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.menu_item_about).setOnMenuItemClickListener((v) -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        });
        return true;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}