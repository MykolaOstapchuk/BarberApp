package com.example.barsber.Interface;

import com.example.barsber.Model.Banner;
import java.util.List;

public interface ILookBookLoadListener {
    void onLookBookLoadSuccess(List<Banner> banners);
    void onLookBookLoadFailed(String massage);
}
