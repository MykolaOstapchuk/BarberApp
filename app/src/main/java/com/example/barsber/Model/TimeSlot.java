package com.example.barsber.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class TimeSlot implements Parcelable {

    private int slot;

    public TimeSlot(){}

    public TimeSlot(int slot) {
        this.slot = slot;
    }

    private String TimeId;


    private String timeWork;

    protected TimeSlot(Parcel in) {
        if (in.readByte() == 0) {
            slot = 0;
        } else {
            slot = in.readInt();
        }
        TimeId = in.readString();
        timeWork = in.readString();
    }

    public static final Creator<TimeSlot> CREATOR = new Creator<TimeSlot>() {
        @Override
        public TimeSlot createFromParcel(Parcel in) {
            return new TimeSlot(in);
        }

        @Override
        public TimeSlot[] newArray(int size) {
            return new TimeSlot[size];
        }
    };

    public String getTimeWork() {
        return timeWork;
    }
    public void setTimeWork(String timeWork) {
        this.timeWork = timeWork;
    }


    public String getTimeId() {
        return TimeId;
    }

    public void setTimeId(String timeId) {
        TimeId = timeId;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (slot == 0) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(slot);
        }
        parcel.writeString(TimeId);
        parcel.writeString(timeWork);
    }
}
