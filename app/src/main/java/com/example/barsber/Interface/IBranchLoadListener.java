package com.example.barsber.Interface;

import com.example.barsber.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> areaNameList);
    void onBranchFailed(String message);
}
