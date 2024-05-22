package com.example.lab2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LangsDbHelper (context: Context) : //наш класс для работы с БД, наследуется от
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){ //стандартного класса
    companion object{ // тут прописываем переменные для БД
        private val DATABASE_NAME = "COUNTRIES" //имя БД
        private val DATABASE_VERSION = 1 // версия
        val TABLE_NAME = "countries_table" // имя таблицы
        val ID_COL = "id" // переменная для поля id
        val NAME_COl = "country_name" // переменная для поля lang_name
        val SURNAME_COL = "country_surname" // переменная для поля surname
        val FORM_COL = "country_form"
        val PICTURE_COL = "picture" // переменная для поля picture
    }
    override fun onCreate(db: SQLiteDatabase) { //метод для создания таблицы через SQL-запрос
        val query = ("CREATE TABLE " + TABLE_NAME + " (" //конструируем запрос через
                + ID_COL + " INTEGER PRIMARY KEY autoincrement, " + //созданные выше
                NAME_COl + " TEXT," + //переменные
                SURNAME_COL + " TEXT," + FORM_COL + " TEXT, " + PICTURE_COL + " TEXT" + ")")
        db.execSQL(query) // выполняем SQL-запрос
    }
    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {//метод для обновления БД
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }
    fun getCurcor(): Cursor? { // метод для получения всех записей таблицы БД в виде курсора
        val db = this.readableDatabase // получаем ссылку на БД только для чтения
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null) //возвращаем курсор в виде
    } //результата выборки всех записей из нашей таблицы
    fun isEmpty(): Boolean { //метод для проверки БД на отсутствие записей
        val cursor = getCurcor() //получаем курсор таблицы БД с записями
        return !cursor!!.moveToFirst() //и возвращаем результат перехода к первой записи,
    } //инвертируя его, т.е. если нет записей, cursor!!.moveToFirst() вернет false, отрицание его
    //даст true
    fun printDB(){ //метод для печати БД в консоль
        val cursor = getCurcor() //получаем курсор БД
        if (!isEmpty()) { //если БД не пустая
            cursor!!.moveToFirst() //переходим к первой записи
            val nameColIndex = cursor.getColumnIndex(NAME_COl) //получаем индексы для колонок
            val surnameColIndex = cursor.getColumnIndex(SURNAME_COL) //с нужными данными
            val formColIndex = cursor.getColumnIndex(FORM_COL)
            val pictureColIndex = cursor.getColumnIndex(PICTURE_COL)
            do { //цикл по всем записям
                print("${cursor.getString(nameColIndex)} ") //печатаем данные поля с именем
                print("${cursor.getString(surnameColIndex)} ") //поля с годом
                print("${cursor.getString(formColIndex)} ")
                println("${cursor.getString(pictureColIndex)} ") //поля с картинкой
            } while (cursor.moveToNext()) //пока есть записи
        } else println("DB is empty") //иначе печатаем, что БД пустая
    }
    fun addArrayToDB(progLangs: ArrayList<ProgrLang>){ //метод для добавления целого массива в БД
        progLangs.forEach { //цикл по всем элементам массива
            addLang(it) //добавляем элемент массива в БД
        }
    }

    fun addLang(lang: ProgrLang){ // метод для добавления языка в БД
        val values = ContentValues() // объект для создания значений, которые вставим в БД
        values.put(NAME_COl, lang.name) // добавляем значения в виде пары ключ-значение
        values.put(SURNAME_COL, lang.surname)
        values.put(FORM_COL, lang.form)
        values.put(PICTURE_COL, lang.picture)
        val db = this.writableDatabase //получаем ссылку для записи в БД
        db.insert(TABLE_NAME, null, values) // вставляем все значения в БД в нашу таблицу
        db.close() // закрываем БД (для записи)
    }
    fun changeImgForLang(name: String, img: String){ // метод для изменения картинки для языка
        val db = this.writableDatabase //получаем ссылку для записи в БД
        val values = ContentValues() // объект для изменения записи
        values.put(PICTURE_COL, img) // вставляем новую картинку
//и делаем запрос в БД на изменение поля с нужным названием в нашей таблице
        db.update(TABLE_NAME, values, NAME_COl+" = '$name'", null)
        db.close() // закрываем БД (для записи)
    }
    fun getLangsArray(): ArrayList<ProgrLang>{ // метод для получения данных из таблицы в виде
//массива
        var progsArray = ArrayList<ProgrLang>() //массив, в который запишем данные
        val cursor = getCurcor() //получаем курсор таблицы БД
        if (!isEmpty()) { //если БД не пустая
            cursor!!.moveToFirst() //переходим к первой записи
            val nameColIndex = cursor.getColumnIndex(NAME_COl) //получаем индексы для колонок
            val surnameColIndex = cursor.getColumnIndex(SURNAME_COL) //с нужными данными
            val formColIndex = cursor.getColumnIndex(FORM_COL)
            val pictureColIndex = cursor.getColumnIndex(PICTURE_COL)
            do { //цикл по всем записям
                val name = cursor.getString(nameColIndex) //получаем данные полей
                val surname = cursor.getString(surnameColIndex) //и записываем их в переменные
                val form = cursor.getString(formColIndex)
                val picture = cursor.getString(pictureColIndex)
                progsArray.add(ProgrLang(name, surname, form, picture)) //и создаем объект с этими данными
            } while (cursor.moveToNext()) //пока есть записи
        } else println("DB is empty") //иначе пишем, что БД пустая
        return progsArray //возвращаем созданный массив
    }
}