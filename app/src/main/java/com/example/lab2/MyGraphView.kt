package com.example.lab2

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.widget.Toast
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout. *
import androidx.compose.material. *
import androidx.compose.runtime. *
import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.*
import android.net.Uri
import android.provider.MediaStore
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.app.Activity

class MyGraphView(context: Context?) : View(context) {
    private lateinit var path: Path
    private var mPaint: Paint? = null //объект для параметров рисования графических примитивов
    private var mBitmapPaint: Paint? = null //объект для параметров вывода битмапа на холст
    private var mBitmap : Bitmap? = null //сам битмап
    private var mCanvas: Canvas? = null //холст
    init { //секция инициализации полей класса
//создаем объект класса Paint для параметров вывода битмапа на холст
        mBitmapPaint = Paint(Paint.DITHER_FLAG) // Paint.DITHER_FLAG – для эффекта сглаживания
        mPaint = Paint()//создаем объект класса Paint для параметров рисования графики
        mPaint!!.setAntiAlias(true) //устанавливаем антиалиасинг (сглаживание)
        mPaint?.setColor(Color.GREEN) //цвет рисования
        mPaint?.setStyle(Paint.Style.STROKE) //стиль рисования
//(Paint.Style.STROKE – без заполнения)
        mPaint?.setStrokeJoin(Paint.Join.ROUND) //стиль соединения линий (ROUND - скруглённый)
        mPaint?.setStrokeCap(Paint.Cap.ROUND) //стиль концов линий (ROUND - скруглённый)
        mPaint?.setStrokeWidth(12F) //толщина линии рисования
    }
    //метод onSizeChanged вызывается первый раз при создании объекта,
//далее – при изменении размера объекта, нам он нужен для выяснения первичных размеров битмапа
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
//создаем битмап с высотой и шириной как у текущего объекта и с параметром
        Bitmap.Config.ARGB_8888
//это четырехканальный RGB (прозрачность и 3 цвета)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!) //создаем канвас и связываем его с битмапом
        Toast.makeText(this.context, "onSizeChanged ", Toast.LENGTH_SHORT).show() //для отладки
    }
    //метод перерисовки объекта, он будет срабатывать каждый раз
    override fun onDraw(canvas: Canvas) { //при вызове функции invalidate() текущего объекта
        super.onDraw(canvas)
//отрисовываем на канвасе текущего объекта (не путать с созданным нами канвасом) наш битмап
        canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint!!)
    }
    fun drawfam() { //метод для рисования круга
        val mBitmapFromSdcard = BitmapFactory.decodeFile("/storage/emulated/0/Android/data/com.example.lab2/files/fam.png")
        mCanvas!!.drawBitmap(mBitmapFromSdcard, 100f, 100f, mPaint) //рисуем его на нашем канвасе
        invalidate() //для срабатывания метода onDraw
    }
    fun drawCircle() { //метод для рисования круга
        println("mCanvas = $mCanvas")
        mCanvas!!.drawCircle(100f, 100f, 50f, mPaint!!)
        invalidate() //для срабатывания метода onDraw
    }
    fun drawSquare() { //метод для рисования квадрата
        println("mCanvas = $mCanvas")
        mCanvas!!.drawRect(200f, 200f, 300f, 300f, mPaint!!)
        invalidate()
    }
    fun Context.getMyContentResolver(): ContentResolver {
        return this.contentResolver
    }
    fun drawFace(imgUri: Uri?) { //метод для рисования картинки из файла
//создаем временный битмап из файла
        val contentResolver = context.getMyContentResolver()
        val bitmap = MediaStore.Images.Media.getBitmap(this.context.contentResolver, imgUri)

        //val mBitmapFromSdcard = BitmapFactory.decodeFile("/mnt/sdcard/newpicture.jpg")
        // val bitmap = getBitmapFromUri(LocalContext.current.contentResolver, imgUri)
        mCanvas!!.drawBitmap(bitmap, 100f, 100f, mPaint) //рисуем его на нашем канвасе
        invalidate()
    }
    @Composable
    fun onSaveClick(openDialog: MutableState<Boolean>) { // метод для сохранения нарисованного
//получаем путь к каталогу программы на карте памяти (для этого проекта -
// /storage/emulated/0/Android/data/com.example.composeexample/files)

        if (openDialog.value){
            var text by remember { mutableStateOf("myPNG") }
            AlertDialog( // создаем AlertDialog
                onDismissRequest = { openDialog.value = false },//действия при закрытии окна
                title = { Text(text = stringResource(R.string.save), onTextLayout = {}) }, //заголовок окна
                text = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                    )
                },//содержимое окна
                confirmButton = { //кнопка Ok, которая будет закрывать окно
                    Button(onClick = {
                        val destPath: String = context.getExternalFilesDir(null)!!.absolutePath
                        var outStream: OutputStream? = null //объявляем поток вывода
                        val file = File(destPath, "${text}.PNG") //создаем файл с нужным путем и названием
                        println("path = $destPath") //вывод в консоль для отладки
                        outStream = FileOutputStream(file) //создаем объект потока и связываем его с файлом
//у нашего битмапа вызываем функцию для записи его с нужными параметрами (тип графического файла,
//качество в процентах и поток для записи)
                        mBitmap!!.compress(Bitmap.CompressFormat.PNG, 100,
                            outStream as FileOutputStream
                        )
                        (outStream as FileOutputStream).flush() //для прохождения данных вызываем функцию flush у потока
                        (outStream as FileOutputStream).close() //закрываем поток
                        openDialog.value = false
                    })
                    { Text(text = "OK", onTextLayout = {}) }
                }
            )

        }

    }

    @Composable
    fun ShowImagePicker(
        openDialog: MutableState<Boolean>,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) {


        val context = LocalContext.current //получаем текущий контекст

        var mDisplayMenu by remember { mutableStateOf(false) }

        val permission: String = Manifest.permission.READ_EXTERNAL_STORAGE
        val grant = ContextCompat.checkSelfPermission(context, permission)
        if (grant != PackageManager.PERMISSION_GRANTED) {
            val permission_list = arrayOfNulls<String>(1)
            permission_list[0] = permission
            ActivityCompat.requestPermissions(context as Activity, permission_list, 1)
        }



        // openDialog.value = !openDialog.value
        mDisplayMenu = !mDisplayMenu


//создаем намерение на получение внешнего объекта в виде картинки
        val intent = Intent(

            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            println("dsdf")
        }
        launcher.launch(intent)
        println("LAUNCHERLFJD")

    }

    @Composable
    fun ShowLineWidthPicker(openDialog: MutableState<Boolean>) {
        var lineWidth by remember { mutableStateOf(1f) }
        if (openDialog.value) {
            AlertDialog( // создаем AlertDialog
                onDismissRequest = { openDialog.value = false },//действия при закрытии окна
                title = { Text(text = stringResource(R.string.chosewidth), onTextLayout = {}) }, //заголовок окна
                text = {
                    Slider(
                        value = lineWidth,
                        onValueChange = { lineWidth = it },
                        steps = 100,
                        valueRange = 1f..20f
                    )
                    Text(
                        text = stringResource(R.string.tempWidth)+": ${lineWidth.toInt()}px",
                        fontWeight = FontWeight.Bold
                    )
                },//содержимое окна

                confirmButton = { //кнопка Ok, которая будет закрывать окно
                    Button(onClick = {
                        onConfirm(lineWidth)
                        openDialog.value = false
                    })
                    { Text(text = "OK", onTextLayout = {}) }
                }
            )
        }
    }

    @Composable
    fun ShowColorPicker(openDialog: MutableState<Boolean>) {
        if (openDialog.value) {
            AlertDialog( // создаем AlertDialog
                onDismissRequest = { openDialog.value = false },//действия при закрытии окна
                title = { Text(text = stringResource(R.string.color), onTextLayout = {}) }, //заголовок окна
                text = {
                    Column {
                        Row {
                            OutlinedButton(
                                onClick = {
                                    mPaint?.setColor(Color.GREEN) //цвет рисования
                                    openDialog.value = false
                                },
                            ) {
                                Text(text = stringResource(R.string.green), onTextLayout = {})
                            }
                            OutlinedButton(onClick = {
                                mPaint?.setColor(Color.RED) //цвет рисования
                                openDialog.value = false
                            })
                            { Text(text = stringResource(R.string.red), onTextLayout = {}) }

                        }
                        Row {
                            OutlinedButton(onClick = {
                                mPaint?.setColor(Color.YELLOW) //цвет рисования
                                openDialog.value = false
                            })
                            { Text(text = stringResource(R.string.yellow), onTextLayout = {}) }
                            OutlinedButton(onClick = {
                                mPaint?.setColor(Color.BLUE) //цвет рисования
                                openDialog.value = false
                            })
                            { Text(text = stringResource(R.string.blue), onTextLayout = {}) }
                        }
                    }


                },//содержимое окна

                confirmButton = { //кнопка Ok, которая будет закрывать окно

                }
            )
        }
    }

    @Composable
    fun ShowStylePicker(openDialog: MutableState<Boolean>) {
        if (openDialog.value) {
            AlertDialog( // создаем AlertDialog
                onDismissRequest = { openDialog.value = false },//действия при закрытии окна
                title = { Text(text = stringResource(R.string.styles), onTextLayout = {}) }, //заголовок окна
                text = {
                    Column {
                        Row {
                            OutlinedButton(
                                onClick = {
                                    mPaint?.setStyle(Paint.Style.STROKE) //стиль рисования
                                    openDialog.value = false//                                  openDialog.value = false
                                },
                            ) {
                                Text(text = stringResource(R.string.stroke), onTextLayout = {})
                            }
                            OutlinedButton(
                                onClick = {
                                    mPaint?.setStyle(Paint.Style.FILL) //стиль рисования
                                    openDialog.value = false//                                   openDialog.value = false
                                },
                            ) {
                                Text(text = stringResource(R.string.fill), onTextLayout = {})
                            }
                        }
                        Row {
                            OutlinedButton(
                                onClick = {
                                    mPaint?.setStyle(Paint.Style.FILL_AND_STROKE) //стиль рисования
                                    openDialog.value = false//                                 openDialog.value = false
                                },
                            ) {
                                Text(text = stringResource(R.string.fillAndStroke), onTextLayout = {})
                            }
                        }
                    }
                },//содержимое окна
                confirmButton = { //кнопка Ok, которая будет закрывать окно
                }
            )
        }
    }

    //создаем массив функций (понадобится позже)
    val funcArray = arrayOf(::drawSquare, ::drawCircle)

    fun onConfirm(value: Float) {
        mPaint?.setStrokeWidth(value) //толщина линии рисования
    }

    //этот метод будет срабатывать при касании нашего объекта пользователем (для свободного рисования)
    override fun onTouchEvent(event: MotionEvent): Boolean { //event хранит информацию о событии
        when (event.action) { // в зависимости от события
            MotionEvent.ACTION_DOWN -> { //если пользователь только коснулся объекта
                path = Path() //создаем новый объект класса Path для записи линии рисования
                path.moveTo(event.x, event.y) //перемещаемся к месту касания
            }
//если пользователь перемещает палец по экрану или отпустил палец
//проводим линию в объекте path до точки касания
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> path.lineTo(event.x,event.y)
        }
        if (path != null) { //если объект не нулевой
            println("mCanvas = $mCanvas")
            mCanvas!!.drawPath(path, mPaint!!)//рисуем на канвасе объект path (и что с ним связано)
            invalidate() //для срабатывания метода onDraw
        }
        return true
    }
}