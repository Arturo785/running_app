package com.example.running_app.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.running_app.models.Run
import com.example.running_app.repository.MainRepository
import kotlinx.coroutines.launch

// we can inject it because MainRepository only needs the dao and dagger already knows how to inject it
// which means that the repository can also be injected without having a provide fun
// if it had more parameters that dagger does not know how to create a provide fun would be necessary
class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel()  {

    fun insertRun(run : Run) =
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }


}