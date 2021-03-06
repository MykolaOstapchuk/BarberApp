package com.example.barsber.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.barsber.Common.Common;
import com.example.barsber.Model.BookingInformation;
import com.example.barsber.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


//
//
//
//
//   https://firebase.google.com/docs/database/android/read-and-write
//
//
//
//
public class BookingStep4Fragment extends Fragment {

    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;
    Unbinder unbinder;

    @BindView(R.id.txt_booking_barber_text)
    TextView txt_booking_barber_text;
    @BindView(R.id.txt_booking_time_text)
    TextView txt_booking_time_next;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;
    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;
    @BindView(R.id.txt_salon_website)
    TextView txt_salon_website;
    @OnClick(R.id.btn_confirm)
    void confirmBooking()
    {
        //Create booking information
        BookingInformation bookingInformation = new BookingInformation();

        bookingInformation.setBarberId(Common.currentBarber.getBarberId());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
        bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setTime(new StringBuilder(Common.currentTimeSlotString).append(" at ").append(simpleDateFormat.format(Common.currentDate.getTime())).toString());
        //bookingInformation.setTime(Common.currentTimeSlotString +" at " +Common.CURRENT_DAY);
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));

        //Submit to barber document
        String bookDate = Common.CURRENT_DAY;

        String year = bookDate.substring(6,10);
        String month = bookDate.substring(3,5);

        DocumentReference bookingDate = FirebaseFirestore.getInstance()
            .collection("AllSalon")
            .document(Common.city)
            .collection("Branch")
            .document(Common.currentSalon.getSalonId())
            .collection("Barbers")
            .document(Common.currentBarber.getBarberId())
            .collection("Working")
            .document(year)
            .collection(month)
            .document("Tt")
                .collection(bookDate)
                .document(String.valueOf(Common.currentTimeSlot)); //bookDate is date simpleFormat with dd_MM_yyyy

        //Write Date
        bookingDate.set(bookingInformation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        resetStaticData();
                        getActivity().finish(); // Close Activity
                        Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentTimeSlotString ="";
        Common.currentSalon =null;
        //Common.currentDate.add(Calendar.DATE,0);
    }

    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData();
        }
    };

    private void setData() {
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        //txt_booking_time_next.setText(new StringBuilder(Common.currentTimeSlotString).append(" at ").append(simpleDateFormat.format(Common.currentDate.getTime())));
        txt_booking_time_next.setText(Common.currentTimeSlotString + " at " +Common.CURRENT_DAY);

        txt_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());
    }

    static BookingStep4Fragment instance;
    public static BookingStep4Fragment getInstance()
    {
        if(instance == null) {
            instance = new BookingStep4Fragment();
        }
        return instance;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply format for date display on Confirm
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_four,container,false);
        unbinder = ButterKnife.bind(this,itemView);

        return itemView;
    }
}
