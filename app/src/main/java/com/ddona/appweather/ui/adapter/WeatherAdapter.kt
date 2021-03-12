package com.ddona.appweather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ddona.appweather.databinding.ItemWeatherBinding
import com.ddona.appweather.model.List

class WeatherAdapter(val inter: IWaether) : RecyclerView.Adapter<WeatherAdapter.WeatherHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherHolder {
        return WeatherHolder(
            ItemWeatherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WeatherHolder, position: Int) {
        holder.binding.data = inter.getData(position)
    }

    override fun getItemCount() = inter.getCount()

    interface IWaether {
        fun getCount(): Int
        fun getData(position: Int): List
    }

    class WeatherHolder(val binding: ItemWeatherBinding) : RecyclerView.ViewHolder(binding.root)
}