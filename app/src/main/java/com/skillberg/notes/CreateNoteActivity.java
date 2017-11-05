package com.skillberg.notes;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.skillberg.notes.db.NotesContract;
import com.skillberg.notes.ui.NoteImagesAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Activity для создания новой заметки
 */
public class CreateNoteActivity extends BaseNoteActivity {

    public static final String EXTRA_NOTE_ID = "note_id";

    private static final int REQUEST_CODE_PICK_FROM_GALLERY = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;

    private TextInputEditText titleEt;
    private TextInputEditText textEt;

    private TextInputLayout titleTil;
    private TextInputLayout textTil;

    private File currentImageFile;


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

        RecyclerView recyclerView = findViewById(R.id.images_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        noteImagesAdapter = new NoteImagesAdapter(null);
        recyclerView.setAdapter(noteImagesAdapter);

        noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);

        if (noteId != -1) {
            initNoteLoader();

            initImagesLoader();
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
        }

        String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_TITLE));
        String noteText = cursor.getString(cursor.getColumnIndexOrThrow(NotesContract.Notes.COLUMN_NOTE));

        titleEt.setText(title);
        textEt.setText(noteText);
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

            case R.id.action_attach:
                showImageSelectionDialog();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FROM_GALLERY
                && resultCode == RESULT_OK
                && data != null) {

            // Получаем URI изображения
            Uri imageUri = data.getData();

            if (imageUri != null) {
                try {
                    // Получаем InputStream, из которого будем декодировать Bitmap
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    // Копируем изображение в наш файл
                    File imageFile = createImageFile();

                    writeInputStreamToFile(inputStream, imageFile);

                    addImageToDatabase(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO
                && resultCode == RESULT_OK) {

            // Сохраняем изображение
            addImageToDatabase(currentImageFile);

            // На всякий случай обнуляем файл
            currentImageFile = null;
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


    /**
     * Показываем диалог выбора изображения
     */
    private void showImageSelectionDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_attachment_variants)
                .setItems(R.array.attachment_variants, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            pickImageFromGallery();
                        } else if (which == 1) {
                            takePhoto();
                        }
                    }
                })
                .create();

        if (!isFinishing()) {
            alertDialog.show();
        }
    }

    /**
     * Создаём файл для хранения изображения
     */
    @Nullable
    private File createImageFile() {
        // Генерируем имя файла
        String filename = System.currentTimeMillis() + ".jpg";

        // Получаем приватную директорию на карте памяти для хранения изображений
        // Выглядит она примерно так: /sdcard/Android/data/com.skillberg.notes/files/Pictures
        // Директория будет создана автоматически, если ещё не существует
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Создаём файл
        File image = new File(storageDir, filename);
        try {
            if (image.createNewFile()) {
                return image;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Запускаем выбор изображения из галереи
     */
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, REQUEST_CODE_PICK_FROM_GALLERY);
    }

    /**
     * Получаем фотографию с камеры
     */
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Создаём файл для изображения
        currentImageFile = createImageFile();

        if (currentImageFile != null) {
            // Если файл создался — получаем его URI
            Uri imageUri = FileProvider.getUriForFile(this,
                    "com.skillberg.notes.fileprovider",
                    currentImageFile);

            // Передаём URI в камеру
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
        }
    }

    /**
     * Пишем из InputStream в файл
     */
    private void writeInputStreamToFile(InputStream inputStream, File outFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[8192];
        int n;

        while ((n = inputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, n);
        }

        fileOutputStream.flush();
        fileOutputStream.close();

        inputStream.close();
    }

    /**
     * Добавляем изображение в БД
     */
    private void addImageToDatabase(File file) {
        if (noteId == -1) {
            // На данный момент мы добавляем аттачи только в режиме редактирования
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(NotesContract.Images.COLUMN_PATH, file.getAbsolutePath());
        contentValues.put(NotesContract.Images.COLUMN_NOTE_ID, noteId);

        getContentResolver().insert(NotesContract.Images.URI, contentValues);
    }

}
