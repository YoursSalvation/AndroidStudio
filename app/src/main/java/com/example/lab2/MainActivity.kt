package com.example.lab2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab2.ui.theme.Lab2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel = ItemViewModel() //модель данных нашего списка
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //если в хранилище есть наш массив с языками программирования
        if (savedInstanceState!=null && savedInstanceState.containsKey("langs")) {
            //то в нашу модель переписываем эл-ты из savedInstanceState
            val tempLangArray = savedInstanceState.getSerializable("langs") as ArrayList<ProgrLang>
            viewModel.clearList()
            tempLangArray.forEach {
                viewModel.addLangToEnd(it)
            }
            Toast.makeText(this, "From saved", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(this, "From create", Toast.LENGTH_SHORT).show()
        setContent {
            val lazyListState = rememberLazyListState() //объект для сохранения состояния списка
            Lab2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(Modifier.fillMaxSize()) { //создаем колонку
                        MakeAppBar(viewModel, lazyListState)
                        //MakeInputPart(viewModel, lazyListState)//вызываем ф-ию для создания полей ввода данных
                        MakeList(viewModel, lazyListState) //вызываем ф-ию для самого списка с данными
                    }
                }
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show() //сообщение для отслеживания
        var tempLangArray = ArrayList<ProgrLang>() //временный ArrayList для сохранения данных
        viewModel.langListFlow.value.forEach {//переносим данные из нашего основного массива
            tempLangArray.add(it)
        }
        outState.putSerializable("langs", tempLangArray) //помещаем созданный массив в хранилище
        //и даем ему метку langs, по ней потом его и найдем
        super.onSaveInstanceState(outState) //вызов метода базового класса
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeAppBar(model: ItemViewModel, lazyListState: LazyListState) {
//создаем объект для хранения состояния меню – открыто (true) или нет (false)
    var mDisplayMenu by remember { mutableStateOf(false) }
    val mContext = LocalContext.current // контекст нашего приложения
    val openDialog = remember { mutableStateOf(false) } //объект для состояния дочернего окна
    val scope = rememberCoroutineScope()//объект для прокручивания списка при вставке нового эл-та
    val startForResult = //переменная-объект класса ManagedActivityResultLauncher,
//ей присваиваем результат вызова метода rememberLauncherForActivityResult
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
//внутри метода смотрим результат работы запущенного активити – если закрытие с кодом RESULT_OK
            if (result.resultCode == Activity.RESULT_OK) {//то берем объект из его данных
                val newLang = result.data?.getSerializableExtra("newItem") as ProgrLang //как язык
                println("new lang name = ${newLang.name}") //вывод для отладки
                model.addLangToHead(newLang)
                scope.launch {//прокручиваем список, чтобы был виден добавленный элемент
                    lazyListState.scrollToItem(0)
                }
            }
        }

    if (openDialog.value) //если дочернее окно вызвано, то запускаем функцию для его создания
        MakeAlertDialog(context = mContext, dialogTitle = "About", openDialog = openDialog)
    TopAppBar( //создаем верхнюю панель нашего приложения, в нем будет меню
        title = { Text("Страны") }, //заголовок в верхней панели
        actions = { //здесь разные действия можно прописать, например, иконку для меню
            IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) { //создаем иконку
                Icon(Icons.Default.MoreVert, null) //в виде трех вертикальных точек
            } //в методе onClick прописано изменение объекта для хранения состояния меню
            DropdownMenu( //создаем меню
                expanded = mDisplayMenu, //признак, открыто оно или нет
                onDismissRequest = { mDisplayMenu = false } //при закрытии меню устанавливаем
//соответствующее значение объекту mDisplayMenu
            ) {
                DropdownMenuItem( //создаем пункт меню для вызова информации о программе (About)
                    text = { Text(text = "About") }, //его текст
                    onClick = { //и обработчик нажатия на него
//всплывающее сообщение с названием пункта
                        Toast.makeText(mContext, "About", Toast.LENGTH_SHORT).show()

                        mDisplayMenu =
                            !mDisplayMenu //меняем параметр, отвечающий за состояние меню,
                        openDialog.value =
                            true //и параметр, отвечающий за состояние дочернего окна,
                    } //в котором выводим доп. информацию
                )
//создаем второй пункт меню для вызова окна, в которое перенесли ввод нового языка
                DropdownMenuItem(
                    text = { Text(text = "Add country") }, //его текст
                    onClick = { //и обработчик нажатия на него
                        Toast.makeText(mContext, "Add country", Toast.LENGTH_SHORT).show()
//создаем Intent – намерение, специальный объект для вызова нового окна приложения, сам класс
                        val newAct = Intent(mContext, InputActivity::class.java) //описан ниже
                        startForResult.launch(newAct)
                        mDisplayMenu = !mDisplayMenu //и меняем признак открытия меню
                    }
                )
            }
        }
    )
}

@Composable
fun MakeList(viewModel: ItemViewModel, lazyListState: LazyListState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        state = lazyListState //состояние списка соотносим с переданным объектом
    ) {
        items(
            items = viewModel.langListFlow.value,
            key = { lang -> lang.name },
            itemContent = { item ->
                ListRow(item)
            }
        )
    }
}

@Composable
fun MakeAlertDialog(context: Context, dialogTitle: String, openDialog: MutableState<Boolean>) {
//создаем переменную, в ней будет сохраняться текст, полученный из строковых ресурсов для выбранного языка
    var strValue = remember{ mutableStateOf("") } //для получения значения строки из ресурсов
//получаем id нужной строки из ресурсов через имя в dialogTitle
    val strId = context.resources.getIdentifier(dialogTitle, "string", context.packageName)
//секция try..catch нужна для обработки ошибки Resources.NotFoundException – отсутствие искомого ресурса
    try{ //если такой ресурс есть (т.е. его id не равен 0), то берем само значение этого ресурса
        if (strId != 0) strValue.value = context.getString(strId)
    } catch (e: Resources.NotFoundException) {
        //если произошла ошибка Resources.NotFoundException, то ничего не делаем
    }
    AlertDialog( // создаем AlertDialog
        onDismissRequest = { openDialog.value = false },//действия при закрытии окна
        title = { Text(text = dialogTitle) }, //заголовок окна
        text = { Text(text = strValue.value, fontSize = 20.sp) },//содержимое окна
        confirmButton = { //кнопка Ok, которая будет закрывать окно
            Button(onClick = { openDialog.value = false })
            { Text(text = "OK") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListRow(item: ProgrLang){ //ф-ия для создания ряда с данными для LazyColumn
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false)} //по умолчанию – false, т.е. окно не вызвано
    val langSelected = remember { mutableStateOf("") } // и переменная для сохранения названия языка
    if (openDialog.value) //если дочернее окно (AlertDialog) вызвано
        MakeAlertDialog(context, langSelected.value, openDialog)
    Row( //создаем ряд с данными
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.Blue)) //синяя граница для каждого эл-та списка
            .combinedClickable(
                onClick = {
                    println("item = ${item.name}")
                    langSelected.value = item.name
                    Toast
                        .makeText(context, "item = ${item.name}", Toast.LENGTH_LONG)
                        .show()
                    openDialog.value = true
                }
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text( // поле с текстом для названия языка
                text = item.name, //берем имя языка
                fontSize = 24.sp, //устанавливаем размер шрифта
                fontWeight = FontWeight.SemiBold, //делаем текст жирным
                //и добавляем отступ слева
                modifier = Modifier.padding(start = 20.dp)
            )
            Text( // поле с текстом для года создания языка
                text = item.surname, //берем год и преобразуем в строку
                fontSize = 20.sp, //устанавливаем размер шрифта
                modifier = Modifier.padding(10.dp), //добавляем отступ
                fontStyle = FontStyle.Italic //и делаем шрифт курсивом
            )
            Text( // поле с текстом для года создания языка
                text = item.form, //берем год и преобразуем в строку
                fontSize = 20.sp, //устанавливаем размер шрифта
                modifier = Modifier.padding(10.dp), //добавляем отступ
                fontStyle = FontStyle.Italic //и делаем шрифт курсивом
            )
        }
        Image(//нужен import androidx.compose.foundation.Image
            painter = painterResource(id = item.picture), //указываем источник изображения
            contentDescription = "", //можно вставить описание изображения
            contentScale = ContentScale.Fit, //параметры масштабирования изображения
            modifier = Modifier.size(90.dp)
        )
    }
}