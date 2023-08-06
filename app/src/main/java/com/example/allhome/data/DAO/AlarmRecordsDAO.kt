package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import com.example.allhome.data.entities.AlarmRecordsEntity
import com.example.allhome.data.entities.AppSettingEntity
import com.example.allhome.data.entities.LogsEntity

@Dao
interface AlarmRecordsDAO {
    @Insert
    fun insert(alarmRecordsEntity: AlarmRecordsEntity): Long
}