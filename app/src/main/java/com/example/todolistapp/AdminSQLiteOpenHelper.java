package com.example.todolistapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelper(
            @Nullable Context context,
            @Nullable String name,
            @Nullable SQLiteDatabase.CursorFactory factory,
            int version) {

        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Agregamos la columna 'cantidad' aquí
        String query =
                "CREATE TABLE tareas (" +
                        "id INTEGER PRIMARY KEY," +
                        "titulo TEXT," +
                        "descripcion TEXT," +
                        "cantidad INTEGER," +
                        "estado TEXT)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Al incrementar la versión en el MainActivity, esto borrará la tabla vieja y creará la nueva con el campo cantidad
        db.execSQL("DROP TABLE IF EXISTS tareas");
        onCreate(db);
    }
}