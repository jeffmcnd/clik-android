package xyz.mcnallydawes.clik

import android.app.Application
import com.facebook.FacebookSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
