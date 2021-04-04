package com.example.barsber;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.barsber.Common.Common;
import com.example.barsber.Fragments.HomeFragment;
import com.example.barsber.Fragments.ShopingFragment;
import com.example.barsber.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;
    CollectionReference userFef;

    AlertDialog dialog;


    //
    // https://chris.banes.dev/2014/10/17/appcompat-v21/
    //
    // https://stackoverflow.com/questions/22192291/how-to-change-the-status-bar-color-in-android
    //

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);

        //Init
        userFef = FirebaseFirestore.getInstance().collection("User");
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();


        //Check intent ,  if is login =true , enable full access
        //If is login = fasle, just let user around shopping to view
        if(getIntent() != null)
        {
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN,false);
            if(isLogin)
            {
                //this resolve login problem
                Common.login =true;

                if(dialog!=null && !dialog.isShowing())
                {
                    dialog.show();
                    //dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                }

                //check if user is exists
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DocumentReference currentUser = userFef.document(user.getPhoneNumber());
                currentUser.get()
                        .addOnCompleteListener(task -> {
                        if(task.isSuccessful())
                            {
                                runOnUiThread(() -> {
                                    DocumentSnapshot userSnapshot = task.getResult();
                                    if(!userSnapshot.exists())
                                    {
                                        showUpdateDialog(user.getPhoneNumber());
                                    }
                                    else
                                    {
                                        //if user already available in our system
                                        Common.login =true;
                                        Common.currentUser = userSnapshot.toObject(User.class);
                                        bottomNavigationView.setSelectedItemId(R.id.action_home);
                                    }
                                    if(dialog!= null && dialog.isShowing())
                                    {dialog.dismiss();}
                                });
                            }
                        else{
                            Toast.makeText(this, "Error in HomeActivity", Toast.LENGTH_SHORT).show();
                        }
                        });
            }
        }
        //View
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment =null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.action_home)
                    fragment = new HomeFragment();
                else if(item.getItemId() == R.id.action_shopping)
                    fragment = new ShopingFragment();

                return loadFragment(fragment);
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private boolean loadFragment(Fragment fragment) {
        if(fragment!=null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(String phoneNumber) {

        //if(dialog.isShowing())
        //    dialog.dismiss();

        //Init dialog
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("One more step!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);

        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information,null);

        Button btn_update = sheetView.findViewById(R.id.btn_update);
        TextInputEditText edt_name = sheetView.findViewById(R.id.edt_name);
        TextInputEditText edt_address = sheetView.findViewById(R.id.edt_address);

        btn_update.setOnClickListener(view -> {

            if(dialog!= null && !dialog.isShowing())
            {
                dialog.show();
                //dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }


            User user = new User(edt_name.getText().toString(),
                    edt_address.getText().toString(),
                    phoneNumber);
            userFef.document(phoneNumber)
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            bottomSheetDialog.dismiss();
                            if(dialog.isShowing())
                            {dialog.dismiss();}

                            Common.login =true;
                            Common.currentUser = user;
                            bottomNavigationView.setSelectedItemId(R.id.action_home);
                            Toast.makeText(HomeActivity.this, "Thank you", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    bottomSheetDialog.dismiss();
                    if(dialog!=null && dialog.isShowing())
                        {dialog.dismiss();}

                    Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}