package com.skillberg.notes;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.skillberg.notes.db.NotesContract;

/**
 * Activity для создания новой заметки
 */
public class CreateNoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_NOTE_ID = "note_id";

    private TextInputEditText titleEt;
    private TextInputEditText textEt;

    private TextInputLayout titleTil;
    private TextInputLayout textTil;

    private long noteId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleEt = findViewById(R.id.title_et);
        textEt = findViewById(R.id.text_et);

        titleTil = findViewById(R.id.title_til);
        textTil = findViewById(R.id.text_til);

        noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);

        if (noteId != -1) {
            getLoaderManager().initLoader(
                    0, // Идентификатор загрузчика
                    null, // Аргументы
                    this // Callback для событий загрузчика
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.create_note, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;

            case R.id.action_save:
                saveNote();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Метод для сохранения заметок
     */
    private void saveNote() {
        String title = titleEt.getText().toString().trim();
        String text = textEt.getText().toString().trim();

        boolean isCorrect = true;

        if (TextUtils.isEmpty(title)) {
            isCorrect = false;

            titleTil.setError(getString(R.string.error_empty_field));
            titleTil.setErrorEnabled(true);
        } else {
            titleTil.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(text)) {
            isCorrect = false;

            textTil.setError(getString(R.string.error_empty_field));
            textTil.setErrorEnabled(true);
        } else {
            textTil.setErrorEnabled(false);
        }

        if (isCorrect) {
            long currentTime = System.currentTimeMillis();

            ContentValues contentValues = new ContentValues();
            contentValues.put(NotesContract.Notes.COLUMN_TITLE, title);
            contentValues.put(NotesContract.Notes.COLUMN_NOTE, text);

            if (noteId == -1) {
                contentValues.put(NotesContract.Notes.COLUMN_CREATED_TS, currentTime);
            }

            contentValues.put(NotesContract.Notes.COLUMN_UPDATED_TS, currentTime);

            if (noteId == -1) {
                getContentResolver().insert(NotesContract.Notes.URI, contentValues);
            } else {
                getContentResolver().update(ContentUris.withAppendedId(NotesContract.Notes.URI, noteId),
                        contentValues,
                        null,
                        null);
            }

            finish();
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
     * Отображаем данные из курсора
     */
    private void displayNote(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            // Если не получилось перейти к первой строке — завершаем Activity

            finish();
        }

        String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_TITLE));
        String noteText = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_NOTE));

        titleEt.setText(title);
        textEt.setText(noteText);
    }
}
