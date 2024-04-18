package com.example.yoru.ui.gallery

import Mqtt5Handler
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yoru.ValueStatic
import com.example.yoru.databinding.FragmentGalleryBinding

class ConfigurationFragment : Fragment() {

    private lateinit var mqtt5Handler: Mqtt5Handler
    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val configurationViewModel =
            ViewModelProvider(this).get(ConfigurationViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Load existing values into the UI
        binding.host.setText(ValueStatic.host)
        binding.port.setText(ValueStatic.port.toString())
        binding.username.setText(ValueStatic.username)
        binding.password.setText(ValueStatic.password)
        binding.look.setText(ValueStatic.look)
        binding.topic.setText(ValueStatic.topic)

        // Setup the submit button click listener
        binding.submitId.setOnClickListener {
            ValueStatic.host = binding.host.text.toString()
            ValueStatic.port = binding.port.text.toString().toInt()
            ValueStatic.username = binding.username.text.toString()
            ValueStatic.password = binding.password.text.toString()
            ValueStatic.look = binding.look.text.toString()
            ValueStatic.topic = binding.topic.text.toString()

            submitHandler()
        }
        return root
    }

    private fun submitHandler() {
        mqtt5Handler = Mqtt5Handler(
            ValueStatic.host,
            ValueStatic.port,
            ValueStatic.username,
            ValueStatic.password
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}