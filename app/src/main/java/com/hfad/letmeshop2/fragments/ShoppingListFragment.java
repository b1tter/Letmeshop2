package com.hfad.letmeshop2.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hfad.letmeshop2.ListActivity;
import com.hfad.letmeshop2.R;
import com.hfad.letmeshop2.holders.ProductViewHolder;
import com.hfad.letmeshop2.models.ProductModel;
import com.hfad.letmeshop2.models.ShoppingListModel;

public class ShoppingListFragment extends Fragment {
    private String shoppingListId;
    private FirebaseFirestore rootRef;
    private CollectionReference shoppingListProductsRef;
    private Boolean izInShoppingList;
    private String userEmail;
    private GoogleApiClient googleApiClient;
    private FirestoreRecyclerAdapter<ProductModel, ProductViewHolder> firestoreRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View listViewFragment = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        Bundle bundle = getArguments();
        izInShoppingList = bundle.getBoolean("izInShoppingList");

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
        }

        //Get googleAPI client
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        ShoppingListModel shoppingListModel = ((ListActivity) getActivity()).getShoppingListModel();
        shoppingListId = shoppingListModel.getShoppingListId();


        //Add new product button
        FloatingActionButton add_button = listViewFragment.findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Use builder to create a new item
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Create new product");

                //EditText to provide the input / text field
                EditText editText = new EditText(getContext());
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                editText.setHint("Name of the list");
                editText.setHintTextColor(Color.GRAY);
                builder.setView(editText);

                //Submit button
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String productName = editText.getText().toString().trim();
                        addProduct(productName);
                    }
                });

                // Cancel/ Back button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                //Feedback for the created list
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        rootRef = FirebaseFirestore.getInstance();
        shoppingListProductsRef = rootRef.collection("products").document(shoppingListId).collection("shoppingListProducts");

        //Display the shopping list
        RecyclerView recyclerView = listViewFragment.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        TextView emptyView = listViewFragment.findViewById(R.id.empty_view);
        //Order the list by the created date
        Query query = shoppingListProductsRef.whereEqualTo("izInShoppingList", izInShoppingList)
                .orderBy("productName", Query.Direction.ASCENDING);


        FirestoreRecyclerOptions<ProductModel> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<ProductModel>()
                .setQuery(query, ProductModel.class)
                .build();

        firestoreRecyclerAdapter =
                new FirestoreRecyclerAdapter<ProductModel, ProductViewHolder>(firestoreRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull ProductModel model) {
                        holder.setProduct(getContext(), listViewFragment, userEmail, shoppingListModel, model);
                    }

                    //Change data for the list Holder
                    @Override
                    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
                        return new ProductViewHolder(view);
                    }

                    //To do when a new list is added
                    @Override
                    public void onDataChanged() {
                        if (getItemCount() == 0 ) {
                            emptyView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public int getItemCount() {
                        return super.getItemCount();
                    }
                };
        recyclerView.setAdapter(firestoreRecyclerAdapter);

        return listViewFragment;
    }

    private void addProduct (String productName) {
        String productId = shoppingListProductsRef.document().getId();
        ProductModel productModel = new ProductModel(productId, productName, izInShoppingList);
        shoppingListProductsRef.document(productId).set(productModel);
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();
        }
    }
}
