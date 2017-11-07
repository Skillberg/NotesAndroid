package com.skillberg.notes.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Notes content provider
 */
public class NotesProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int NOTES = 1;
    private static final int NOTE = 2;

    private static final int IMAGES = 3;
    private static final int IMAGE = 4;

    static {
        URI_MATCHER.addURI(NotesContract.AUTHORITY, "notes", NOTES);
        URI_MATCHER.addURI(NotesContract.AUTHORITY, "notes/#", NOTE);

        URI_MATCHER.addURI(NotesContract.AUTHORITY, "images", IMAGES);
        URI_MATCHER.addURI(NotesContract.AUTHORITY, "images/#", IMAGE);
    }

    private NotesDbHelper notesDbHelper;

    @Override
    public boolean onCreate() {
        notesDbHelper = new NotesDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = notesDbHelper.getReadableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case NOTES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = NotesContract.Notes.COLUMN_UPDATED_TS + " DESC";
                }

                return db.query(NotesContract.Notes.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

            case NOTE:
                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    selection = NotesContract.Notes._ID + " = ?";
                    selectionArgs = new String[]{id};
                } else {
                    selection = selection + " AND " + NotesContract.Notes._ID + " = ?";

                    String[] newSelectionArgs = new String[selectionArgs.length + 1];

                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgs.length);

                    newSelectionArgs[newSelectionArgs.length - 1] = id;

                    selectionArgs = newSelectionArgs;
                }

                return db.query(NotesContract.Notes.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

            case IMAGES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = NotesContract.Images._ID + " ASC";
                }

                return db.query(NotesContract.Images.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

            default:
                return null;
        }

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case NOTES:
                return NotesContract.Notes.URI_TYPE_NOTE_DIR;

            case NOTE:
                return NotesContract.Notes.URI_TYPE_NOTE_ITEM;

            case IMAGES:
                return NotesContract.Images.URI_TYPE_IMAGE_DIR;

            case IMAGE:
                return NotesContract.Images.URI_TYPE_IMAGE_ITEM;

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = notesDbHelper.getWritableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case NOTES:
                long rowId = db.insert(NotesContract.Notes.TABLE_NAME,
                        null,
                        contentValues);

                if (rowId > 0) {
                    Uri noteUri = ContentUris.withAppendedId(NotesContract.Notes.URI, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);

                    return noteUri;
                }

                return null;

            case IMAGES:
                long imageRowId = db.insert(NotesContract.Images.TABLE_NAME,
                        null,
                        contentValues);

                if (imageRowId > 0) {
                    Uri imageUri = ContentUris.withAppendedId(NotesContract.Images.URI, imageRowId);
                    getContext().getContentResolver().notifyChange(uri, null);

                    return imageUri;
                }

                return null;

            default:
                return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = notesDbHelper.getWritableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case NOTE:
                String noteId = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    selection = NotesContract.Notes._ID + " = ?";
                    selectionArgs = new String[]{noteId};
                } else {
                    selection = selection + " AND " + NotesContract.Notes._ID + " = ?";
                    String[] newSelectionArgs = new String[selectionArgs.length + 1];
                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgs.length);
                    newSelectionArgs[newSelectionArgs.length - 1] = noteId;
                    selectionArgs = newSelectionArgs;
                }

                int noteRowsUpdated = db.delete(NotesContract.Notes.TABLE_NAME, selection, selectionArgs);

                getContext().getContentResolver().notifyChange(uri, null);

                return noteRowsUpdated;

            case IMAGE:
                String imageId = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    selection = NotesContract.Images._ID + " = ?";
                    selectionArgs = new String[]{imageId};
                } else {
                    selection = selection + " AND " + NotesContract.Images._ID + " = ?";
                    String[] newSelectionArgs = new String[selectionArgs.length + 1];
                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgs.length);
                    newSelectionArgs[newSelectionArgs.length - 1] = imageId;
                    selectionArgs = newSelectionArgs;
                }

                int imageRowsUpdated = db.delete(NotesContract.Images.TABLE_NAME, selection, selectionArgs);

                getContext().getContentResolver().notifyChange(uri, null);

                return imageRowsUpdated;
        }

        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = notesDbHelper.getWritableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case NOTE:
                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    selection = NotesContract.Notes._ID + " = ?";
                    selectionArgs = new String[]{id};
                } else {
                    selection = selection + " AND " + NotesContract.Notes._ID + " = ?";

                    String[] newSelectionArgs = new String[selectionArgs.length + 1];

                    System.arraycopy(selectionArgs, 0, newSelectionArgs, 0, selectionArgs.length);

                    newSelectionArgs[newSelectionArgs.length - 1] = id;

                    selectionArgs = newSelectionArgs;
                }

                int rowsUpdated = db.update(NotesContract.Notes.TABLE_NAME, contentValues, selection, selectionArgs);

                getContext().getContentResolver().notifyChange(uri, null);

                return rowsUpdated;
        }


        return 0;
    }
}
