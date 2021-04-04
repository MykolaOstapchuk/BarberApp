package com.example.barsber.Interface;

import com.example.barsber.Model.TimeSlot;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList);
    void onTimeSlotLoadFailer(String message);
    void onTimeSlotLoadEmpty();
}
