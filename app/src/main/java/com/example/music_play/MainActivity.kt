package com.example.music_play

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Color.blue
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    lateinit var mediaPlayer:MediaPlayer
    lateinit var listView: ListView
    lateinit var adapter: FileListAdapter
    lateinit var musicList :MutableList<Fileitem>
    val handler = Handler()
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
        seekBar.setOnSeekBarChangeListener(OnSeekBarChangerListen())

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

    //播放按鈕、前一首、下一首按鈕監聽器
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

    //listview內項目監聽器
    private inner class InnerItemOnClick :AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            pausePosion = 0
            currentMusicIndex = position
            play()
        }
    }

    private inner class OnCompletionListener :MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer?) {
            next()
        }
    }

    private inner class OnSeekBarChangerListen :SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            //SeekBar 進度變更過程觸發，設定進度改變時要做的事
            //seekBar：使用者滑動的 SeekBar，progress：SeekBar 的進度 ，fromUser：如果是使用者滑動造成進度變動則為 True，若是經 Code 變更進度則為 False
           if(fromUser){
               pausePosion = progress
               mediaPlayer.seekTo(pausePosion)
           }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            //在 SeekBar 被使用者觸摸的當下觸發
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            //在使用者手指離開 SeekBar 時當下觸發

        }

    }

    private fun play(){
        mediaPlayer.reset()
        mediaPlayer.setDataSource(musicList.get(currentMusicIndex).filePath)
        mediaPlayer.prepare()
        seekBar.max = mediaPlayer.duration
        mediaPlayer.seekTo(pausePosion)
        mediaPlayer.start()

        tvNowpath.text = getString(R.string.playing) + musicList.get(currentMusicIndex).fileName

        var sec = String.format("%02d",mediaPlayer.duration/1000%60)
        tvAlltime.text = "${mediaPlayer.duration/1000/60} : ${sec}"

        ibPlay.setImageResource(android.R.drawable.ic_media_pause)
        handler.post(task);//立即调用

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


    private val task: Runnable = object : Runnable {
        override fun run() {
            handler.postDelayed(this,1000.toLong()) //设置延迟时间，此处是5秒
            seekBar.progress = mediaPlayer.currentPosition
            var min = mediaPlayer.currentPosition/1000/60
            var sec =String.format("%02d",mediaPlayer.currentPosition/1000%60)
            tvNowtime.text = "${min}:${sec}"
        }
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
