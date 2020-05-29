package com.example.music_play

import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.time.temporal.TemporalAdjusters.next

class MainActivity : AppCompatActivity() {
    lateinit var mediaPlayer:MediaPlayer
    lateinit var listView: ListView
    lateinit var adapter: FileListAdapter
    lateinit var musicList :MutableList<Fileitem>
    var currentMusicIndex :Int = 0
    var pausePosion :Int =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        musicList= flieList()
        listView = findViewById<ListView>(R.id.file_Listview)
        adapter  = FileListAdapter(this, musicList )
        listView.adapter = adapter
        mediaPlayer =MediaPlayer()
        buttonListener()
        rdbLoop.isChecked = true



    }
    private fun buttonListener(){
        var buttonListen =InnerOnClickListener()
        ibPlay.setOnClickListener(buttonListen)
        ibNext.setOnClickListener(buttonListen)
        ibPrevious.setOnClickListener(buttonListen)
        listView.setOnItemClickListener(InnerItemOnClick())
        mediaPlayer.setOnCompletionListener(OnCompletionListener())

    }

    //讀取音樂檔案
    fun flieList():MutableList<Fileitem>{
        var fileNames :MutableList<Fileitem> = mutableListOf()
        val path :String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();

        val fileTree :FileTreeWalk = File(path).walk()
        fileTree.maxDepth(1)
            .filter { it.isFile }       //只挑選檔案
            .filter{it.extension == "mp3"}  //只挑選副檔名為mp3
            .forEach { fileNames.add(Fileitem (it.name,it.path)) }

        return fileNames
    }


    private inner class InnerOnClickListener :View.OnClickListener{
        override fun onClick(v: View?) {
            if (v != null) {
                when(v.id){
                    R.id.ibPlay->{
                        if(mediaPlayer.isPlaying) {
                            pause()
                        }else{
                            play()
                        }
                    }
                    R.id.ibNext->{
                        next()
                    }
                    R.id.ibPrevious->{
                        private()
                    }
                }
            }
        }

    }

    private inner class InnerItemOnClick :AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if(currentMusicIndex!=position){
                currentMusicIndex=position
                play()
            }else{
                pausePosion=0
                play()
            }

        }
    }

    private inner class OnCompletionListener :MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer?) {
            next()
        }
    }

    private fun play(){
        mediaPlayer.reset()
        mediaPlayer.setDataSource(musicList.get(currentMusicIndex).filePath)
        mediaPlayer.prepare()
        mediaPlayer.seekTo(pausePosion)
        mediaPlayer.start()
        tvNowpath.text = getString(R.string.playing) + musicList.get(currentMusicIndex).fileName
        ibPlay.setImageResource(android.R.drawable.ic_media_pause)
    }
    private fun pause(){
        mediaPlayer.pause()
        pausePosion = mediaPlayer.currentPosition
        ibPlay.setImageResource(android.R.drawable.ic_media_play)
    }
     private fun next(){
        if(rdbLoop.isChecked){
            loop()
        }else if(rdbRandom.isChecked){
            randoms()
        }else if (rdbSingle.isChecked){
            single()
        }
    }

    private fun private(){
        if(rdbSingle.isChecked){
            single()
        }else if(rdbRandom.isChecked){
            randoms()
        }else if(rdbLoop.isChecked){
            currentMusicIndex--
            if(currentMusicIndex<0){
                currentMusicIndex= musicList.size-1
            }
            play()
        }
    }

    private fun loop(){
        currentMusicIndex++
        if(currentMusicIndex>=musicList.size){
            currentMusicIndex=0
        }
        pausePosion = 0
        play()
    }

    private fun randoms(){
        val size = musicList.size-1
        currentMusicIndex = (0..size).random()
        pausePosion = 0
        play()
    }
    private fun single(){
        pausePosion = 0
        play()
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
