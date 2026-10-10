package ru.netology.nework.application

import android.app.Application
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.io.InputStream
import java.util.concurrent.TimeUnit


@HiltAndroidApp
class NeWorkApp : Application(){
    override fun onCreate() {
        super.onCreate()
        val glideClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .protocols(listOf(Protocol.HTTP_1_1)) // не грузятся картинки, возможно конфликт imagekit и HTTP/2
            .build()
        Glide.get(this).registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(glideClient)
        )
    }
}