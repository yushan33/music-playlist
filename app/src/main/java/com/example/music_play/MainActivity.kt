package com.example.music_play

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var files= flieList()
        var listView = findViewById<ListView>(R.id.file_Listview)
        var adapter  = FileListAdapter(this, files as MutableList<Fileitem>)
        listView.adapter = adapter

        var mediaPlayer =MediaPlayer()

        var file_all :String = ""
        for (i in  files){
            file_all ="${file_all} 檔案:  ${i.fileName} \n"
        }
        tvLog.text = "${file_all}"


    }
    fun flieList():List<Fileitem>{
        var fileNames :MutableList<Fileitem> = mutableListOf()
        val path :String = Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MUSIC).toString();
        val fileTree :FileTreeWalk = File(path).walk()
        fileTree.maxDepth(1)
            .filter { it.isFile }       //只挑選檔案
            .filter{it.extension == "mp3"}  //只挑選副檔名為mp3
            .forEach { fileNames.add(Fileitem (it.name,it.path)) }

        return fileNames
    }

    class Fileitem( var fileName: String, var filePath: String)

    class FileListAdapter (
        private  val context: Context, private val fileList :MutableList<Fileitem>): BaseAdapter() {

        inner class ViewHolder{
            lateinit var ll_item :LinearLayout
            lateinit var tv_name :TextView
            lateinit var tv_path :TextView
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view  = convertView
            val holder :ViewHolder
            if(convertView == null){
                view = LayoutInflater.from(context).inflate(R.layout.item_fileitem,null)
                holder = ViewHolder()
                holder.ll_item = view.findViewById<LinearLayout>(R.id.LL_item)
                holder.tv_name = view.findViewById<TextView>(R.id.item_filepame)
                holder.tv_path = view.findViewById(R.id.item_filepath)
                view.tag = holder
            }else{
                view = convertView
                holder = view.tag as ViewHolder
            }
            val file = fileList[position]
            holder.tv_name.text = file.fileName
            holder.tv_path.text = file.filePath
            return view!!

        }

        override fun getItem(position: Int): Any = fileList.get(position)

        override fun getItemId(position: Int): Long =position.toLong()

        override fun getCount(): Int =fileList.size
    }


}
