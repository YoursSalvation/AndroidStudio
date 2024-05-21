package com.example.lab2

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable

data class ProgrLang(val name: String, val surname: String, val form: String, var picture: String = R.drawable.nopicture.toString()):Serializable
class ItemViewModel : ViewModel() {
    private var langList = mutableStateListOf( //создаем список из языков программирования
        ProgrLang("Russia", "Putin", "Democraty", R.drawable.putin.toString()),
        ProgrLang("Germany", "Merkel", "Parlament", R.drawable.merkel.toString())
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
    fun changeImage(index: Int, value: String) {
        langList[index] = langList[index].copy(picture = value)
    }
}
