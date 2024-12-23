package com.example.sdmanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class FileAdapter(
    private val files: MutableList<FileModel>,
    private val navigateNotFound: (String) -> Unit = {},
    private val navigateDown: (String) -> Unit = {},
    private val context: Context
): RecyclerView.Adapter<FileAdapter.FileViewHolder>() {
    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textFileName: TextView = itemView.findViewById(R.id.file_name)
        private val textFileChild: TextView = itemView.findViewById(R.id.count)
        private val textFileCreationDate: TextView = itemView.findViewById(R.id.date)
        private val iconFile: ImageView = itemView.findViewById(R.id.file_icon)

        fun bind(file: FileModel) {
            textFileName.text = ellipsizeMiddle(file.name)
            textFileChild.text = when (file.isDirectory) {
                true -> itemView.context.getString(R.string.items_count, file.child)
                false -> {
                    val fileSizeFormat = StorageUtility.fileSizeConversion(file.size)
                    itemView.context.getString(R.string.items_size, fileSizeFormat.first, fileSizeFormat.second)
                }
            }
            textFileCreationDate.text = file.creationDate.format(
                DateTimeFormatter.ofPattern("dd MMMM, yyyy")
            )
            iconFile.setImageDrawable(
                AppCompatResources.getDrawable(
                    itemView.context,
                    if (file.isDirectory) R.drawable.baseline_folder_24 else R.drawable.baseline_insert_drive_file_24
                )
            )
            itemView.setOnClickListener { _ ->
                if (file.isDirectory) {
                    if (file.child == 0) navigateNotFound(file.name)
                    else navigateDown(file.path)
                } else {
                    StorageUtility.openFile(context, file.path)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.file_layout,
            parent, false
        )
        return FileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]

        holder.bind(file)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    private fun ellipsizeMiddle(text: String, maxLength: Int = 40): String {
        if (text.length <= maxLength) return text

        val keepLength = maxLength / 2
        val start = text.substring(0, keepLength)
        val end = text.substring(text.length - keepLength)
        return "$start...$end"
    }
}