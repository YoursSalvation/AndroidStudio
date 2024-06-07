package com.example.lab2

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.lab2.ui.theme.Lab2Theme
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.provider.MediaStore
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class DrawingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val buttonNames = arrayOf(
                stringResource(R.string.rect),
                stringResource(R.string.circle),
                //stringResource(R.string.image),
                //stringResource(R.string.save) //добавляем надпись для кнопки Save
            )
// создаем наш объект для рисования
            val myView: MyGraphView? = MyGraphView(applicationContext)
            val viewRemember = remember { //и объект для его хранения
                mutableStateOf(myView)
            }
            Lab2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(Modifier.fillMaxSize()) {
//вызываем функцию для создания кнопок, ей передаем массив с именами и объект для рисования
                        MakeTopButtons(buttonNames, viewRemember.value)
//и вызываем функцию с нашим объектом для рисования, где и будет всё отображаться
                        CustomView(viewRemember.value)
                    }
                }
            }
        }
    }
}
fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap {
    return BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
}
@Composable
fun MakeTopButtons(buttonNames: Array<String>, myView: MyGraphView?) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
//res – результат выполнения метода
            println("Inlauncher")
            if (res.data?.data != null) { //если была выбрана новая картинка
                //println("LAUNCHER")
                println("image uri = ${res.data?.data}") //отладочный вывод (будет в разделе Run внизу IDE)
                val imgURI = res.data?.data //берем адрес картинки
                myView?.drawFace(imgURI)
            }
        }

    val openWidth = remember { mutableStateOf(false) } //объект для состояния дочернего окна
    val openColor = remember { mutableStateOf(false) } //объект для состояния дочернего окна
    val openStyle = remember { mutableStateOf(false) } //объект для состояния дочернего окна
    val openSave = remember { mutableStateOf(false) } //объект для состояния дочернего окна
    val openImage = remember { mutableStateOf(false) }

    if (openWidth.value) {
        myView?.ShowLineWidthPicker(openWidth)
    }
    if (openColor.value) {
        myView?.ShowColorPicker(openColor)
    }
    if (openStyle.value) {
        myView?.ShowStylePicker(openStyle)
    }
    if (openSave.value) {
        myView?.onSaveClick(openSave)
    }
    if (openImage.value) {
        val context = LocalContext.current //получаем текущий контекст
        var mDisplayMenu by remember { mutableStateOf(false) }

        val permission: String = Manifest.permission.READ_EXTERNAL_STORAGE
        val grant = ContextCompat.checkSelfPermission(context, permission)
        if (grant != PackageManager.PERMISSION_GRANTED) {
            val permission_list = arrayOfNulls<String>(1)
            permission_list[0] = permission
            ActivityCompat.requestPermissions(context as Activity, permission_list, 1)
        }

        val intent = Intent(

            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            println("dsdf")
        }
        launcher.launch(intent)
        openImage.value=false


    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.Blue))
    ) {
        buttonNames.forEach {
            Button(onClick = { //цикл по названиям кнопок
//и вызываем из массива функций нужный метод согласно номеру кнопки в массиве
                myView!!.funcArray[buttonNames.lastIndexOf(it)]()
            }) {
                Text(text = it) //текст для каждой кнопки
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.Blue))
    ) {

        Button(onClick = { //цикл по названиям кнопок
//и вызываем из массива функций нужный метод согласно номеру кнопки в массиве
            openSave.value = true
        }) {
            Text(
                text = stringResource(R.string.save)
            ) //текст для каждой кнопки
        }
        Button(onClick = {
            openWidth.value = true
        }) {
            Text(text =  stringResource(R.string.width)) //текст для каждой кнопки
        }
        Button(onClick = {
            openColor.value = true
        }) {
            Text(text =  stringResource(R.string.color)) //текст для каждой кнопки
        }
        Button(onClick = {
            openStyle.value = true
        }) {
            Text(text =  stringResource(R.string.style)) //текст для каждой кнопки
        }

    }
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.Blue))
    ) {
        Button(onClick = {
            myView?.drawfam()
        }) {
            Text(text =  stringResource(R.string.fam)) //текст для каждой кнопки
        }
        Button(onClick = {
            openImage.value=true
        }) {
            Text(text =  stringResource(R.string.image)) //текст для каждой кнопки
        }
    }
}
@Composable
fun CustomView(myView: MyGraphView?) { //функция для вставки нашего объекта для рисования
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            myView!! //сам объект
        },
    )
}