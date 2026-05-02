package com.example.pegasus.di

import android.content.Context
import androidx.room.Room
import com.example.pegasus.data.local.AppDatabase
import com.example.pegasus.data.local.dao.AccessLogDao
import com.example.pegasus.data.local.dao.ActivityDao
import com.example.pegasus.data.local.dao.TripDao
import com.example.pegasus.data.local.dao.UserDao
import com.example.pegasus.data.repository.ActivityRepositoryImpl
import com.example.pegasus.data.repository.AuthRepositoryImpl
import com.example.pegasus.data.repository.TripRepositoryImpl
import com.example.pegasus.data.repository.UserRepositoryImpl
import com.example.pegasus.domain.ActivityRepository
import com.example.pegasus.domain.AuthRepository
import com.example.pegasus.domain.TripRepository
import com.example.pegasus.domain.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Sprint 03: Hilt application-wide module.
 *
 * - Builds the Room database singleton.
 * - Provides DAOs.
 * - Provides FirebaseAuth.
 * - Binds repository interfaces to their Room/Firebase implementations.
 *
 * `fallbackToDestructiveMigration` keeps dev iteration fast — for production,
 * write proper Migrations.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideTripDao(db: AppDatabase): TripDao = db.tripDao()

    @Provides
    fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()

    @Provides
    fun provideAccessLogDao(db: AppDatabase): AccessLogDao = db.accessLogDao()
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
