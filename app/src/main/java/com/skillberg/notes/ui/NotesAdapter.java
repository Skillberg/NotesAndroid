package com.skillberg.notes.ui;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skillberg.notes.R;
import com.skillberg.notes.db.NotesContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Адаптер для заметок
 */
public class NotesAdapter extends CursorRecyclerAdapter<NotesAdapter.ViewHolder> {

    private final OnNoteClickListener onNoteClickListener;

    public NotesAdapter(Cursor cursor, OnNoteClickListener onNoteClickListener) {
        super(cursor);

        this.onNoteClickListener = onNoteClickListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        int idColumnIndex = cursor.getColumnIndexOrThrow(NotesContract.Notes._ID);
        long id = cursor.getLong(idColumnIndex);

        viewHolder.itemView.setTag(id);

        int titleColumnIndex = cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_TITLE);
        String title = cursor.getString(titleColumnIndex);

        viewHolder.titleTv.setText(title);

        int dateColumnIndex = cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_UPDATED_TS);
        long updatedTs = cursor.getLong(dateColumnIndex);
        Date date = new Date(updatedTs);

        viewHolder.dateTv.setText(viewHolder.SDF.format(date));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.view_item_note, parent, false);

        return new ViewHolder(view);
    }

    /**
     * View holder
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        private final TextView titleTv;
        private final TextView dateTv;

        public ViewHolder(View itemView) {
            super(itemView);

            this.titleTv = itemView.findViewById(R.id.title_tv);
            this.dateTv = itemView.findViewById(R.id.date_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long noteId = (Long) v.getTag();

                    onNoteClickListener.onNoteClick(noteId);
                }
            });
        }
    }

    /**
     * Слушатель для обработки кликов
     */
    public interface OnNoteClickListener {
        void onNoteClick(long noteId);
    }
}
