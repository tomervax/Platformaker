package com.tomer.platformaker.editor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tomer.platformaker.LevelService;
import com.tomer.platformaker.MainActivity;
import com.tomer.platformaker.R;
import com.tomer.platformaker.game.GameActivity;

import java.util.HashMap;

public class EditorActivity extends AppCompatActivity {

    private EditorView editorView;
    private FirebaseFirestore db;
    private EditText levelTitleEditText;
    private ImageButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_editor);
        getSupportActionBar().hide();
        editorView = findViewById(R.id.view_editor);
        editorView.init(
                findViewById(R.id.image_tile),
                findViewById(R.id.button_pan),
                findViewById(R.id.button_draw),
                findViewById(R.id.button_erase),
                findViewById(R.id.button_eyedropper)
        );
        saveButton = findViewById(R.id.button_save_level);

        db = FirebaseFirestore.getInstance();

        levelTitleEditText = findViewById(R.id.edit_level_title);
        levelTitleEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus();
                editorView.level.title = v.getText().toString();
            }
            return false;
        });

        Intent intent = getIntent();
        editorView.restart((HashMap<String, Object>) intent.getSerializableExtra("level"));
        if (editorView.level.title != null) {
            levelTitleEditText.setText(editorView.level.title);
        }
    }

    public void returnToHome(View view) {
        finish();
    }

    public void clearLevel(View view) {
        editorView.restart(null);
        levelTitleEditText.setText("");
        Toast.makeText(this, "Created new level", Toast.LENGTH_SHORT).show();
    }

    public void showResizeMenu(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View menu = getLayoutInflater().inflate(R.layout.menu_level_resize, null);
        EditText editWidth = menu.findViewById(R.id.edit_width);
        EditText editHeight = menu.findViewById(R.id.edit_height);
        editWidth.setHint("width: " + editorView.level.WIDTH);
        editHeight.setHint("height: " + editorView.level.HEIGHT);
        builder.setView(menu);
        AlertDialog dialog = builder.show();
        menu.findViewById(R.id.button_resize).setOnClickListener(v -> {
            int width = 0, height = 0;
            try {
                if (editWidth.getText().length() == 0){
                    width = editorView.level.WIDTH;
                } else {
                    width = Integer.parseInt(editWidth.getText().toString());
                }
                if (editHeight.getText().length() == 0){
                    height = editorView.level.HEIGHT;
                } else {
                    height = Integer.parseInt(editHeight.getText().toString());
                }
            } catch (NumberFormatException e){
                Toast.makeText(this,"Invalid level dimensions", Toast.LENGTH_SHORT).show();
            }
            if (width > 1 && height > 1) {
                editorView.level.resize(width, height);
                editorView.calcMinZoomConstraint();
                editorView.calcCameraConstraints();
                editorView.changeZoom(1);
                editorView.updateView();
                dialog.dismiss();
            } else {
                Toast.makeText(this,"Invalid level dimensions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void scrollTile(View view) {
        editorView.scrollTile(view.getId() == R.id.button_scroll_up);
    }

    public void zoom(View view) {
        if (view.getId() == R.id.button_zoom_in){
            editorView.zoomIn();
        } else {
            editorView.zoomOut();
        }
        editorView.updateView();
    }

    public void playLevel(View view){
        if (editorView.level.getPlayerSpawnX() < 0 || editorView.level.getPlayerSpawnY() < 0) {
            Toast.makeText(this,"Level does not have a player start", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, GameActivity.class);
            HashMap<String, Object> level = editorView.level.serialize();
            level.put("id", editorView.level.id);
            level.put("author", editorView.level.author);
            intent.putExtra("level", level);
            startActivity(intent);
        }
    }

    public void saveLevel(View view){
        if (editorView.level.getPlayerSpawnX() < 0 || editorView.level.getPlayerSpawnY() < 0) {
            Toast.makeText(this, "Upload failed: level does not have a player start", Toast.LENGTH_SHORT).show();
            return;
        } else if (editorView.level.title == null) {
            Toast.makeText(this, "Upload failed: level does not have a title", Toast.LENGTH_SHORT).show();
            return;
        }
        CollectionReference levelsRef = db.collection("levels");
        saveButton.setImageDrawable(AppCompatResources.getDrawable(this, android.R.drawable.ic_popup_sync));
        if (editorView.level.id == null) {
            levelsRef.add(editorView.level.serialize())
                    .addOnSuccessListener(documentReference -> {
                        editorView.level.id = documentReference.getId();
                        HashMap<String, Object> user = new HashMap<>();
                        user.put("activeLevel", levelsRef.document(editorView.level.id));
                        MainActivity.user.set(user, SetOptions.merge());
                        saveButton.setImageDrawable(AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_upload));
                        Toast.makeText(this, "Upload succeeded", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        saveButton.setImageDrawable(AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_upload));
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            levelsRef.document(editorView.level.id).set(editorView.level.serialize())
                    .addOnSuccessListener(documentReference -> {
                        saveButton.setImageDrawable(AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_upload));
                        Toast.makeText(this, "Upload succeeded", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        saveButton.setImageDrawable(AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_upload));
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}