package org.timer

import android.app.Application
import org.koin.android.ext.koin.*

class MyApplication: Application()  {
    override fun onCreate() {
        super.onCreate()
        initializeKoin{
            androidContext(this@MyApplication)
        }
    }
}