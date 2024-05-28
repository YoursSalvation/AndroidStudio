package com.example.lab2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter
import com.example.lab2.ui.theme.Lab2Theme
import kotlinx.coroutines.launch
import android.Manifest
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState

class MainActivity : ComponentActivity() {
    private val viewModel = ItemViewModel() //модель данных нашего списка
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //если в хранилище есть наш массив с языками программирования
        val dbHelper = LangsDbHelper(this) //создаем объект класса LangsDbHelper
        if (savedInstanceState!=null && savedInstanceState.containsKey("langs")) {
            //то в нашу модель переписываем эл-ты из savedInstanceState
            val tempLangArray = savedInstanceState.getSerializable("langs") as ArrayList<ProgrLang>
            viewModel.clearList()
            tempLangArray.forEach {
                viewModel.addLangToEnd(it)
            }
            Toast.makeText(this, "From saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "From create", Toast.LENGTH_SHORT).show()
            if (dbHelper!!.isEmpty()) { //если БД пустая
                println("DB is emty")
                var tempLangArray = ArrayList<ProgrLang>()//временный ArrayList для сохранения данных
                viewModel.langListFlow.value.forEach {//переносим данные из нашего основного массива
                    tempLangArray.add(it)
                }
                dbHelper!!.addArrayToDB(tempLangArray) //заносим в БД наш массив
                dbHelper!!.printDB() //и выводим в консоль для проверки
            } else { //иначе, если в БД есть записи
                println("DB has records")
                dbHelper!!.printDB() //выводим записи в консоль для проверки
                val tempLangArray = dbHelper!!.getLangsArray() //берем записи из БД в виде массива
                viewModel.clearList() //очищаем нашу модель данных
                tempLangArray.forEach {//и в цикле по массиву переносим данные в нашу модель
                    viewModel.addLangToEnd(it)
                }
            }
        }
        setContent {
            val lazyListState = rememberLazyListState() //объект для сохранения состояния списка
            Lab2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(Modifier.fillMaxSize()) { //создаем колонку
                        MakeAppBar(viewModel, lazyListState, dbHelper!!)
                        //MakeInputPart(viewModel, lazyListState)//вызываем ф-ию для создания полей ввода данных
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
fun MakeAppBar(model: ItemViewModel, lazyListState: LazyListState, dbHelper: LangsDbHelper) {
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
                dbHelper.addLang(newLang)
                scope.launch {//прокручиваем список, чтобы был виден добавленный элемент
                    lazyListState.scrollToItem(0)
                }
            }
        }

    if (openDialog.value) //если дочернее окно вызвано, то запускаем функцию для его создания
        MakeAlertDialog(context = mContext, dialogTitle = "About", openDialog = openDialog)
    val drawerStateObj = rememberDrawerState(initialValue = DrawerValue.Closed)
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
        },
        navigationIcon = { //описываем левую кнопку с навигацией (три горизонтальных полоски)
            IconButton( //кнопка с иконкой
                onClick = { //при нажатии на нее будет раскрываться или закрываться меню
                    scope.launch {
                        if (drawerStateObj.isClosed) drawerStateObj.open() //для открытия
                        else drawerStateObj.close() //для закрытия
                    }
                },
            ) {
                Icon( //для самой иконки
                    Icons.Rounded.Menu, //берем изображение из системных ресурсов
                    contentDescription = "" //можно добавить описание
                )
            }
        }
    )
    ModalNavigationDrawer( //это само боковое левое меню
        drawerState = drawerStateObj, //параметр, отвечающий за раскрытие меню, связываем с нашим объектом
        drawerContent = { //содержимое меню
            ModalDrawerSheet { //лист с меню
                Spacer(Modifier.height(12.dp)) //отступ
                NavigationDrawerItem( //пункт меню
                    icon = { Icon(Icons.Default.Star, contentDescription = null) }, //иконка для него
                    label = { Text("Drawing") }, //текст для него
                    selected = false, //выбран или нет (актуально, когда несколько эл-ов)
                    onClick = { //обработчик нажатия
                        scope.launch { drawerStateObj.close() } //закрываем меню
                        val newAct = Intent(mContext, DrawingActivity::class.java)//создаем намерение
                        mContext.startActivity(newAct) //и запускаем новое активити (описано ниже)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        content = { //а здесь содержимое нашего приложения, сюда переносим вызов метода MakeList
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MakeList(viewModel = model, lazyListState, dbHelper)
            }
        }
    )
}

@Composable
fun MakeList(viewModel: ItemViewModel, lazyListState: LazyListState, dbHelper: LangsDbHelper) {
    val langListState = viewModel.langListFlow.collectAsState()
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
                ListRow(item, langListState, viewModel, dbHelper)
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
fun ListRow(model: ProgrLang, langListState: State<List<ProgrLang>>, viewModel: ItemViewModel, dbHelper: LangsDbHelper){ //ф-ия для создания ряда с данными для LazyColumn
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false)} //по умолчанию – false, т.е. окно не вызвано
    val langSelected = remember { mutableStateOf("") } // и переменная для сохранения названия языка
    if (openDialog.value) //если дочернее окно (AlertDialog) вызвано
        MakeAlertDialog(context, langSelected.value, openDialog)
    var mDisplayMenu by remember { mutableStateOf(false) }
//объект для запуска деятельности выбора нового изображения
//запускается метод rememberLauncherForActivityResult с контрактом на получение данных
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
//res – результат выполнения метода
            if (res.data?.data != null) { //если была выбрана новая картинка
                println("image uri = ${res.data?.data}") //отладочный вывод (будет в разделе Run внизу IDE)
                val imgURI = res.data?.data //берем адрес картинки
                val index = langListState.value.indexOf(model) //получаем индекс текущего объекта в списке
                viewModel.changeImage(index, imgURI.toString())//и меняем картинку для нужного языка
                dbHelper!!.changeImgForLang(model.name, imgURI.toString())//меняем картинку в БД
            }
        }
    Row( //создаем ряд с данными
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.Blue)) //синяя граница для каждого эл-та списка
            .combinedClickable(
                onClick = {
                    println("item = ${model.name}")
                    langSelected.value = model.name
                    Toast
                        .makeText(context, "item = ${model.name}", Toast.LENGTH_LONG)
                        .show()
                    openDialog.value = true
                },
                onLongClick = { mDisplayMenu = true } //меняем значение объекта для меню
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text( // поле с текстом для названия языка
                text = model.name, //берем имя языка
                fontSize = 24.sp, //устанавливаем размер шрифта
                fontWeight = FontWeight.SemiBold, //делаем текст жирным
                //и добавляем отступ слева
                modifier = Modifier.padding(start = 20.dp)
            )
            Text( // поле с текстом для года создания языка
                text = model.surname, //берем год и преобразуем в строку
                fontSize = 20.sp, //устанавливаем размер шрифта
                modifier = Modifier.padding(10.dp), //добавляем отступ
                fontStyle = FontStyle.Italic //и делаем шрифт курсивом
            )
            Text( // поле с текстом для года создания языка
                text = model.form, //берем год и преобразуем в строку
                fontSize = 20.sp, //устанавливаем размер шрифта
                modifier = Modifier.padding(10.dp), //добавляем отступ
                fontStyle = FontStyle.Italic //и делаем шрифт курсивом
            )
        }
        DropdownMenu(//создаем контекстное меню
            expanded = mDisplayMenu, //связываем его св-во, отвечающее за показ меню, с объектом
            onDismissRequest = { mDisplayMenu = false }
        ) {
            DropdownMenuItem( //вставляем нужный пункт меню
                text = { Text(text = "Поменять картинку", fontSize = 20.sp) },
                onClick = { //обрабатываем нажатие на него
                    mDisplayMenu = !mDisplayMenu //меняем объект, отвечающий за открытие меню
                    //получаем разрешение на чтение внешних ресурсов
                    val permission: String = Manifest.permission.READ_EXTERNAL_STORAGE
                    val grant = ContextCompat.checkSelfPermission(context, permission)
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        val permission_list = arrayOfNulls<String>(1)
                        permission_list[0] = permission
                        ActivityCompat.requestPermissions(context as Activity, permission_list,1)
                    }
//создаем намерение на получение внешнего объекта в виде картинки
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                        addCategory(Intent.CATEGORY_OPENABLE) }
                    launcher.launch(intent) //стартуем объект для получения картинки
                }
            )
        }
        Image(//нужен import androidx.compose.foundation.Image
            painter = if (pictureIsInt(model.picture)) painterResource(model.picture.toInt())
            else rememberImagePainter(model.picture),
            contentDescription = "", //можно вставить описание изображения
            contentScale = ContentScale.Fit, //параметры масштабирования изображения
            modifier = Modifier.size(90.dp)
        )
    }
}
fun pictureIsInt(picture: String): Boolean{ //ф-ия для проверки источника изображения
// переменной data присваиваем результат блока try … catch
    var data = try { //пробуем перевести строку с ресурсом картинки в число, т.к. внутренние
        picture.toInt() //ресурсы приложения хранятся в виде числового id
    } catch (e:NumberFormatException){ //если строка не переводится в число, то значит это
        null //изображение из внешних ресурсов и присваиваем null
    } //в результате data будет равна либо picture.toInt(), либо null
    return data!=null //результат ф-ии зависит от значения переменной data
}
