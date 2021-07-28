package com.diegolima.businesscard

import android.app.Application
import com.diegolima.businesscard.data.AppDatabase
import com.diegolima.businesscard.data.BusinessCardRepository

class App : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { BusinessCardRepository(database.businessDao()) }
}