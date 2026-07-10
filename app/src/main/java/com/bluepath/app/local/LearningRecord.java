package com.bluepath.app.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "learning_records")
public class LearningRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String recordType;
    public String targetId;
    public String title;
    public String status;
    public long updatedAt;
    public boolean synced;

    public LearningRecord(String recordType, String targetId, String title, String status, long updatedAt, boolean synced) {
        this.recordType = recordType;
        this.targetId = targetId;
        this.title = title;
        this.status = status;
        this.updatedAt = updatedAt;
        this.synced = synced;
    }
}
