package com.e.caribbeanadmin.fragments.shop_management_component;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.e.caribbeanadmin.DatabaseController.DatabaseAddresses;
import com.e.caribbeanadmin.DatabaseController.DatabaseUploader;
import com.e.caribbeanadmin.FireStorageController.FireStorageAddresses;
import com.e.caribbeanadmin.FireStorageController.FireStoreUploader;
import com.e.caribbeanadmin.Listeners.OnFileUploadListeners;
import com.e.caribbeanadmin.Listeners.OnTaskCompleteListeners;
import com.e.caribbeanadmin.R;
import com.e.caribbeanadmin.Util.DialogBuilder;
import com.e.caribbeanadmin.data_model.Shop;
import com.e.caribbeanadmin.data_model.ShopLocation;
import com.e.caribbeanadmin.databinding.FragmentAddLocationsBinding;
import com.e.caribbeanadmin.databinding.FragmentEditShopLocationBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import es.dmoral.toasty.Toasty;


public class EditShopLocation extends Fragment {

    private Uri imageUri;
    private ShopLocation updateShopLocation;
    private FragmentEditShopLocationBinding mDataBinding;
    public EditShopLocation() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            updateShopLocation=getArguments().getParcelable("location");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDataBinding= DataBindingUtil.inflate(inflater,R.layout.fragment_edit_shop_location, container, false);

        if(updateShopLocation!=null){
            mDataBinding.addLocationEditShop.setShopLocation(updateShopLocation);
        }
        mDataBinding.addLocationEditShop.addLocationPublish.setText("Update Location");
        Glide.with(getContext()).load(updateShopLocation.getImageUrl()).into(mDataBinding.addLocationEditShop.addLocationsImage);
        mDataBinding.addLocationEditShop.addLocationsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });
        mDataBinding.addLocationEditShop.addLocationPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog loading= DialogBuilder.getSimpleLoadingDialog(getContext(),"Loading","Updating location");

                if(mDataBinding.addLocationEditShop.addLocationShopName.getText().toString().isEmpty()){
                    mDataBinding.addLocationEditShop.addLocationShopName.setError("Empty Not Allowed");
                }else if(mDataBinding.addLocationEditShop.addLocationLat.getText().toString().isEmpty()){
                    mDataBinding.addLocationEditShop.addLocationLat.setError("Empty Not Allowed");
                }else if(mDataBinding.addLocationEditShop.addLocationLng.getText().toString().isEmpty()){
                    mDataBinding.addLocationEditShop.addLocationLng.setError("Empty Not Allowed ");
                }else if(mDataBinding.addLocationEditShop.addLocationAddress.getText().toString().isEmpty()){
                    mDataBinding.addLocationEditShop.addLocationAddress.setError("Empty Not allowed");
                }else{
                    ShopLocation shopLocation=updateShopLocation;
                    shopLocation.setLat(Double.parseDouble(mDataBinding.addLocationEditShop.addLocationLat.getText().toString()));
                    shopLocation.setLng(Double.parseDouble(mDataBinding.addLocationEditShop.addLocationLng.getText().toString()));
                    shopLocation.setName(mDataBinding.addLocationEditShop.addLocationShopName.getText().toString());
                    shopLocation.setShopAddress(mDataBinding.addLocationEditShop.addLocationAddress.getText().toString());
                    loading.show();
                    if(imageUri!=null){
                        FireStoreUploader.uploadPhotos(imageUri, new OnFileUploadListeners() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        shopLocation.setImageUrl(String.valueOf(uri));
                                        DatabaseUploader.publishShopLocation(shopLocation, DatabaseAddresses.getShopLocationCollection(), new OnTaskCompleteListeners() {
                                            @Override
                                            public void onTaskSuccess() {
                                                // MotionToast.Companion.createToast(getActivity(),"Success",,MotionToast.TOAST_SUCCESS,MotionToast.GRAVITY_BOTTOM,MotionToast.LONG_DURATION,ResourcesCompat.getFont(getContext(),R.font.helvetica_regular));
                                                Toasty.success(getContext(), "Shop Location Added", Toast.LENGTH_SHORT, true).show();
                                                getActivity().getSupportFragmentManager().popBackStack();
                                                loading.dismiss();
                                            }

                                            @Override
                                            public void onTaskFail(String e) {
                                                // MotionToast.Companion.createToast(getActivity(),"Error",e,MotionToast.TOAST_ERROR,MotionToast.GRAVITY_BOTTOM,MotionToast.LONG_DURATION,ResourcesCompat.getFont(getContext(),R.font.helvetica_regular));
                                                Toasty.error(getContext(), "Error "+e, Toast.LENGTH_SHORT, true).show();
                                                loading.dismiss();


                                            }
                                        });

                                    }
                                });
                            }

                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            }

                            @Override
                            public void onFailure(String e) {

                            }
                        }, FireStorageAddresses.getShopComponents());
                    }
                    else{
                        DatabaseUploader.publishShopLocation(shopLocation, DatabaseAddresses.getShopLocationCollection(), new OnTaskCompleteListeners() {
                            @Override
                            public void onTaskSuccess() {
                                // MotionToast.Companion.createToast(getActivity(),"Success",,MotionToast.TOAST_SUCCESS,MotionToast.GRAVITY_BOTTOM,MotionToast.LONG_DURATION,ResourcesCompat.getFont(getContext(),R.font.helvetica_regular));
                                Toasty.success(getContext(), "Shop Location Updated", Toast.LENGTH_SHORT, true).show();
                                getActivity().getSupportFragmentManager().popBackStack();
                                loading.dismiss();


                            }

                            @Override
                            public void onTaskFail(String e) {
                                // MotionToast.Companion.createToast(getActivity(),"Error",e,MotionToast.TOAST_ERROR,MotionToast.GRAVITY_BOTTOM,MotionToast.LONG_DURATION,ResourcesCompat.getFont(getContext(),R.font.helvetica_regular));
                                Toasty.error(getContext(), "Error "+e, Toast.LENGTH_SHORT, true).show();
                                loading.dismiss();


                            }
                        });
                    }
                }
            }
        });
        return mDataBinding.getRoot();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(data!=null&&data.getData()!=null){
                imageUri=data.getData();
                mDataBinding.addLocationEditShop.addLocationsImage.setImageURI(imageUri);
            }
        }
    }
}