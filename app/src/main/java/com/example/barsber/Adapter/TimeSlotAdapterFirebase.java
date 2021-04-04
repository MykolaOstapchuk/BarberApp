package com.example.barsber.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barsber.Common.Common;
import com.example.barsber.Interface.IRecyclerItemSelectedListener;
import com.example.barsber.Model.TimeSlot;
import com.example.barsber.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeSlotAdapterFirebase extends RecyclerView.Adapter<TimeSlotAdapterFirebase.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlotList;
    List<TimeSlot> timeWorkOn;

    //add new choose place
    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;
    final int[] temp = {0};

    public TimeSlotAdapterFirebase(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.timeWorkOn   = new ArrayList<>();

        //new
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    public TimeSlotAdapterFirebase(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.timeWorkOn   = new ArrayList<>();

        //new
        checkHour();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    public TimeSlotAdapterFirebase(Context context, List<TimeSlot> timeSlotList, List<TimeSlot> timeWorkOn) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.timeWorkOn = timeWorkOn;

        //new
        checkHour();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.txt_time_slot.setText(timeSlotList.get(position).getTimeWork());
        String tempName = timeSlotList.get(position).getTimeWork();


        if(timeWorkOn.size() == 0) //If all position  is available    , just show list
        {

                holder.txt_time_slot_description.setText("Available");
                holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black)); //white
                holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));

                holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
        }
        else //If have position is full {booked}
        {
            //Set all to visible
            holder.txt_time_slot_description.setText("Available");
            holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black)); //white
            holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));


            for (TimeSlot slotValue : timeWorkOn)
            {
                //Loop all time slot from server and set different color
                //int slot = Integer.parseInt(slotValue.getSlot().toString());
                int slot = slotValue.getSlot();
                if(slot == position)  //if slot == position
                {
                    //We will set tag for all time  slot is full
                    //So base on tag , we can set all remain card background without change full time slot
                    holder.card_time_slot.setTag(Common.DISABLE_TAG);
                    holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                    holder.txt_time_slot_description.setText("Full");
                    holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                    holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
                }
            }
        }

        //Add all card to list (from firebase database)
        //No add card already in cardView
        if(!cardViewList.contains(holder.card_time_slot))
            cardViewList.add(holder.card_time_slot);


        //Check if card time slot is available
            holder.setiRecyclerItemSelectedListener((view, pos) -> {

                //Loop all card in card list
                for (CardView cardView : cardViewList) {
                    if (cardView.getTag() == null)  //Only available cart time slot be change
                        cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                }

                //To color last good selected slot
                if (temp[0] != 0) {
                    //Toast.makeText(context, "Temp[0]= "+temp[0], Toast.LENGTH_SHORT).show();
                    cardViewList.get(temp[0]).setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                }


                //Our selected card will be change color
                if (cardViewList.get(pos).getTag() == null) {

                    //check if someone already reserved date
                    //
                    //
                    //
                    //
                    //
                    //
                    //

                    Common.currentTimeSlotString = tempName;

                    //To uncolor "last" selected slot , because we selected other slot
                    if (temp[0] != 0) {
                        //Toast.makeText(context, "Temp[0]= "+temp[0], Toast.LENGTH_SHORT).show();
                        cardViewList.get(temp[0]).setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                    }

                    holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                    //After that send Broadcast to enable next button
                    //kastyl
                    temp[0] = pos;
                    Common.testTimeSlot = true;
                    Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);

                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    intent.putExtra(Common.KEY_TIME_SLOT, position);

                    intent.putExtra(Common.KEY_TIME_STRING, tempName);


                    //intent.putExtra(Common.KEY_TIME_SLOT, pos);    //Put index of time slot we have selected
                    intent.putExtra(Common.KEY_STEP, 3);     //Go to step 3
                    localBroadcastManager.sendBroadcast(intent);
                }
            });
    }

    private void checkHour() {
        Calendar lk = Calendar.getInstance();
        lk.setTimeInMillis(Common.timeFromServer);

        String dateServer = android.text.format.DateFormat.format("dd-MM-yyyy",lk).toString();
        String houServer = android.text.format.DateFormat.format("HH:mm:ss",lk).toString();
        //Toast.makeText(context,  houServer, Toast.LENGTH_SHORT).show();


        int HourServer;
        String tem = houServer.substring(0,2);
        HourServer = Integer.parseInt(tem);
        //Toast.makeText(context, String.valueOf(HourServer), Toast.LENGTH_SHORT).show();

        int DayServer;
        tem = dateServer.substring(0,2);
        DayServer = Integer.parseInt(tem);

        int MonthServer;
        tem = dateServer.substring(3,5);
        MonthServer = Integer.parseInt(tem);

        String dayOfMonth = Common.CURRENT_DAY;
        String aa = dayOfMonth.substring(0,2);
        String bb = dayOfMonth.substring(3,5);
        //Toast.makeText(context, dayOfMonth, Toast.LENGTH_SHORT).show();


        //current day selected
        int dayM = Integer.parseInt(aa);
        //current month selected
        int monthM = Integer.parseInt(bb);

        //month
        if(MonthServer < monthM)
        {
            //timeBefore.clear();
            return;
        }
        //month
        else if(MonthServer == monthM)
        {
            //day
            if(DayServer < dayM)
            {
                //timeBefore.clear();
                return;
            }
        }
        //month
        else if(MonthServer == 12 && monthM == 1)
        {
            //default month = december  month year 2000 / 2021
            //timeBefore.clear();
            return;
        }






        int hour24hrs = HourServer;
        tem = houServer.substring(3,5);
        int minutes =Integer.parseInt(tem);
        //Toast.makeText(context, String.valueOf(minutes), Toast.LENGTH_SHORT).show();


        String boxTimeHour="",boxTimeMinute="";

        for (TimeSlot slotValue : timeSlotList) {
            // Check if time passed or no
            //if yes add this box to timeWorkOn
            boxTimeHour = slotValue.getTimeWork().substring(0,2);
            int hour = Integer.parseInt(boxTimeHour);


            //jezeli czas w slotTime jest mniejszy od tego co my mamy teraz to
            //dodaj ten box do boxuZ zajetymi slotami
            if(hour < hour24hrs)
            {
                timeWorkOn.add(slotValue);
                //timeSlotList.remove(slotValue);
            }
            else if(hour == hour24hrs)
            {
                boxTimeMinute=slotValue.getTimeWork().substring(3,5);
                int minute = Integer.parseInt(boxTimeMinute);

                if(minute < minutes)
                {
                    timeWorkOn.add(slotValue);
                    //timeSlotList.remove(slotValue);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_time_slot = itemView.findViewById(R.id.card_time_slot);
            txt_time_slot  = itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = itemView.findViewById(R.id.txt_time_slot_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view,getAdapterPosition());
        }
    }
}
