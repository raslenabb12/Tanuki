package com.youme.tanuki

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class entreyupdateFragment(
    val entry: mediaListEntry?=null,
    val manga: MangaDetails? =null, private val OnDone: (String) -> Unit,) : BottomSheetDialogFragment() {
    private val repository: MangaRepository = MangaRepository()
    private var startd :Date?=null
    private var compld :Date?=null
    private lateinit var tokenManager: AniListTokenManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tokenManager=AniListTokenManager(requireContext())
        return inflater.inflate(R.layout.entylist_layout2, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button5).setOnClickListener {
            dismiss()
        }
        entry?.let {
            view.findViewById<Button>(R.id.button7).visibility=View.VISIBLE
            view.findViewById<Button>(R.id.button7).setOnClickListener {
                lifecycleScope.launch {
                    repository.removeMangaFromList(
                        accessToken = tokenManager.getAccessToken().toString(),
                        mediaId = entry.id!!
                    ).onSuccess {
                        OnDone("Done")
                        dismiss()
                    }.onFailure {
                        showError(it.message.toString())
                    }
                }
            }
        }
        view.findViewById<Button>(R.id.button8).setOnClickListener {
            showDatePicker(LocalDate.now()) { date ->
                startd=Date(date.year,date.dayOfMonth,date.monthValue)
                view.findViewById<Button>(R.id.button8).text=(date.format(DateTimeFormatter.ofPattern("yyyy-dd-MM")))
            }
        }
        view.findViewById<Button>(R.id.button10).setOnClickListener {
            showDatePicker(LocalDate.now()) { date ->
                compld=Date(date.year,date.dayOfMonth,date.monthValue)
                view.findViewById<Button>(R.id.button10).text=(date.format(DateTimeFormatter.ofPattern("yyyy-dd-MM")))
            }
        }
        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.statusDropdown)
        val progress = view.findViewById<TextInputEditText>(R.id.progressInput)
        val statusOptions = listOf("CURRENT", "PLANNING", "COMPLETED", "DROPPED", "PAUSED","REPEATING")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions)
        entry?.startedAt?.let {
            if (it.year!=null){
                startd=entry.startedAt
                view.findViewById<Button>(R.id.button8).text=it.year.toString()+"-"+it.month.toString()+"-"+it.day.toString()
            }
        }
        entry?.completedAt?.let {
            if (it.year!=null){
                compld = entry.completedAt
                view.findViewById<Button>(R.id.button10).text=it.year.toString()+"-"+it.month.toString()+"-"+it.day.toString()
            }
        }
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = statusOptions[position]
            if (selectedOption=="COMPLETED" ){
                val today = LocalDate.now()
                view.findViewById<Button>(R.id.button10).text=today.year.toString()+"-"+today.monthValue.toString()+"-"+today.dayOfMonth.toString()
                compld=Date(today.year,today.dayOfMonth,today.monthValue)
            }
            if (selectedOption=="CURRENT" ){
                val today = LocalDate.now()
                view.findViewById<Button>(R.id.button8).text=today.year.toString()+"-"+today.monthValue.toString()+"-"+today.dayOfMonth.toString()
                startd=Date(today.year,today.dayOfMonth,today.monthValue)
            }
        }
        entry?.let {
            autoCompleteTextView.setText(entry.status, false)
            progress.setText(entry.progress.toString())
        }
        view.findViewById<Button>(R.id.button4).setOnClickListener {
            lifecycleScope.launch {
                repository.updateMangaProgress(
                    accessToken = tokenManager.getAccessToken().toString(),
                    mediaId = entry?.mediaId?:manga?.id?:0,
                    progress=view.findViewById<TextInputEditText>(R.id.progressInput).text.toString().toInt(),
                    status = autoCompleteTextView.text.toString(),
                    startedAt = startd,
                    completedAt = compld
                ).onSuccess {
                    OnDone("Done")
                    dismiss()
                }.onFailure {
                    showError(it.message.toString())
                }
            }


        }
    }
    private fun showDatePicker(initialDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
        val today = LocalDate.now()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setTheme(R.style.CustomDatePickerStyle)
            .setSelection(initialDate?.toEpochDay()?.times(86400000) ?: today.toEpochDay() * 86400000)
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            onDateSelected(selectedDate)
        }
        picker.show(parentFragmentManager, "date_picker")
    }
    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .show()
    }
}