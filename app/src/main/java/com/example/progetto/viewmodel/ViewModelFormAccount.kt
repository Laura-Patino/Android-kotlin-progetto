package com.example.progetto.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.room.util.copy
import com.example.progetto.model.dataclasses.UpdateUserParamsWithSid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ViewModelFormAccount {
    private val TAG = ViewModelFormAccount::class.simpleName

    //private val _errorsState = MutableStateFlow(emptyMap<String, String?>())
    //val errorsState = _errorsState //asStateFlow()

    private fun validateFirstName(firstName: String) : Boolean{
        return firstName.length in 1..15
    }

    private fun validateLastName(lastName: String): Boolean {
        return lastName.length in 1..15
    }

    private fun validateCardFullName(cardFullName: String) : Boolean {
        return cardFullName.length in 1..31
    }

    private fun validateCardNumber(cardNumber: String) : Boolean {
        // check se è lunga 16 ed è un numero
        return cardNumber.length == 16 && cardNumber.toBigIntegerOrNull() != null
    }

    private fun validateCardExpireMonth(cardExpireMonth: String) : Boolean {
        //check se è di lunghezza 2, se è un numero, ed è compreso tra 1 e 12
        return cardExpireMonth.length >= 1 && cardExpireMonth.toIntOrNull() != null && cardExpireMonth.toInt() in 1..12
    }

    private fun validateCardExpireYear(cardExpireMonth: String): Boolean {
        return cardExpireMonth.length == 4 && cardExpireMonth.toIntOrNull() != null && cardExpireMonth.toInt() > 2024
    }

    private fun validateCardCVV(cardCVV: String): Boolean {
        return cardCVV.length == 3 && cardCVV.toIntOrNull() != null
    }

    fun handleSubmitForm(fields: UpdateUserParamsWithSid): Map<String, String?> {
        val newErrors = mutableMapOf<String, String?>()

        newErrors["firstName"] = if (!validateFirstName(fields.firstName)) "Nome obbligatorio" else null
        newErrors["lastName"] = if (!validateLastName(fields.lastName)) "Cognome obbligatorio" else null
        newErrors["cardFullName"] = if (!validateCardFullName(fields.cardFullName)) "Nome sulla carta obbligatorio" else null
        newErrors["cardNumber"] = if (!validateCardNumber(fields.cardNumber)) "Il numero della carta deve essere da 16 numeri" else null
        newErrors["cardExpireMonth"] = if (!validateCardExpireMonth(fields.cardExpireMonth.toString())) "Mese non valido" else null
        newErrors["cardExpireYear"] = if (!validateCardExpireYear(fields.cardExpireYear.toString())) "Anno non valido" else null
        newErrors["cardCVV"] = if (!validateCardCVV(fields.cardCVV)) "CVV non valido" else null

        Log.d(TAG, "Numeri errori del form: ${newErrors.values.count { it != null }}")
        Log.d(TAG, "ViewModel Errors del form: $newErrors")
        return newErrors
    }

}