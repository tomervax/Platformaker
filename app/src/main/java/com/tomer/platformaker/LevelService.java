package com.tomer.platformaker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LevelService extends Service {
    public static final String LEVEL_UPDATE_ACTION = "LevelUpdate", LEVEL_REQUEST_ACTION = "LevelRequest";
    public static final String SEND_ALL_REQUEST = "sendAll", LOAD_NEXT_REQUEST = "loadNext", LOAD_ACTIVE_REQUEST = "loadActiveLevel";

    private static final int BATCH_SIZE = 10;

    private FirebaseFirestore db;

    private final HashMap<String, Integer> index = new HashMap<>();
    private final ArrayList<HashMap<String, Object>> levels = new ArrayList<>();

    private DocumentSnapshot lastDoc;
    private Query query;
    private CollectionReference levelsRef;

    private LocalBroadcastManager localBroadcastManager;

    private boolean isAfterRefresh;

    public LevelService() {
    }

    public void loadLevelBatch() {
        if (lastDoc != null) {
            Query nextQuery = query.startAfter(lastDoc);
            if (nextQuery.equals(query) && !isAfterRefresh){
                return;
            }
            isAfterRefresh = false;
            query = nextQuery;
        }
        query.get().addOnSuccessListener(snapshots -> {
            List<DocumentSnapshot> documents = snapshots.getDocuments();
            if (documents.isEmpty()) {
                return;
            }
            for (DocumentSnapshot document: documents) {
                sendLevel(createLevel(document));
            }
            lastDoc = documents.get(documents.size() - 1);
        });

        query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }
            for (DocumentChange change: snapshots.getDocumentChanges()) {
                if (change.getType() == DocumentChange.Type.MODIFIED || change.getType() == DocumentChange.Type.ADDED) {
                    DocumentSnapshot document = change.getDocument();
                    createLevel(document);
                }
            }
        });
    }

    private void loadLevel(String id) {
        if (index.containsKey(id)) {
            sendLevel(levels.get(index.get(id)));
        } else {
            DocumentReference levelRef = levelsRef.document(id);
            levelRef.get().addOnSuccessListener(document ->
                    sendLevel(createLevel(document)));
            levelRef.addSnapshotListener((document, e) -> {
                if (e != null) {
                    return;
                }
                createLevel(document);
            });
        }
    }

    private HashMap<String, Object> createLevel(DocumentSnapshot document) {
        HashMap<String, Object> level = (HashMap<String, Object>) document.getData();
        if (level != null) {
            String id = document.getId();
            level.put("id", id);
            if (!index.containsKey(id)) {
                int i = levels.size();
                index.put(id, i);
                levels.add(level);
            } else {
                int i = index.get(id);
                levels.set(i, level);
            }
        }
        return level;
    }

    public void sendLevel(HashMap<String, Object> level) {
        Intent intent = new Intent(LEVEL_UPDATE_ACTION);
        intent.putExtra("level", level);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        levelsRef = db.collection("levels");
        query = levelsRef.limit(BATCH_SIZE);
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String request = intent.getStringExtra("request");
                switch (request) {
                    case LOAD_NEXT_REQUEST:
                        loadLevelBatch();
                        break;
                    case SEND_ALL_REQUEST:
                        isAfterRefresh = true;
                        for (HashMap<String, Object> level : levels) {
                            sendLevel(level);
                        }
                        break;
                    case LOAD_ACTIVE_REQUEST:
                        if (MainActivity.activeLevelId != null) {
                            loadLevel(MainActivity.activeLevelId);
                        }
                        break;
                }
            }
        }, new IntentFilter(LEVEL_REQUEST_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loadLevelBatch();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}