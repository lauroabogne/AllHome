package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import com.example.allhome.data.entities.AppSettingEntity
import com.example.allhome.data.entities.LogsEntity

@Dao
interface LogsDAO {
    @Insert
    fun insert(logsEntity: LogsEntity): Long
}