package com.ddona.appweather

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object Utils {
    // set text
    @JvmStatic
    @BindingAdapter("setText")
    fun setText(tv: TextView, content: String?) {
        tv.setText(content)
    }

    @JvmStatic
    @BindingAdapter("setText")
    fun setText(tv: TextView, content: Int) {
        tv.setText(content.toString())
    }

    @JvmStatic
    @BindingAdapter("setText")
    fun setText(tv: TextView, content: Float) {
        tv.setText(content.toString())
    }

    // set image
    @JvmStatic
    @BindingAdapter("setImageLink")
    fun setImageLink(iv: ImageView, nameIcon: String) {
        var link = "http://openweathermap.org/img/wn/" + nameIcon + "@2x.png"
        Glide.with(iv.context)
            .load(link)
            .into(iv)
    }
}