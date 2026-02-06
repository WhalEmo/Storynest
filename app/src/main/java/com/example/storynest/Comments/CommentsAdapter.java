package com.example.storynest.Comments;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class CommentsAdapter
        extends ListAdapter<Object, CommentsAdapter.CommentViewHolder> {

    public CommentsAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        // şimdilik boş ViewHolder
        return new CommentViewHolder(new android.view.View(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(
            @NonNull CommentViewHolder holder,
            int position
    ) {
        // şimdilik boş
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        public CommentViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
        }
    }

    private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Object>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Object oldItem,
                        @NonNull Object newItem
                ) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Object oldItem,
                        @NonNull Object newItem
                ) {
                    return oldItem.equals(newItem);
                }
            };
}
