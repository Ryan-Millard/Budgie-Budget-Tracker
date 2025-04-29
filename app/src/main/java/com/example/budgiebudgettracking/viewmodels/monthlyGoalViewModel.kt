package com.example.budgiebudgettracking.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.budgiebudgettracking.database.AppDatabase
import com.example.budgiebudgettracking.entities.MonthlyGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MonthlyGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).monthlyGoalDao()

    fun getGoal(yearMonth: String, userId: Int): LiveData<MonthlyGoal?> = dao.getByYearMonth(yearMonth, userId)

    fun upsert(goal: MonthlyGoal) = viewModelScope.launch(Dispatchers.IO) {
        if (goal.id == 0) dao.insert(goal)
        else dao.update(goal)
    }
}