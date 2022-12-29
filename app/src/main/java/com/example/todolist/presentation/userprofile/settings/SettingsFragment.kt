package com.example.todolist.presentation.userprofile.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.todolist.R
import com.example.todolist.databinding.FragmentSettingsBinding
import com.example.todolist.presentation.activity.ActivityViewModel
import vadiole.colorpicker.ColorModel
import vadiole.colorpicker.ColorPickerDialog

typealias ColorPickListener = (color:Int) -> Unit

class SettingsFragment : Fragment() {
    private lateinit var binding:FragmentSettingsBinding
    private val vModel: ActivityViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater,container,false)

        binding.background.setOnClickListener {
            pickColor(vModel.getBackgroundColor()){
                vModel.setBackgroundColor(it)
            }
        }


        binding.toolbar.setOnClickListener{
            pickColor(vModel.getToolbarColor()){
                vModel.setToolbarBackgroundColor(it)
            }
        }


        binding.window.setOnClickListener {
            pickColor(vModel.getWindowColor()){
                vModel.setWindowColor(it)
            }
        }

        return binding.root
    }
    private fun pickColor(initColor:Int,colorPickListener: ColorPickListener){
        ColorPickerDialog.Builder()
            .setInitialColor(initColor)
            .setColorModel(ColorModel.RGB)
            .setButtonOkText(R.string.done)
            .setButtonCancelText(R.string.cancel)
            .onColorSelected(colorPickListener)
            .create()
            .show(parentFragmentManager,"xdd")
    }


}