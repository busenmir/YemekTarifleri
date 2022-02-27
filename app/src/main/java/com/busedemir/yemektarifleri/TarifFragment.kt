package com.busedemir.yemektarifleri

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream

class TarifFragment : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener{
            kaydet(it)
        }
        imageView.setOnClickListener{
            gorselSec(it)
        }

        arguments?.let{

            val gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi
            // yeni bir yemek eklemeye geldi
            val unit = if (gelenBilgi.equals("menudengeldim")) {

                YemekismiText.setText("")
                YemekTarifiText.setText("")

                button.visibility = View.VISIBLE

                val gorselArkaPlani =
                    BitmapFactory.decodeResource(context?.resources, R.drawable.gorsel)
                imageView.setImageBitmap(gorselArkaPlani)


            } else {

                //daha önce oluşturulan yemeği görmeye geldi. Burada ver tabanından bilgileri çekitoruz.
                button.visibility = View.INVISIBLE

                val secilenId = TarifFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE, null)
                        val cursor = db.rawQuery(
                            "SELECT * FROM yemekler WHERE id= ? ",
                            arrayOf(secilenId.toString())
                        )

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemektarifi")
                        val gorseli = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()) {
                            YemekismiText.setText(cursor.getString(yemekIsmiIndex))
                            YemekTarifiText.setText(cursor.getString(yemekMalzemeIndex))
                            val byteDizisi = cursor.getBlob(gorseli)

                            // Bitmapı byte dizisine çevirmek
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi, 0, byteDizisi.size)
                            imageView.setImageBitmap(bitmap)
                        }
                        cursor.close()

                    } catch (e: Exception) {

                    }
                }
            }
            unit
        }

    }

    fun kaydet(view: View){

        val yemekIsmi = YemekismiText.text.toString()
        val yemekMalzemeleri = YemekTarifiText.text.toString()

        if(secilenBitmap !=null){

            val kucukBitMap = kucukBitmapOlusturmak(secilenBitmap!!,500)

            val outputsSource = ByteArrayOutputStream()
            kucukBitMap.compress(Bitmap.CompressFormat.PNG,50,outputsSource)
            val byteDizisi=outputsSource.toByteArray()

            try{
                context?.let{
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemektarifi VARCHAR, gorsel BLOB)")
                    val sqlstring = " INSERT INTO yemekler(yemekismi, yemektarifi, gorsel) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sqlstring)
                    statement.bindString(1,yemekIsmi)
                    statement.bindString(2,yemekMalzemeleri)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()

                }
            }catch (e :Exception){
                e.printStackTrace()
            }
            val action =TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)


        }

    }

    fun gorselSec(view: View) {

        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                // request ile izin isredik
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            } else {
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)

            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1 ){

            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }


        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            // görselin nerde durduğunu secilengorsel ile aldık. sonra bitmape çevirmeliyiz.

            secilenGorsel = data.data

            try{

                context?.let{
                    if(secilenGorsel != null){
                        if(Build.VERSION.SDK_INT >=28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(secilenBitmap)
                        }else{

                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            imageView.setImageBitmap(secilenBitmap)
                        }
                    }
                }


            }catch (e : Exception){
                e.printStackTrace()

            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun kucukBitmapOlusturmak(kullanicinsecdigiBitmap: Bitmap, maximumBoyut:Int): Bitmap{
        var width = kullanicinsecdigiBitmap.width
        var height = kullanicinsecdigiBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if ( bitmapOrani > 1 ){
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        }else{
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()

        }

        return Bitmap.createScaledBitmap(kullanicinsecdigiBitmap,width,height,true)
    }

}

