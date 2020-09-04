package com.example.running_app.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.running_app.models.Run
import com.example.running_app.repository.MainRepository
import com.example.running_app.utils.SortType
import kotlinx.coroutines.launch

// we can inject it because MainRepository only needs the dao and dagger already knows how to inject it
// which means that the repository can also be injected without having a provide fun
// if it had more parameters that dagger does not know how to create a provide fun would be necessary
class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel()  {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCalories = mainRepository.getAllRunsSortedByCalories()
    private val runsSortedByKm = mainRepository.getAllRunsSortedByKM()
    private val runsSortedByMillis = mainRepository.getAllRunsSortedByMilliseconds()


    /*MediatorLiveData is a subclass of MutableLiveData that can observe other LiveData objects and react to OnChanged events from them.*/
    val runs = MediatorLiveData<List<Run>>()

    //Default sortType
    var _sortType = SortType.DATE

    init {
        registerToMediator(runsSortedByDate,SortType.DATE)
        registerToMediator(runsSortedByDistance,SortType.DISTANCE)
        registerToMediator(runsSortedByCalories,SortType.CALORIES_BURNED)
        registerToMediator(runsSortedByKm,SortType.AVG_SPEED)
        registerToMediator(runsSortedByMillis,SortType.RUNNING_TIME)
    }

    fun insertRun(run : Run) =
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }


    //Merges the liveData into the mediator and depending on the enumState it replaces the value or not
    //this acts like the callBacks of what to do when this type of source is called
    //listens to the source given
    //if the liveData is nor registered as source it won't be listened
    private fun registerToMediator(listPassed : LiveData<List<Run>>, filteredBy: SortType){
        runs.addSource(listPassed){result -> //Whenever something is emitted from the live data, do this
            if (_sortType == filteredBy){
                result?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortRuns(sortType : SortType) = when(sortType){
        SortType.DATE -> runsSortedByDate.value?.let {
            runs.value = it
        }

        SortType.AVG_SPEED -> runsSortedByKm.value?.let {
            runs.value = it
        }

        SortType.RUNNING_TIME -> runsSortedByMillis.value?.let {
            runs.value = it
        }

        SortType.CALORIES_BURNED -> runsSortedByCalories.value?.let {
            runs.value = it
        }

        SortType.DISTANCE -> runsSortedByDistance.value?.let {
            runs.value = it
        }
    }.also {
        this._sortType = sortType
    }



}