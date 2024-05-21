package com.example.lab2

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable

data class ProgrLang(val name: String, val surname: String, val form: String, var picture: Int = R.drawable.nopicture):Serializable
class ItemViewModel : ViewModel() {
    private var langList = mutableStateListOf( //создаем список из языков программирования
        ProgrLang("Russia", "Putin", "Democraty", R.drawable.putin),
        ProgrLang("Germany", "Merkel", "Parlament", R.drawable.merkel)
    )
    //добавляем объект, который будет отвечать за изменения в созданном списке
    private val _langListFlow = MutableStateFlow(langList)
    //и геттер для него, который его возвращает
    val langListFlow: StateFlow<List<ProgrLang>> get() = _langListFlow
    fun clearList(){ //метод для очистки списка, понадобится в лаб.раб.№5
        langList.clear()
    }
    fun addLangToHead(lang: ProgrLang) { //метод для добавления нового языка в начало списка
        langList.add(0, lang)
    }
    fun addLangToEnd(lang: ProgrLang) { //метод для добавления нового языка в конец списка
        langList.add( lang)
    }
    fun removeItem(item: ProgrLang) { //метод для удаления элемента из списка
        val index = langList.indexOf(item)
        langList.remove(langList[index])
    }
}
