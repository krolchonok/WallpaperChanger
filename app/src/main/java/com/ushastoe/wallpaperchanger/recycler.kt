package com.ushastoe.wallpaperchanger

import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class CustomRecyclerAdapter(private val names: MutableList<String>,  private val contextMain: Context ) :
    RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler, parent, false)
        return MyViewHolder(itemView)
    }

    private val params = LinearLayout.LayoutParams(500, 500)

    private fun showConfirmBar(path: String) {
        val builder = MaterialAlertDialogBuilder(contextMain)
        builder.setMessage(R.string.delete_confirm)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { _, _ ->
                deleteItemByTag(path)
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val bmp = BitmapFactory.decodeFile(names[position])
        val image = ImageView(contextMain)
        image.layoutParams = params
        image.scaleType = ImageView.ScaleType.CENTER_CROP
        image.setImageBitmap(bmp)
        holder.itemView.setPadding(0, 0, 0, 10)
        holder.cardView.tag = names[position]
        holder.cardView.radius = 30f
        holder.cardView.setOnClickListener {
            changeWallpaper(contextMain, holder.cardView.tag.toString())
        }

        holder.cardView.setOnLongClickListener{
            showConfirmBar(holder.cardView.tag.toString())
            true
        }

        holder.cardView.addView(image)

    }

    private fun deleteItemByTag(tag: String) {
        val position = names.indexOf(tag)
        if (position != -1) {
            val file = File(tag)
            try {
                val delete = file.delete()
                if (delete) {
                    names.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)
                }
            } catch (e: NumberFormatException) {
                println("NO")
            }
        }
    }

    override fun getItemCount(): Int {
        return names.size
    }

    private fun changeWallpaper(context: Context, pathMain: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle(R.string.select_screen)

        val options = arrayOf(
            contextMain.getString(R.string.lockscreen),
            contextMain.getString(R.string.desktop),
            contextMain.getString(R.string.both)
        )

        alertDialogBuilder.setItems(options) { _, which ->
            when (which) {
                0 -> setsWallpaper(contextMain, pathMain, WallpaperManager.FLAG_LOCK)
                1 -> setsWallpaper(contextMain, pathMain, WallpaperManager.FLAG_SYSTEM)
                2 -> setsWallpaper(contextMain, pathMain, WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM)
            }
        }

        alertDialogBuilder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun setsWallpaper(context: Context, pathMain: String, flag: Int) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = BitmapFactory.decodeFile(pathMain)
            wallpaperManager.setBitmap(bitmap, null, true, flag)
            Toast.makeText(context, R.string.wallpaper_set, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.wallpaper_set_failed, Toast.LENGTH_SHORT).show()
        }
    }
}