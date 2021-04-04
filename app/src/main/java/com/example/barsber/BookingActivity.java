package com.example.barsber;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.barsber.Adapter.MyViewPagerAdapter;
import com.example.barsber.Common.Common;
import com.example.barsber.Common.NonSwipeViewPage;
import com.example.barsber.Model.Barber;
import com.example.barsber.Model.TimeSlot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class BookingActivity extends AppCompatActivity {

    LocalBroadcastManager localBroadcastManager;
    AlertDialog dialog;
    CollectionReference barberRef;
    CollectionReference slotTimeRef;

    @BindView(R.id.step_view)
    StepView stepView;
    @BindView(R.id.view_pager)
    NonSwipeViewPage viewPager;
    @BindView(R.id.btn_previous_step)
    Button btn_previous_step;
    @BindView(R.id.btn_next_step)
    Button btn_next_step;

    @Override
    public void onBackPressed() {
        if(Common.step == 0 )
        {
            Common.testBarber=false;
            //Common.currentSalon = null;
            //Common.city         = null;
            finish();
        }
        else if(Common.step == 3 || Common.step > 0)
        {
            if(Common.step == 1)
            {
                //Common.currentBarber = null;
            }
            else if(Common.step ==2)
            {
                //here null to selected date?
                Common.reloadCalendar =true;
            }
            Common.step--;
            viewPager.setCurrentItem(Common.step);
        }
    }

    //Event
    @OnClick(R.id.btn_previous_step)
    void previousButton(){
        if(Common.step == 0)
        {
            Common.testBarber=false;
            finish();
        }

        if(Common.step == 3 || Common.step > 0)
        {
            if(Common.step == 2)
            {
                Common.reloadCalendar =true;
            }

            //if to enable next from youtube tutorial 7 time 37:15
            //if(Common.step < 3)
            //{
            //    btn_next_step.setEnabled(true);
            //    setColorButton();
            //}

            Common.step--;
            viewPager.setCurrentItem(Common.step);
        }
    }
    @OnClick(R.id.btn_next_step)
    void nextClick(){
        //Toast.makeText(this, ""+Common.currentSalon.getSalonId(), Toast.LENGTH_SHORT).show();

        if(Common.step < 3 || Common.step ==0)
        {
            Common.step++; //Increase
            if(Common.step == 1)   //After choose salon
            {
                if(Common.currentSalon!=null)
                {
                    //Toast.makeText(this, "Bool = " + String.valueOf(Common.reloadCalendar), Toast.LENGTH_SHORT).show();
                    loadBarberBySalon(Common.currentSalon.getSalonId());
                }
            }
            else if(Common.step == 2)  //Pick time slot
            {
                if(Common.currentBarber!=null)
                {
                    loadWeekend();
                    //Toast.makeText(this, "Bool = " + String.valueOf(Common.reloadCalendar), Toast.LENGTH_SHORT).show();
                    loadTimeSlotOfBarber(Common.currentBarber.getBarberId());
                }
            }
            else if(Common.step ==3)   //Confirm
            {
                if(Common.currentTimeSlot != -1)
                {
                    //Toast.makeText(this, "Bool = " + String.valueOf(Common.reloadCalendar), Toast.LENGTH_SHORT).show();
                    confirmBooking();
                }
            }
            viewPager.setCurrentItem(Common.step);
        }
    }

    DocumentReference barberDoc;
    ArrayList<TimeSlot> weekendSlotArrayList;
    private void loadWeekend() {
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId());

        //Get information of this barber
        barberDoc.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists())   //if barber available
                {
                    ///AllSalon/NewYork/Branch/DfJVGYWLGfe9PtvIMhku/Barbers/fBCva8vwcZucUXXXyL5x/Weekend/BusyDay/FreeDay/0
                    CollectionReference chechDateExist = FirebaseFirestore.getInstance()
                            .collection("AllSalon")
                            .document(Common.city)
                            .collection("Branch")
                            .document(Common.currentSalon.getSalonId())
                            .collection("Barbers")
                            .document(Common.currentBarber.getBarberId())
                            .collection("Weekend")
                            .document("BusyDay")
                            .collection("FreeDay");





                    chechDateExist.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                QuerySnapshot qwerty = task.getResult();
                                if (qwerty == null) {

                                    //iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                }

                                if (qwerty.isEmpty())  //if dont have any appoment
                                {

                                    //iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    return;
                                } else {
                                    //if have wekend
                                    weekendSlotArrayList.clear();  //clear old Slots
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        weekendSlotArrayList.add(document.toObject(TimeSlot.class));
                                    }
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //no weekend
                        }
                    });
                }

            }
        });
    }


    private void confirmBooking() {
        //Send broadcast to fragment step four
        Intent intent = new Intent(Common.KEY_CONFIRM_BOOKING);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void loadTimeSlotOfBarber(String barberId) {
        dialog.show();
        //Now selected all slotTime from firebase
        ///AllSalon/NewYork/Branch/DfJVGYWLGfe9PtvIMhku/Barbers/jKT8yQstXyS0jyYVHpUT/TimeWork

        slotTimeRef =  FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection("TimeWork");


        slotTimeRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<TimeSlot> timeSlots = new ArrayList<>();
                        for(QueryDocumentSnapshot querySnapshot : task.getResult())
                        {
                            TimeSlot timeSlot = querySnapshot.toObject(TimeSlot.class);
                            timeSlot.setTimeId(querySnapshot.getId());  //Get id of Day Working

                            timeSlots.add(timeSlot);
                        }


                        //Read weekend
                        barberDoc = FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.city)
                                .collection("Branch")
                                .document(Common.currentSalon.getSalonId())
                                .collection("Barbers")
                                .document(Common.currentBarber.getBarberId());

                        //Get information of this barber
                        barberDoc.get().addOnCompleteListener(task6 -> {
                            if(task6.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task6.getResult();
                                if (documentSnapshot.exists())   //if barber available
                                {
                                    ///AllSalon/NewYork/Branch/DfJVGYWLGfe9PtvIMhku/Barbers/fBCva8vwcZucUXXXyL5x/Weekend/BusyDay/FreeDay/0
                                    CollectionReference chechDateExist = FirebaseFirestore.getInstance()
                                            .collection("AllSalon")
                                            .document(Common.city)
                                            .collection("Branch")
                                            .document(Common.currentSalon.getSalonId())
                                            .collection("Barbers")
                                            .document(Common.currentBarber.getBarberId())
                                            .collection("Weekend")
                                            .document("BusyDay")
                                            .collection("FreeDay");



                                    chechDateExist.get().addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful())
                                        {
                                            QuerySnapshot qwerty = task1.getResult();
                                            if (qwerty == null) {

                                                //iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                            }
                                            else if (!qwerty.isEmpty())  //if dont have any appoment
                                            {
                                                //if have wekend
                                                weekendSlotArrayList.clear();  //clear old Slots
                                                for (QueryDocumentSnapshot document : task1.getResult()) {
                                                    weekendSlotArrayList.add(document.toObject(TimeSlot.class));
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });



                        //Send BroadCast to BookingStep 3 Fragment to load Recycler
                        Intent intent = new Intent(Common.KEY_DAY_LOAD_DONE);
                        intent.putParcelableArrayListExtra(Common.KEY_DAY_LOAD_DONE,timeSlots);
                        intent.putParcelableArrayListExtra(Common.WEEKEND_LOAD_DONE,weekendSlotArrayList);
                        localBroadcastManager.sendBroadcast(intent);

                        dialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                    }
                });


    }

    private void loadBarberBySalon(String salonId) {
        dialog.show();

        //Now selected all barber of Salon
        ///AllSalon/NewYork/Branch/DfJVGYWLGfe9PtvIMhku/Barbers

        if(!TextUtils.isEmpty(Common.city))
        {
            barberRef = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.city)
                    .collection("Branch")
                    .document(salonId)
                    .collection("Barbers");

            barberRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Barber> barbers = new ArrayList<>();
                        for(QueryDocumentSnapshot barberSnapShot: task.getResult())
                        {
                            Barber barber = barberSnapShot.toObject(Barber.class);
                            barber.setPassword("");  //remove password because in client app
                            barber.setBarberId(barberSnapShot.getId()); //get Id of barber

                            barbers.add(barber);
                        }

                        //Send Broadcast to BookingStet2Fragment to load Recycler
                        Intent intent = new Intent(Common.KEY_BARBER_LOAD_DONE);
                        intent.putParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE,barbers);
                        localBroadcastManager.sendBroadcast(intent);

                        dialog.dismiss();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                        }
                    });
        }



    }

    //Broadcast Receiver
    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int step = intent.getIntExtra(Common.KEY_STEP,0);
            if(step == 1)
            {
                Common.currentSalon = intent.getParcelableExtra(Common.KEY_SALON_STORE);
            }
            else if(step == 2)
            {
                Common.currentBarber = intent.getParcelableExtra(Common.KEY_BARBER_SELECTED);
            }
            else if(step == 3)
            {
                Common.currentTimeSlot = intent.getIntExtra(Common.KEY_TIME_SLOT,-1);
                Common.currentTimeSlotString = intent.getStringExtra(Common.KEY_TIME_STRING);
            }


            btn_next_step.setEnabled(true);
            setColorButton();
        }
    };

    @Override
    protected void onDestroy() {
        Common.test = false;
        Common.testBarber=false;
        Common.testTimeSlot=false;

        //this last delete if problem
        Common.timeFromServer=0;
        //
        Common.reloadCalendar=false;
        Common.firstTimeLoadCalendar=true;


        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        ButterKnife.bind(BookingActivity.this);


        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();


        //loadWeekend
        weekendSlotArrayList = new ArrayList<>();


        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));


        setupStepView();

        //
        btn_previous_step.setEnabled(true);
        //


        setColorButton();

        //View
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4);  //We have 4 fragments so we need keep state of these 4 screne page
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //Show step
                stepView.go(position,true);

                if(position ==0)
                {
                    btn_previous_step.setEnabled(true);
                    //btn_previous_step.setEnabled(false);
                    Common.testBarber=false;

                    if(Common.test) {
                        btn_next_step.setEnabled(true);
                    }
                    else
                        btn_next_step.setEnabled(false);

                    setColorButton();
                    return;
                }
                else
                {
                    btn_previous_step.setEnabled(true);
                }


                //btn_next_step.setEnabled(false);
                if(position == 1) {
                    if (Common.testBarber) {
                        btn_next_step.setEnabled(true);
                    } else
                        btn_next_step.setEnabled(false);
                    setColorButton();
                    return;
                }


                btn_next_step.setEnabled(false);


                setColorButton();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setColorButton() {
        if(btn_next_step.isEnabled())
        {
            btn_next_step.setBackgroundResource(R.color.colorButton);
        }
        else
        {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }

        if(btn_previous_step.isEnabled())
        {
            btn_previous_step.setBackgroundResource(R.color.colorButton);
        }
        else
        {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }
    }

    private void setupStepView(){
        List<String> stepList = new ArrayList<String>();
        stepList.add("Salon");
        stepList.add("Barber");
        stepList.add("Time");
        stepList.add("Confirm");
        stepView.setSteps(stepList);
    }
}