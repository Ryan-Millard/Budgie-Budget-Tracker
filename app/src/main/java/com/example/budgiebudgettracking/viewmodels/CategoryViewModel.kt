package com.example.budgiebudgettracking.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.entities.Category
import com.example.budgiebudgettracking.utils.SessionManager
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

	private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
	private val sessionManager = SessionManager.getInstance(application)

	private val _categories = MediatorLiveData<List<Category>>()
	val categories: LiveData<List<Category>> = _categories

	private val _operationResult = MutableLiveData<Boolean>()
	val operationResult: LiveData<Boolean> = _operationResult

	init {
		loadCategories()
	}

	private fun loadCategories() {
		viewModelScope.launch {
			val userId = getUserId()
			val categoriesSource = categoryDao.getCategoriesByUserLive(userId)
			_categories.addSource(categoriesSource) { result ->
				_categories.value = result
			}
		}
	}

	fun addCategory(category: Category) {
		viewModelScope.launch {
			try {
				categoryDao.insert(category)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	fun updateCategory(category: Category) {
		viewModelScope.launch {
			try {
				categoryDao.update(category)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	fun deleteCategory(category: Category) {
		viewModelScope.launch {
			try {
				categoryDao.delete(category)
				_operationResult.postValue(true)
			} catch (e: Exception) {
				_operationResult.postValue(false)
			}
		}
	}

	fun getCategory(categoryId: Int, callback: (Category?) -> Unit) {
		viewModelScope.launch {
			val category = categoryDao.getCategoryById(categoryId)
			callback(category)
		}
	}

	private suspend fun getUserId(): Int? {
		val email = sessionManager.getUserEmail()
		if (email.isEmpty()) return null

		return AppDatabase.getDatabase(getApplication()).userDao().getUserByEmail(email)?.id
	}

	class Factory(private val application: Application) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
				@Suppress("UNCHECKED_CAST")
				return CategoryViewModel(application) as T
			}
			throw IllegalArgumentException("Unknown ViewModel class")
		}
	}
}
