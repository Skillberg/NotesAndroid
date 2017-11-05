package com.skillberg.notes;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.skillberg.notes.db.NotesContract;
import com.skillberg.notes.ui.NoteImagesAdapter;

/**
 * Activity для просмотра заметки
 */
public class NoteActivity extends BaseNoteActivity {

    public static final String EXTRA_NOTE_ID = "note_id";

    private TextView noteTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteTv = findViewById(R.id.text_tv);

        RecyclerView recyclerView = findViewById(R.id.images_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        noteImagesAdapter = new NoteImagesAdapter(null);
        recyclerView.setAdapter(noteImagesAdapter);

        noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
        if (noteId != -1) {
            initNoteLoader();
            initImagesLoader();
        } else {
            finish();
        }
    }

    /**
     * Отображаем данные из курсора
     */
    @Override
    protected void displayNote(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            // Если не получилось перейти к первой строке — завершаем Activity

            finish();
            return;
        }

        String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_TITLE));
        String noteText = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_NOTE));

        setTitle(title);
        noteTv.setText(noteText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.view_note, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;

            case R.id.action_edit:
                editNote();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Редактирование заметки
     */
    private void editNote() {
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra(CreateNoteActivity.EXTRA_NOTE_ID, noteId);

        startActivity(intent);
    }


}
