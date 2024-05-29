package com.example.lab2

import android.graphics.BitmapFactory
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

class DrawingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val buttonNames = arrayOf(
                stringResource(R.string.rect),
                stringResource(R.string.circle),
                stringResource(R.string.image),
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
@Composable
fun MakeTopButtons(buttonNames: Array<String>, myView: MyGraphView?) {
    val openWidth = remember { mutableStateOf(false) } //объект для состояния дочернего окна
    val openColor = remember { mutableStateOf(false) } //объект для состояния дочернего окна
    val openStyle = remember { mutableStateOf(false) } //объект для состояния дочернего окна
    val openSave = remember { mutableStateOf(false) } //объект для состояния дочернего окна

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