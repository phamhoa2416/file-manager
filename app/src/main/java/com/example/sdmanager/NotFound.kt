package com.example.sdmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private const val ARG_PARAM1 = "msg"
private const val ARG_PARAM2 = "returnable"
private const val ARG_PARAM3 = "title"

class NotFound : Fragment() {
    private var message: String = ""
    private var returnable: Boolean = true
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString(ARG_PARAM1) ?: getString(R.string.denied)
            returnable = it.getBoolean(ARG_PARAM2, true)
            title = it.getString(ARG_PARAM3) ?: getString(R.string.app_name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(returnable)
        actionBar?.title = title
        return inflater.inflate(R.layout.not_found, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.no_files_msg).text = message
    }
}