package com.tomer.platformaker;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.ViewHolder>{
    private final ArrayList<HashMap<String, Object>> levels = new ArrayList<>();
    private final HashMap<String, Integer> index = new HashMap<>();
    private MainActivity activity;

    public LevelAdapter(MainActivity activity) {
        this.activity = activity;
    }

    public void putLevel(HashMap<String, Object> level) {
        String id = (String) level.get("id");
        int i;
        if (index.containsKey(id)) {
            i = index.get(id);
            levels.set(i, level);
        } else {
            i = levels.size();
            index.put(id, i);
            levels.add(level);
        }
        notifyItemChanged(i);
    }

    public void clear() {
        levels.clear();
        index.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_level, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, Object> level = levels.get(position);
        holder.bindLevel(Level.deserialize(level, null), position);
    }

    private static String constrain(String s, int l) {
        if (s.length() >= l) {
            return s.substring(0, l);
        }
        return s;
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, author;
        private final ImageView image;
//        private final RatingBar rating;
        private int index;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_card_level);
            title = itemView.findViewById(R.id.text_card_title);
            author = itemView.findViewById(R.id.text_card_author);
//            rating = itemView.findViewById(R.id.rating_card);
            itemView.setOnClickListener(v -> {
                activity.playLevel(levels.get(index));
            });
        }

        public void bindLevel(Level level, int index) {
            this.index = index;
            title.setText(constrain(level.title, 13));
            author.setText("By " + constrain(level.author, 15));
            image.setImageBitmap(level.drawLevelImage());
//            rating.setRating((float) (Math.random()*5f));
        }
    }
}
