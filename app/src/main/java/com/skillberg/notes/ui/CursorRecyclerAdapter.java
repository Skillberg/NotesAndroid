package com.skillberg.notes.ui;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.skillberg.notes.db.NotesContract;

/**
 * Адаптер для RecyclerView, работающий с Cursor
 */
public abstract class CursorRecyclerAdapter<ViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<ViewHolder> {

    protected Cursor cursor; // Курсор
    protected boolean isDataValid; // Валидны ли данные
    protected int idColumnIndex; // Индекс столбца ID в курсоре

    public CursorRecyclerAdapter(Cursor cursor) {
        super();

        this.cursor = cursor;

        // Данные корректны если курсор не null
        isDataValid = cursor != null;

        // Пытаемся получить индекс столбца ID, если курсор не null, в ином случае -1
        idColumnIndex = cursor != null
                ? cursor.getColumnIndexOrThrow(NotesContract.Notes._ID)
                : -1;

        // Каждый элемент имеет уникальный ID
        setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // Если данные некорректны — кидаем исключение
        if (!isDataValid) {
            throw new IllegalStateException("Cursor is not valid!");
        }

        // Попробовали перейти к определённой строке, но это не получилось
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("Can not move to position " + position);
        }

        // Вызываем новый метод
        onBindViewHolder(viewHolder, cursor);
    }

    public abstract void onBindViewHolder(ViewHolder viewHolder, Cursor cursor);

    @Override
    public int getItemCount() {
        if (isDataValid && cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {

        // Если с данными всё хорошо и есть курсор
        if (isDataValid && cursor != null) {

            // Если смогли найти нужную строку в курсоре
            if (cursor.moveToPosition(position)) {

                // Возвращаем значение столбца ID
                return cursor.getLong(idColumnIndex);
            }
        }

        // Во всех остальных случаях возвращаем дефолтное значение
        return RecyclerView.NO_ID;
    }

    /**
     * Заменяет старый курсор новым
     *
     * @param newCursor Новый курсор
     * @return Старый курсор или null
     */
    @Nullable
    public Cursor swapCursor(Cursor newCursor) {
        // Если курсор не изменился — ничего не заменяем
        if (newCursor == this.cursor) {
            return null;
        }

        Cursor oldCursor = this.cursor;
        this.cursor = newCursor;

        if (newCursor != null) {
            idColumnIndex = newCursor.getColumnIndexOrThrow(NotesContract.Notes._ID);
            isDataValid = true;

            notifyDataSetChanged();
        } else {
            idColumnIndex = -1;
            isDataValid = false;

            // Сообщаем, что данных в адаптере больше нет
            notifyItemRangeRemoved(0, getItemCount());
        }

        return oldCursor;
    }


}
