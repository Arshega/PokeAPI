package com.example.pc.pokeapi

import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    var searchBar: EditText?= null
    var submit:Button?=null
    var txt1:TextView?=null
    var txt2 :TextView?=null
    var txt3 :TextView?=null
    var txt4 :TextView?=null
    var txt5 :TextView?=null
    var txt6 :TextView?=null
    var pbr: ProgressBar?=null
    var pbr1:ProgressBar?=null
    var pbr2:ProgressBar?=null
    var pbr3:ProgressBar?=null
    var pbr4:ProgressBar?=null
    var download : downloader? = null
    var pkm : ArrayList<Pokemons> = ArrayList()
    var imageView : ImageView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        imageView = findViewById(R.id.imageView)
        searchBar = findViewById(R.id.txtName)
        submit = findViewById(R.id.button)
        txt1=findViewById(R.id.PokeName)
        txt2=findViewById(R.id.Attack)
        txt3=findViewById(R.id.Health)
        txt4=findViewById(R.id.Defense)
        txt5=findViewById(R.id.Speed)
        txt6=findViewById(R.id.SpecialAttack)
        pbr=findViewById(R.id.progressBar)
        pbr1=findViewById(R.id.progressBar1)
        pbr2=findViewById(R.id.progressBar2)
        pbr3=findViewById(R.id.progressBar3)
        pbr4=findViewById(R.id.progressBar4)
        submit!!.setOnClickListener {
            var process = Loader()
            process.execute()
        }
        download = downloader()
        download!!.execute()
    }

    inner class Loader : AsyncTask<Void,Pokemons,Void>(){
        var SingleParse = ""
        var Statst = ""
        var refresher = 0
        var ImageUrl =""
        var counter =0

        override fun onPreExecute() {
            super.onPreExecute()
            searchBar!!.isEnabled = false
            submit!!.isEnabled = false
            var toast = Toast.makeText(this@MainActivity,"Loading....",Toast.LENGTH_LONG)
            toast.show()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            var searchvalue = searchBar!!.text.toString()
            while(counter != 100) {
                var pokemonholder = pkm[counter]
                SingleParse = pokemonholder.name.toLowerCase()
                if(SingleParse.equals(searchvalue.toLowerCase())){
                    ImageUrl = pokemonholder.imageurl
                    var data = pokemonholder.url
                    var c = 0
                    while(c !=6 ){
                        var value = pokepower(data, c)
                        if(c == 3) pokemonholder.setspeed(""+value.toString())
                        if(c == 4) pokemonholder.setsa("" + value.toString())
                        if(c == 2) pokemonholder.setdefense("" + value.toString())
                        if(c == 0) pokemonholder.setattack("" + value.toString())
                        if(c== 1) pokemonholder.sethp("" + value.toString())
                        c++
                    }
                    publishProgress(pokemonholder)
                    refresher = 1
                    break
                }
                else{
                    counter ++
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            submit!!.isEnabled = true
            searchBar!!.isEnabled = true
            if(refresher == 1) {
                refresher = setImageBitmap(Statst, ImageUrl, SingleParse)
            }
            else{
                var toast = Toast.makeText(this@MainActivity,"Pokemon Not Found",Toast.LENGTH_LONG)
                toast.show()
            }
        }

        override fun onProgressUpdate(vararg values: Pokemons) {
            super.onProgressUpdate(*values)
            var url = values[0].url
            var counter = 0
            while(counter !=6) {
                var value = pokepower(url, counter)

                if(counter == 0){ pbr!!.setProgress(value)}
                if(counter == 1){pbr1!!.setProgress(value)}
                if(counter == 2) {pbr2!!.setProgress(value)}
                if(counter == 3) {pbr3!!.setProgress(value)}
                if(counter == 4) {pbr4!!.setProgress(value)}
                setpokestats(counter,values[0])
                counter++
            }
        }
    }

    inner class downloader : AsyncTask<Void,Int,Void>(){
        var l1 = findViewById<ConstraintLayout>(R.id.constraintLayout1)
        var l2 = findViewById<ConstraintLayout>(R.id.constraintLayout2)
        var ImageUrl = ""

        override fun onPreExecute() {
            super.onPreExecute()
            var toast = Toast.makeText(this@MainActivity,"FETCHING POKEMONS",Toast.LENGTH_LONG)
            toast.show()
            l2.visibility = View.GONE
            submit!!.isEnabled = false
            searchBar!!.isEnabled = false
        }

        override fun doInBackground(vararg params: Void?): Void? {
            try {
                var counter = 1
                var SingleParse = ""
                var pmon = Pokemons("", "", "", "", "")
                while (counter != 101) {
                    pmon = Pokemons("", "", "", "", "")
                    var data = StringUrl(counter)
                    var JO = JSONObject(data)
                    SingleParse = JO.getString("name")
                    ImageUrl = ImageUrl(data)
                    pmon.url = data
                    pmon.imageurl = ImageUrl
                    pmon.name = SingleParse.toLowerCase()
                    pkm.add(pmon)
                    publishProgress(counter)
                    counter++
                }
            }
            catch(e:InterruptedException){
                e.printStackTrace()
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            var toast = Toast.makeText(this@MainActivity,"Updating PokeDex",Toast.LENGTH_LONG)
            toast.show()
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            l1.visibility = View.VISIBLE
            l2.visibility = View.VISIBLE
            submit!!.isEnabled = true
            searchBar!!.isEnabled = true
        }

    }

    fun setImageBitmap(Statst:String,ImageUrl:String,SingleParse:String):Int{
        txt1!!.setText(SingleParse.capitalize())
        var ins = URL(ImageUrl).openStream()
        var mIcon = BitmapFactory.decodeStream(ins)
        imageView!!.setImageBitmap(mIcon)
        return 0
    }

    fun StringUrl ( counter:Int): String {
        var url = URL("https://pokeapi.co/api/v2/pokemon/"+counter.toString()+"/")
        var httpURLConnection = url.openConnection()
        var inputStream = httpURLConnection.getInputStream()
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String? = ""
        var data :String =""
        while (line != null) {
            line = bufferedReader.readLine()
            data += line
        }
        return  data
    }

    fun ImageUrl (url:String) : String{
        var JO = JSONObject(url).getJSONObject("sprites")
        var SingleParse = JO.getString("front_default")

        return SingleParse
    }

    fun pokepower(data:String,index:Int):Int{
        var JO = JSONObject(data)
        var jArray: JSONArray = JO.getJSONArray("stats")

        var stst = jArray.get(index) as JSONObject
        var  Statst = stst.getString("base_stat").toInt()
        return Statst
    }

    fun setpokestats(counter:Int,pokmon : Pokemons){
        if(counter == 3) txt5!!.setText(pokmon.getspeed())
        if(counter == 4) txt6!!.setText(pokmon.getsa())
        if(counter == 2) txt4!!.setText(pokmon.getdefense())
        if(counter == 0) txt2!!.setText(pokmon.getattack())
        if(counter == 1) txt3!!.setText(pokmon.gethp())
    }






}
