package com.example.bullsandcows;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<GameLogger.GameRecord> history;

    public HistoryAdapter(List<GameLogger.GameRecord> history) {
        this.history = history;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameLogger.GameRecord record = history.get(position);
        holder.wordText.setText(record.word);
        holder.resultText.setText(record.won ? "✅" : "❌");
        holder.attemptsText.setText(record.attempts + " поп.");
        holder.dateText.setText(record.date);
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public void updateData(List<GameLogger.GameRecord> newHistory) {
        this.history = newHistory;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wordText, resultText, attemptsText, dateText;

        ViewHolder(View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.word_text);
            resultText = itemView.findViewById(R.id.result_text);
            attemptsText = itemView.findViewById(R.id.attempts_text);
            dateText = itemView.findViewById(R.id.date_text);
        }
    }
}