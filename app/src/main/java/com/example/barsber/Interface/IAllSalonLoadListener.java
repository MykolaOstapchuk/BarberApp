package com.example.barsber.Interface;

import java.util.List;

public interface IAllSalonLoadListener {
    void onALLSalonLoadSuccess(List<String> salonList);
    void onAllSalonFailed(String message);
}
