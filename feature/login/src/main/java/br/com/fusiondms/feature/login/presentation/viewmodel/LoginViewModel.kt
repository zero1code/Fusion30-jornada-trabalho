package br.com.fusiondms.feature.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(

): ViewModel() {

    sealed class LoginStatus() {
        object Nothing: LoginStatus()
        class Loading(val isLoading: Boolean) : LoginStatus()
        object Success : LoginStatus()
        class Error(val message: String?) : LoginStatus()
    }

    private var _viewFlipperPosition = MutableStateFlow(0)
    val viewFlipperPosition: StateFlow<Int> get() = _viewFlipperPosition

    fun showNext(position: Int) = viewModelScope.launch {
        _viewFlipperPosition.emit(position)
    }

}