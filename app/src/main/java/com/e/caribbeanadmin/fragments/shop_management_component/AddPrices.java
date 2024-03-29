package com.e.caribbeanadmin.fragments.shop_management_component;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.e.caribbeanadmin.Adaptor.DealsAdaptor;
import com.e.caribbeanadmin.DatabaseController.DatabaseAddresses;
import com.e.caribbeanadmin.DatabaseController.DatabaseUploader;
import com.e.caribbeanadmin.FireStorageController.FireStorageAddresses;
import com.e.caribbeanadmin.FireStorageController.FireStoreUploader;
import com.e.caribbeanadmin.Listeners.OnFileUploadListeners;
import com.e.caribbeanadmin.Listeners.OnItemLoadListeners;
import com.e.caribbeanadmin.Listeners.OnTaskCompleteListeners;
import com.e.caribbeanadmin.R;
import com.e.caribbeanadmin.Repository.Repository;
import com.e.caribbeanadmin.data_model.Item;
import com.e.caribbeanadmin.data_model.Shop;
import com.e.caribbeanadmin.databinding.AddDealsBinding;
import com.e.caribbeanadmin.databinding.FragmentAddPricesBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.List;


public class AddPrices extends Fragment {


    private FragmentAddPricesBinding mDataBinding;
    private Shop shop;
    private Uri imageUri;
    private AddDealsBinding dealsBinding;
    private static final String TAG = "AddPrices";
    public AddPrices() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            shop=getArguments().getParcelable("shop");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mDataBinding= DataBindingUtil.inflate(inflater,R.layout.fragment_add_prices, container, false);
        dealsBinding=DataBindingUtil.inflate(inflater,R.layout.add_deals,null,false);
        AlertDialog addDealsDialog=new AlertDialog.Builder(getContext()).setView(dealsBinding.getRoot()).create();
        Repository.getShopItems(shop.getId(), DatabaseAddresses.getShopPricesCollection(), new OnItemLoadListeners() {
            @Override
            public void onItemLoaded(List<Item> itemList) {
                mDataBinding.addPricesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mDataBinding.addPricesRecyclerView.setAdapter(new DealsAdaptor(getContext(),itemList,DatabaseAddresses.getShopPricesCollection()));
            }

            @Override
            public void onEmpty() {
                mDataBinding.addPricesMsg.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String e) {
                mDataBinding.addPricesMsg.setVisibility(View.VISIBLE);
                mDataBinding.addPricesMsg.setText("Error "+e);
            }
        });
        mDataBinding.addNewShowRoomItem.setOnClickListener(v->{

            addDealsDialog.show();

        });
        dealsBinding.addDealsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

            }
        });
        dealsBinding.addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dealsBinding.addDealsContent.getText().toString().isEmpty()){
                    dealsBinding.addDealsContent.setError("Empty not allowed");
                }else if(imageUri==null){
                    Toast.makeText(getContext(), "Please Select Image", Toast.LENGTH_SHORT).show();
                }else{
                    Item item=new Item();
                    item.setId(String.valueOf(Calendar.getInstance().getTimeInMillis()));
                    item.setImageUrl(String.valueOf(imageUri));
                    item.setContent(dealsBinding.addDealsContent.getText().toString());
                    item.setShopId(shop.getId());
                    FireStoreUploader.uploadPhotos(imageUri, new OnFileUploadListeners() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    item.setImageUrl(String.valueOf(uri));
                                    DatabaseUploader.publishItem(item, DatabaseAddresses.getShopPricesCollection(), new OnTaskCompleteListeners() {
                                        @Override
                                        public void onTaskSuccess() {
                                            Toast.makeText(getContext(), "Item Added", Toast.LENGTH_SHORT).show();
                                            addDealsDialog.dismiss();
                                        }

                                        @Override
                                        public void onTaskFail(String e) {
                                            Toast.makeText(getContext(), "Error "+e, Toast.LENGTH_SHORT).show();
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
            }
        });

        return mDataBinding.getRoot();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult: ");

        if (requestCode == 1) {
            if(data!=null&&data.getData()!=null){
                dealsBinding.addDealsImage.setImageURI(data.getData());
                imageUri=data.getData();
            }
        }
    }
}