package com.example.barsber.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barsber.Adapter.MySalonAdapter;
import com.example.barsber.Adapter.TimeSlotAdapterFirebase;
import com.example.barsber.Common.Common;
import com.example.barsber.Common.SpacesItemDecoration;
import com.example.barsber.Dialog.ExampleDialog;
import com.example.barsber.Interface.ITimeSlotLoadListener;
import com.example.barsber.Model.TimeSlot;
import com.example.barsber.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.model.CalendarItemStyle;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarPredicate;
import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListener {

    View itemCopy = null;

    //Variable
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    AlertDialog dialog;
    List<TimeSlot> timeSlots;
    long timeFromServer=-1;
    int dayFromServer;

    TimeSlotAdapterFirebase adapterFirebase;
    ArrayList<TimeSlot> timeSlotArrayList;
    ArrayList<TimeSlot> weekendSlotArrayList;

    Unbinder unbinder;
    LocalBroadcastManager localBroadcastManager;
    Calendar selected_date;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;
    HorizontalCalendar horizontalCalendar;

    Calendar startDate;
    Calendar endDate;


    private BroadcastReceiver slotDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //if date not upload from internet
            if(Common.date1 && Common.date2)
            {
                //Toast.makeText(context, "Load good date from internet", Toast.LENGTH_SHORT).show();
            }
            else {
                //Toast.makeText(context, "Bad date from internet", Toast.LENGTH_SHORT).show();
                opendialog();
                return;
            }
            chechHourFromInternet();

            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(Common.timeFromServer);
            selected_date = date;



            //create calendar because we dont create it before (in onCreate method)
            if(Common.firstTimeLoadCalendar) {
                Common.firstTimeLoadCalendar = false;
                createCalendar();
            }

            //if we have selected calendar
            //after closing it reset calendar to default
            //(when choose other barber calendar have to be set as default)
            if(Common.reloadCalendar)
            {
                //Toast.makeText(context, "Refresh", Toast.LENGTH_SHORT).show();
                resetSelectedDate();
            }

            weekendSlotArrayList = intent.getParcelableArrayListExtra(Common.WEEKEND_LOAD_DONE);
            //int size = weekendSlotArrayList.size();
            //Toast.makeText(context, "Size = " + String.valueOf(size), Toast.LENGTH_SHORT).show();


            timeSlotArrayList =  intent.getParcelableArrayListExtra(Common.KEY_DAY_LOAD_DONE);
            loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), simpleDateFormat.format(date.getTime()),date);
        }
    };

    private void opendialog() {
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getFragmentManager(),"example dialog");
    }

    private void createCalendar() {
        //int size = weekendSlotArrayList.size();
        //Toast.makeText(getActivity(), "Size = " + String.valueOf(size), Toast.LENGTH_SHORT).show();

        if(Common.timeFromServer==0)
            chechHourFromInternet();

        startDate = Calendar.getInstance();
        startDate.setTimeInMillis(Common.timeFromServer);
        startDate.add(Calendar.DATE, 0);



        endDate = Calendar.getInstance();
        endDate.setTimeInMillis(Common.timeFromServer);
        endDate.add(Calendar.DATE, 2);   //2 day left

        horizontalCalendar = new HorizontalCalendar.Builder(itemCopy,R.id.calendarView)
                .range(startDate,endDate)
                .datesNumberOnScreen(3)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();
        horizontalCalendar.refresh();


        //check this one
        HorizontalCalendarPredicate d = new HorizontalCalendarPredicate() {
            @Override
            public boolean test(Calendar date) {
                if(date.get(Calendar.DAY_OF_WEEK) == 3) {
                    Toast.makeText(getContext(), "Good", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;   // return true if this date should be disabled, false otherwise.
            }

            @Override
            public CalendarItemStyle style() {
                return null;   // create and return a new Style for disabled dates, or null if no styling needed.
            }
        };

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                //selected_date.
                if(Common.currentDate.getTimeInMillis() != date.getTimeInMillis())
                {
                    //selected_date = date;   //This code will not load again if you selected new day same with day selected
                    Common.currentDate =date;

                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                            simpleDateFormat.format(date.getTime()),date);
                }
            }
        });
    }

    private void chechHourFromInternet() {

        final boolean[] ty = {true};

        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");

        //Toast.makeText(getActivity(), "insideTimeFromServer", Toast.LENGTH_SHORT).show();

        //This field will be cached
        //FirebaseDatabase.getInstance().goOnline();
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                Common.date1 =true;

                //Long.class
                Long offset = snapshot.getValue(Long.class);
                //zatrymka z serveru w milisekundach 3 600 000 ce zatrymka w odnu godynu (+2 pojas)
                //Toast.makeText(getActivity(), String.valueOf(offset), Toast.LENGTH_SHORT).show();



                long estimatedServerTimeMs =0;
                if(offset > 0)
                {
                    //time in phone is in the past from server(ex. server time 02.02.2020 , phone time 01.02.2020)
                    estimatedServerTimeMs = System.currentTimeMillis() + offset;
                    //Toast.makeText(getActivity(), "We are in future , offssser = "+ estimatedServerTimeMs, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //time in phone is in the future from server(ex. server time 02.02.2020 , phone time 05.02.2020)
                    estimatedServerTimeMs = System.currentTimeMillis()  + offset;

                    //Toast.makeText(getActivity(), "We are in future , offssser = "+ estimatedServerTimeMs, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getActivity(), "Res =" +String.valueOf(estimatedServerTimeMs), Toast.LENGTH_SHORT).show();
                }

                if(offset < 0)
                    offset*=-1;
//                if(offset > (3600000*48)) {
//                    Toast.makeText(getActivity(), "Diff more than 2 day", Toast.LENGTH_SHORT).show();
//                }


                //ale pojas w +2 firebase tomu + 3 600 000 schob na Ukrainu pominiaty server
                //Toast.makeText(getActivity(), String.valueOf(estimatedServerTimeMs, Toast.LENGTH_SHORT).show();
                //estimatedServerTimeMs += 3600000;

                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
                String   timeZone = new SimpleDateFormat("Z").format(calendar.getTime());
                String t= timeZone.substring(0, 3) + ":"+ timeZone.substring(3, 5);
                //Toast.makeText(getActivity(), t, Toast.LENGTH_SHORT).show();
                String timeZon = timeZone.substring(1,3);
                int timeZoneInt = Integer.parseInt(timeZon);

                if(timeZoneInt < 3)
                {
                    if(timeZoneInt<0)
                        timeZoneInt*=-1;

                    estimatedServerTimeMs += (3600000*timeZoneInt);
                }
                else if(timeZoneInt > 3)
                {
                    timeZoneInt-=3;
                    estimatedServerTimeMs -= (3600000*timeZoneInt);
                }




                Calendar lk = Calendar.getInstance();
                lk.setTimeInMillis(estimatedServerTimeMs);
                String dateServer = android.text.format.DateFormat.format("dd-MM-yyyy",lk).toString();
                //Toast.makeText(getActivity(), dateServer, Toast.LENGTH_SHORT).show();

                String dateFromSer = dateServer.substring(0,2);
                dayFromServer = Integer.parseInt(dateFromSer);


                //Toast.makeText(getActivity(), "chechHourFromInternet", Toast.LENGTH_SHORT).show();

                Common.dayFromServer = dayFromServer;
                Common.timeFromServer = estimatedServerTimeMs;
                //Toast.makeText(getActivity(), String.valueOf(dayFromServer)+ " "+ String.valueOf(estimatedServerTimeMs), Toast.LENGTH_SHORT).show();

                timeFromServer = estimatedServerTimeMs;

                //This field will be cached
                //FirebaseDatabase.getInstance().goOffline();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //This field will be cached
                //FirebaseDatabase.getInstance().goOffline();
                Toast.makeText(getActivity(), "insideTimeFromServer", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Listener was cancelled");
            }
        });

        Common.date2 =true;
    }

    private void loadAvailableTimeSlotOfBarber(String barberId, String bookDate,Calendar date7) {
        dialog.show();
        Common.CURRENT_DAY = bookDate;


        HorizontalCalendarPredicate d = new HorizontalCalendarPredicate() {
            @Override
            public boolean test(Calendar date) {
                if(date.get(Calendar.DAY_OF_WEEK) == 3) {
                    Toast.makeText(getContext(), "Good", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;   // return true if this date should be disabled, false otherwise.
            }

            @Override
            public CalendarItemStyle style() {
                return null;   // create and return a new Style for disabled dates, or null if no styling needed.
            }
        };


        //set to default color because later we change it
        //horizontalCalendar.getConfig().setSelectorColor(-16777216);
        //                    horizontalCalendar.getSelectedItemStyle().setColorBottomText(-16777216).setColorMiddleText(-16777216).setColorTopText(-16777216);
        //                    horizontalCalendar.refresh();

        if(weekendSlotArrayList.size()!=0)
        {
            int day = date7.get(Calendar.DAY_OF_WEEK);

            //Toast.makeText(getActivity(), "Day = " + day, Toast.LENGTH_SHORT).show();

            for(TimeSlot t : weekendSlotArrayList)
            {
                if(t.getSlot() == day)
                {
                    horizontalCalendar.getConfig().setSelectorColor(-16777216);
                    horizontalCalendar.getSelectedItemStyle().setColorBottomText(-16777216).setColorMiddleText(-16777216).setColorTopText(-16777216);
                    horizontalCalendar.refresh();

                    dialog.dismiss();


                    MySalonAdapter adapter = new MySalonAdapter(getActivity());
                    recycler_time_slot.setAdapter(adapter);

                    //display tekst that weekend

                    return;
                }
            }
        }


        //Toast.makeText(getActivity(), String.valueOf(Common.CURRENT_DAY), Toast.LENGTH_SHORT).show();

        //AllSalon/NewYork/Branch/DfJVGYWLGfe9PtvIMhku/Barbers/jKT8yQstXyS0jyYVHpUT
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId());


        //Get information of this barber
        barberDoc.get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DocumentSnapshot documentSnapshot = task.getResult();
                if(documentSnapshot.exists())   //if barber available
                {
                    //Get information
                    // /AllSalon/NewYork/Branch/DfJVGYWLGfe9PtvIMhku/Barbers/jKT8yQstXyS0jyYVHpUT/Working/2020/08/Tt/03_08_2020
                    String year = bookDate.substring(6,10);
                    String month = bookDate.substring(3,5);

                    //Check if year exist (year)
                    DocumentReference chechDateExist = FirebaseFirestore.getInstance()
                            .collection("AllSalon")
                            .document(Common.city)
                            .collection("Branch")
                            .document(Common.currentSalon.getSalonId())
                            .collection("Barbers")
                            .document(Common.currentBarber.getBarberId())
                            .collection("Working")
                            .document(year)
                            .collection(month)
                            .document("Tt");

                    chechDateExist.get().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful())
                        {
                            DocumentSnapshot op = task1.getResult();
                            //if(!op.exists())   //if date not created/available
                            //{
                            //    Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
                            //    iTimeSlotLoadListener.onTimeSlotLoadEmpty();  //To wszystkie daty wolne load
                            //    return;
                            //}
                            //else
                            //{
                                CollectionReference iop= chechDateExist.collection(bookDate);
                                iop.get().addOnCompleteListener(task4 -> {
                                    if(task4.isSuccessful())
                                    {
                                        QuerySnapshot qwerty = task4.getResult();
                                        if (qwerty == null) {
                                            //Toast.makeText(getActivity(), "2", Toast.LENGTH_SHORT).show();
                                            iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                        }

                                        if (qwerty.isEmpty())  //if dont have any appoment
                                        {
                                            //Toast.makeText(getActivity(), "3", Toast.LENGTH_SHORT).show();
                                            iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                            return;
                                        } else {
                                            //Toast.makeText(getActivity(), "Have appoinment", Toast.LENGTH_SHORT).show();
                                            //if have appoiment
                                            timeSlots.clear();  //clear old Slots
                                            for (QueryDocumentSnapshot document : task4.getResult()) {
                                                timeSlots.add(document.toObject(TimeSlot.class));
                                            }
                                            iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                        }
                                    }
                                }).addOnFailureListener(e -> {
                                    //Toast.makeText(getActivity(), "4", Toast.LENGTH_SHORT).show();
                                    iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    return;
                                });
                            //}
                        }
                    }).addOnFailureListener(e -> {
                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                        return;
                    });



                    //Check if date exist (date of zajeta data z zamowieniami)
//                    DocumentReference chechDateExist = FirebaseFirestore.getInstance()
//                            .collection("AllSalon")
//                            .document(Common.city)
//                            .collection("Branch")
//                            .document(Common.currentSalon.getSalonId())
//                            .collection("Barbers")
//                            .document(Common.currentBarber.getBarberId())
//                            .collection("Working")
//                            .document(bookDate);


                    //Check if date exist (date of zajeta data z zamowieniami)
//                    chechDateExist.get().addOnCompleteListener(task12 -> {
//                            if(task12.isSuccessful())
//                            {
//                                DocumentSnapshot dateSnapchot = task12.getResult();
//                                if(!dateSnapchot.exists())   //if date not created/available
//                                {
//                                    //Toast.makeText(getActivity(), "NOT EXIST DATE FOLDER", Toast.LENGTH_SHORT).show();
//                                    //chechHourFromInternet();
//                                    iTimeSlotLoadListener.onTimeSlotLoadEmpty();  //To wszystkie daty wolne load
//                                    return;
//                                }
//
//                                else {
//
//                                    String temp = "Working/";
//                                    temp += bookDate;
//                                    temp += '/';
//                                    temp += "Tt";
//
//                                    CollectionReference date = FirebaseFirestore.getInstance()
//                                            .collection("AllSalon")
//                                            .document(Common.city)
//                                            .collection("Branch")
//                                            .document(Common.currentSalon.getSalonId())
//                                            .collection("Barbers")
//                                            .document(Common.currentBarber.getBarberId())
//                                            .collection(temp);
//
//
//                                    date.get().addOnCompleteListener(task1 -> {
//                                        if (task1.isSuccessful()) {
//                                            QuerySnapshot querySnapshot = task1.getResult();
//                                            if (querySnapshot == null) {
//                                                //chechHourFromInternet();
//                                                iTimeSlotLoadListener.onTimeSlotLoadEmpty();
//                                            }
//
//
//                                            if (querySnapshot.isEmpty())  //if dont have any appoment
//                                            {
//                                                //chechHourFromInternet();
//                                                iTimeSlotLoadListener.onTimeSlotLoadEmpty();
//                                            } else {
//                                                //if have appoiment
//                                                timeSlots.clear();  //clear old Slots
//                                                for (QueryDocumentSnapshot document : task1.getResult()) {
//                                                    timeSlots.add(document.toObject(TimeSlot.class));
//                                                }
//
//                                                //chechHourFromInternet();
//                                                iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
//                                            }
//                                        }
//                                    })
//                                            .addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailer(e.getMessage()));
//                                            //maybe just  .addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadEmpty());
//                                }
//                            }
//                    });
                    //maybe .addOnFailerListener if year not exist
                }
            }
        });
    }

    static BookingStep3Fragment instance;
    public static BookingStep3Fragment getInstance()
    {
        if(instance == null) {
            instance = new BookingStep3Fragment();
        }
        return instance;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iTimeSlotLoadListener = this;
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(slotDoneReceiver,new IntentFilter(Common.KEY_DAY_LOAD_DONE));

        timeSlots = new ArrayList<>();
        timeSlotArrayList = new ArrayList<>();
        weekendSlotArrayList = new ArrayList<>();

        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");  //28_03_2020 (this is key)
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();


        chechHourFromInternet();
    }

    @Override
    public void onDestroy() {
        //with these problem when create calendar, close booking,and open calendar again
        itemCopy =  null;
        Common.reloadCalendar = true;

        //this for future date 5month+
        Common.date1=false;
        Common.date2=false;
        //

        //delete this if will be problem after closing and reopening Booking
        Common.firstTimeLoadCalendar = true;


        localBroadcastManager.unregisterReceiver(slotDoneReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       super.onCreateView(inflater, container, savedInstanceState);
        View itemView=  inflater.inflate(R.layout.fragment_booking_step_three,container,false);
        unbinder = ButterKnife.bind(this,itemView);

        itemCopy = itemView;
        init(itemView);
        return itemView;
    }

    private void init(View itemView) {
        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        //
        //chechHourFromInternet();
        //

        if(itemCopy!=null && !Common.firstTimeLoadCalendar)
            createCalendar();



        //Calendar dateFirst = Calendar.getInstance();
        //dateFirst.setTimeInMillis(Common.timeFromServer);
        //Calendar dateLast = Calendar.getInstance();
        //dateLast.setTimeInMillis(Common.timeFromServer);

        //startDate = Calendar.getInstance();
        //startDate.setTimeInMillis(Common.timeFromServer);

        //String dateServer = android.text.format.DateFormat.format("dd-MM-yyyy",startDate).toString();
        //Toast.makeText(getActivity(), dateServer, Toast.LENGTH_SHORT).show();




//        startDate = Calendar.getInstance();
//        startDate.add(Calendar.DATE, 0);
//
//        endDate = Calendar.getInstance();
//        //endDate.setTimeInMillis(Common.timeFromServer);
//        endDate.add(Calendar.DATE, 2);   //2 day left
//
//        horizontalCalendar = new HorizontalCalendar.Builder(itemView,R.id.calendarView)
//                .range(startDate,endDate)
//                .datesNumberOnScreen(3)
//                .mode(HorizontalCalendar.Mode.DAYS)
//                .defaultSelectedDate(startDate)
//                .build();
//
//
//        HorizontalCalendarPredicate d = new HorizontalCalendarPredicate() {
//            @Override
//            public boolean test(Calendar date) {
//                return false;   // return true if this date should be disabled, false otherwise.
//            }
//
//            @Override
//            public CalendarItemStyle style() {
//                return null;   // create and return a new Style for disabled dates, or null if no styling needed.
//            }
//        };
//
//        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
//            @Override
//            public void onDateSelected(Calendar date, int position) {
//                //chechHourFromInternet();
//
//                if(selected_date.getTimeInMillis() != date.getTimeInMillis())
//                {
//                    selected_date = date;   //This code will not load again if you selected new day same with day selected
//
//                    chechHourFromInternet();
//
//                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
//                            simpleDateFormat.format(date.getTime()));
//                }
//            }
//        });
    }

    //refresh date when user click back button/ or previous button in Step 3(when we must to reset calendar)
    private void resetSelectedDate() {

        horizontalCalendar.selectDate(startDate,true);
        horizontalCalendar.refresh();

        //this new because selected date without it wrong
        chechHourFromInternet();
        selected_date.setTimeInMillis(Common.timeFromServer);
        //

        Common.CURRENT_DAY = selected_date.toString();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        chechHourFromInternet();
        Common.timeFromServer = timeFromServer;

        adapterFirebase = new TimeSlotAdapterFirebase(getContext(),timeSlotArrayList,timeSlotList);
        recycler_time_slot.setAdapter(adapterFirebase);

        dialog.dismiss();
    }


    @Override
    public void onTimeSlotLoadFailer(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        chechHourFromInternet();
        Common.timeFromServer = timeFromServer;

        adapterFirebase = new TimeSlotAdapterFirebase(getContext(),timeSlotArrayList);
        recycler_time_slot.setAdapter(adapterFirebase);

        dialog.dismiss();
    }
}
