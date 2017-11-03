package com.skillberg.notes;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.skillberg.notes.db.NotesContract;

/**
 * Activity для просмотра заметки
 */
public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_NOTE_ID = "note_id";

    private TextView noteTv;

    private long noteId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteTv = findViewById(R.id.text_tv);

        noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
        if (noteId == -1) {
            finish();
        }

        getLoaderManager().initLoader(
                0, // Идентификатор загрузчика
                null, // Аргументы
                this // Callback для событий загрузчика
        );
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,  // Контекст
                ContentUris.withAppendedId(NotesContract.Notes.URI, noteId), // URI
                NotesContract.Notes.SINGLE_PROJECTION, // Столбцы
                null, // Параметры выборки
                null, // Аргументы выборки
                null // Сортировка по умолчанию
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i("Test", "Load finished: " + cursor.getCount());

        cursor.setNotificationUri(getContentResolver(), NotesContract.Notes.URI);

        displayNote(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Редактирование заметки
     */
    private void editNote() {
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra(CreateNoteActivity.EXTRA_NOTE_ID, noteId);

        startActivity(intent);
    }


    /**
     * Отображаем данные из курсора
     */
    private void displayNote(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            // Если не получилось перейти к первой строке — завершаем Activity

            finish();
        }

        String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_TITLE));
        String noteText = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_NOTE));

        setTitle(title);
        noteTv.setText(noteText);
    }
}
