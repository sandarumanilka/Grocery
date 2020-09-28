package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.Constants;
import com.example.grocery.models.ModelProduct;
import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainSellerActivity extends AppCompatActivity {
    private TextView nameTv, tabProductsTv, tabOrdersTv, filteredProductsTv;
    private ImageButton logoutBtn, editProfileBtn, addProductBtn, filterProductBtn;
    private FirebaseAuth firebaseAuth;
    private RelativeLayout productsRl, ordersRl;
    private RecyclerView ProductsRv;
    private ProgressDialog progressDialog;
    private EditText searchProductEt;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTv = findViewById(R.id.nameTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        tabProductsTv=findViewById(R.id.tabProductsTv);
        tabOrdersTv=findViewById(R.id.tabOrdersTv);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        ProductsRv = findViewById(R.id.ProductsRv);
        searchProductEt = findViewById(R.id.searchProductEt);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        loadAllProducts();

        showProductsUI();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                            adapterProductSeller.getFilter().filter(s);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit prof page
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
                loadMyInfo();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainSellerActivity.this, addProductActivity.class));
            }
        });

        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProductsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersUI();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Products:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                    String selected = Constants.productCategories1[which];
                                    filteredProductsTv.setText(selected);
                                    if(selected.equals("All")){
                                        loadAllProducts();
                                    }
                                    else{
                                        loadFilteredProducts(selected);
                                    }
                            }
                        })
                .show();

            }
        });

    }

    private void loadFilteredProducts(final String selected) {
        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();

                            if(selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }
                        }

                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);

                        ProductsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }

                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);

                        ProductsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void  showProductsUI () {
        //show products ui and hide orders ui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void  showOrdersUI () {
        //show products ui and hide products ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        }
        else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            nameTv.setText(name);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}