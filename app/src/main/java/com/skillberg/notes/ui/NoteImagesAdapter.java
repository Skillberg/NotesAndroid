package com.skillberg.notes.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.skillberg.notes.R;
import com.skillberg.notes.db.NotesContract;

/**
 * Адаптер для отображения изображений заметки
 */
public class NoteImagesAdapter extends CursorRecyclerAdapter<NoteImagesAdapter.ViewHolder> {

    @Nullable
    private final OnNoteImageLongClickListener onNoteImageLongClickListener;

    public NoteImagesAdapter(Cursor cursor,
                             @Nullable OnNoteImageLongClickListener onNoteImageLongClickListener) {
        super(cursor);

        this.onNoteImageLongClickListener = onNoteImageLongClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.view_item_note_image, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        long imageId = cursor.getLong(cursor.getColumnIndexOrThrow(NotesContract.Images._ID));
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Images.COLUMN_PATH));

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        viewHolder.imageView.setImageBitmap(bitmap);
        viewHolder.itemView.setTag(imageId);
    }

    /**
     * ViewHolder
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView;

            if (onNoteImageLongClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        long imageId = (long) ViewHolder.this.itemView.getTag();

                        onNoteImageLongClickListener.onImageLongClick(imageId);

                        return true;
                    }
                });
            }
        }

    }

    /**
     * Интерфейс для обработки кликов на изображении
     */
    public interface OnNoteImageLongClickListener {
        void onImageLongClick(long imageId);
    }

}
