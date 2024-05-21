package com.example.lab2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab2.ui.theme.Lab2Theme

class InputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MakeInputPart()//наша функция по созданию интерфейса для ввода нового языка
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeInputPart() {
        var langName by remember { //объект для работы с текстом, для названия языка
            mutableStateOf("") //его начальное значение
        }//в функцию mutableStateOf() в качестве параметра передается отслеживаемое значение
        var langSurname by remember {
            mutableStateOf("")
        }
        var langForm by remember {
            mutableStateOf("")
        }
        val scope =
            rememberCoroutineScope() //объект для прокручивания списка при вставке нового эл-та
        Column(
            //ряд для расположения эл-ов
            verticalArrangement = Arrangement.spacedBy(100.dp), //центруем по вертикали
            horizontalAlignment = Alignment.CenterHorizontally, //и добавляем отступы между эл-ми
        ) {
            TextField( //текстовое поле для ввода имени языка
                value = langName, //связываем текст из поля с созданным ранее объектом
                onValueChange = { newText -> //обработчик ввода значений в поле
                    langName = newText //все изменения сохраняем в наш объект
                },
                textStyle = TextStyle( //объект для изменения стиля текста
                    fontSize = 20.sp //увеличиваем шрифт
                ),
                label = { Text("Название") }, //это надпись в текстовом поле
                modifier = Modifier.weight(2f)//это вес колонки.Нужен для распределения долей в ряду.
//Контейнер Row позволяет назначить вложенным компонентам ширину в соответствии с их весом.
//Поэтому полям с данными назначаем вес 2, кнопке вес 1, получается сумма
// всех весов будет 5, и для полей с весом 2 будет выделяться по 2/5 от всей ширины ряда, для
//кнопки с весом 1 будет выделяться 1/5 от всей ширины ряда
            )
            TextField( //текстовое поле для ввода года создания языка
                value = langSurname, //связываем текст из поля с созданным ранее объектом
                onValueChange = { newText ->
                    langSurname = newText
                }, //с учетом возможной пустой строки
                textStyle = TextStyle( //объект для изменения стиля текста
                    fontSize = 20.sp //увеличиваем шрифт
                ),
                //и меняем тип допустимых символов для ввода – только цифры
                label = { Text("Фамилия") },
                modifier = Modifier.weight(2f) //назначаем вес поля
            )
            TextField( //текстовое поле для ввода года создания языка
                value = langForm, //связываем текст из поля с созданным ранее объектом
                onValueChange = { newText ->
                    langForm = newText
                }, //с учетом возможной пустой строки
                textStyle = TextStyle( //объект для изменения стиля текста
                    fontSize = 20.sp //увеличиваем шрифт
                ),
                //и меняем тип допустимых символов для ввода – только цифры
                label = { Text("Форма") },
                modifier = Modifier.weight(2f) //назначаем вес поля
            )
            Button( //кнопка для добавления нового языка
                onClick = { //при нажатии кнопки делаем отладочный вывод
                    println("added $langName $langSurname $langForm")
                    //и добавляем в начало списка новый язык с нужными параметрами
                    val newLang = ProgrLang(langName, langSurname, langForm)
                    val intent = Intent()
                    intent.putExtra("newItem", newLang)
                    setResult(RESULT_OK, intent)
                    langName = "" //и очищаем поля
                    langSurname = ""
                    langForm = ""
                    finish()
                },
                modifier = Modifier.weight(1f) //назначаем вес кнопки
            ) {
                Text("+") //надпись для кнопки
            }
        }
    }
}