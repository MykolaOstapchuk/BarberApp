package com.example.barsber.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barsber.Adapter.HomeSliderAdapter;
import com.example.barsber.Adapter.LookBookAdapter;
import com.example.barsber.BookingActivity;
import com.example.barsber.Common.Common;
import com.example.barsber.Interface.IBannerLoadListener;
import com.example.barsber.Interface.ILookBookLoadListener;
import com.example.barsber.Model.Banner;
import com.example.barsber.R;
import com.example.barsber.Service.PicassoImageLoadingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ss.com.bannerslider.Slider;

public class HomeFragment extends Fragment implements ILookBookLoadListener, IBannerLoadListener {

    private Unbinder unbinder;
    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;
    @BindView(R.id.banner_slired)
    Slider banner_slider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;
    @OnClick(R.id.card_view_booking)
    void booking(){
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }


    LookBookAdapter adapter;

    //FireStore
    CollectionReference bannerRef, lookbookRef;

    //interface
    IBannerLoadListener iBannerLoadListener;
    ILookBookLoadListener iLookBookLoadListener;

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this,view);


        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new LookBookAdapter(getActivity(),null);
        recycler_look_book.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(Common.currentUser!=null && user!=null)
        {
            //Init
            Slider.init(new PicassoImageLoadingService());
            iBannerLoadListener = this;
            iLookBookLoadListener =this;

            setUserInformation();
            loadBanner();
            loadLookBook();
        }
        return view;
    }

    private void loadLookBook() {
        lookbookRef.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        List<Banner> lookbooks =new ArrayList<>();
                        for(QueryDocumentSnapshot banneSnapShot : task.getResult())
                        {
                            Banner banner = banneSnapShot.toObject(Banner.class);
                            lookbooks.add(banner);
                        }
                        iLookBookLoadListener.onLookBookLoadSuccess(lookbooks);
                    }
                })
                .addOnFailureListener(e -> {
                    iLookBookLoadListener.onLookBookLoadFailed(e.getMessage());
                });
    }

    private void loadBanner() {
        bannerRef.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        List<Banner> banners =new ArrayList<>();
                        for(QueryDocumentSnapshot banneSnapShot : task.getResult())
                        {
                            Banner banner = banneSnapShot.toObject(Banner.class);
                            banners.add(banner);
                        }
                        iBannerLoadListener.onBannerLoadSuccess(banners);
                    }
                })
                .addOnFailureListener(e -> {
                    iBannerLoadListener.onBannerLoadFailed(e.getMessage());
                });
    }

    private void setUserInformation(){
        layout_user_information.setVisibility(View.VISIBLE);
        txt_user_name.setText(Common.currentUser.getName());
    }

    @Override
    public void onLookBookLoadSuccess(List<Banner> banners) {
        recycler_look_book.setHasFixedSize(true);
        //recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookBookAdapter(getActivity(),banners));
    }

    @Override
    public void onLookBookLoadFailed(String massage) {
        Toast.makeText(getContext(), massage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));
        banner_slider.setInterval(5000);
    }

    @Override
    public void onBannerLoadFailed(String massage) {
        Toast.makeText(getContext(), massage, Toast.LENGTH_SHORT).show();
    }
}