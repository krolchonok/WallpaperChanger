package com.ushastoe.wallpaperchanger

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
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

    private fun changeWallpaper(pathMain: String){
        val wall: WallpaperManager = WallpaperManager.getInstance(contextMain)
        val bmp = BitmapFactory.decodeFile(pathMain)
        wall.setBitmap(bmp)
    }

    private fun showConfirmBar(path: String) {
        val builder = MaterialAlertDialogBuilder(contextMain)
        builder.setMessage("Are you sure you want to Delete?")
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
            changeWallpaper(holder.cardView.tag.toString())
            android.os.Process.killProcess(android.os.Process.myPid())
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
}