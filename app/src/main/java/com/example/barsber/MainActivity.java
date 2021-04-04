package com.example.barsber;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.barsber.Common.Common;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity{

    private static final int APP_REQUEST_CODE = 7117;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @BindView(R.id.btn_login)
    Button btn_login;
    @BindView(R.id.txt_skip)
    TextView txt_skip;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        firebaseAuth = FirebaseAuth.getInstance();

       authStateListener = firebaseAuth1 -> {
         FirebaseUser user = firebaseAuth1.getCurrentUser();
         if(user != null)
         {
             checkUserFromFirebase(user);
         }
       };

        Dexter.withContext(this)
                .withPermissions( Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(user!=null)
                        {
                               Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                               intent.putExtra(Common.IS_LOGIN, true);
                               startActivity(intent);
                               finish();
                        }
                        else
                        {
                            setContentView(R.layout.activity_main);
                            ButterKnife.bind(MainActivity.this);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) { }
                }).check();




//        authStateListener = firebaseAuth -> {
//           if (firebaseAuth.getCurrentUser() != null){
//               //Do anything here which needs to be done after user is set is complete
//               runOnUiThread(() -> {
//                   Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                   intent.putExtra(Common.IS_LOGIN, true);
//                   startActivity(intent);
//               });
//           }
//       };


       //firebaseAuth.addAuthStateListener(authStateListener);



//        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_login)
    void loginUser(){
        //Toast.makeText(this, "Sign In", Toast.LENGTH_SHORT).show();
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(false).setAvailableProviders(providers).build(),APP_REQUEST_CODE);
    }


    @OnClick(R.id.txt_skip)
    void skipLoginJustGoHome(){
       runOnUiThread(() -> {
           Intent intent = new Intent(MainActivity.this,HomeActivity.class);
           intent.putExtra(Common.IS_LOGIN, false);
           startActivity(intent);
       });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra(Common.IS_LOGIN, true);
                    startActivity(intent);
                });


            } //else {
            // Sign in failed. If response is null the user canceled the
            //Toast.makeText(this, "Failed to Sign In", Toast.LENGTH_SHORT).show();
            //}
                else {
                    if (response == null) {
                        //The user has pressed the back button
                        Toast.makeText(getApplicationContext(), "Sign Up Cancelled", Toast.LENGTH_SHORT).show();
                        //finish();
                    } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unknown Sign Up Error", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }


//nullptr = authListener
@Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void checkUserFromFirebase(FirebaseUser user)
    {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra(Common.IS_LOGIN, true);
        startActivity(intent);
        finish();
    }
}