package com.hfad.letmeshop2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hfad.letmeshop2.holders.ListViewHolder;
import com.hfad.letmeshop2.models.ShoppingListModel;


import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String userEmail, userName;
    private Context context;
    private GoogleApiClient googleApiClient;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore rootRef;
    private FirebaseAuth.AuthStateListener authStateListener;
    //Reference to the collection
    private CollectionReference userShoppingListsRef;
    private FirestoreRecyclerAdapter<ShoppingListModel, ListViewHolder> firestoreRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
            userName = googleSignInAccount.getDisplayName();
            Toast.makeText(this, "Hello " + userName + "!!", Toast.LENGTH_LONG).show();
        }

        //Get googleAPI client
         googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseFirestore.getInstance();

        //If the user is not authenticated redirect to LoginActivity
        authStateListener = firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };

        //Add new list button
        FloatingActionButton add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Use builder to create a new item
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Create a new list");

                //EditText to provide the input / text field
                EditText editText = new EditText(MainActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                editText.setHint("Name of the list");
                editText.setHintTextColor(Color.GRAY);
                builder.setView(editText);

                //Submit button
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String shoppingListName = editText.getText().toString().trim();
                        addShoppingList(shoppingListName);
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

        // get a specific firebase collection (userShoppingLists)
        userShoppingListsRef = rootRef.collection("shoppingLists").document(userEmail).collection("userShoppingLists");

        //Display the shopping list
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView emptyView = findViewById(R.id.empty_view);
        //Order the list by the created date
        Query query = userShoppingListsRef.orderBy("date", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<ShoppingListModel> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<ShoppingListModel>()
                .setQuery(query, ShoppingListModel.class)
                .build();

        firestoreRecyclerAdapter =
                new FirestoreRecyclerAdapter<ShoppingListModel, ListViewHolder>(firestoreRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull ListViewHolder holder, int position, @NonNull ShoppingListModel model) {
                        holder.displayList(context, userEmail, model);
                    }

                    //Change data for the list Holder
                    @Override
                    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shooping_list, parent, false);
                        return new ListViewHolder(view);
                    }

                    //To do when a new list is added
                    @Override
                    public void onDataChanged() {

                        if (getItemCount() == 0 ) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public int getItemCount() {
                        return super.getItemCount();
                    }
                };
        recyclerView.setAdapter(firestoreRecyclerAdapter);
    }


    //Sign out and delete the old tokenId to get a new one for next log-in
    private void signOut() {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenId", FieldValue.delete());

        rootRef.collection("users").document(userEmail).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseAuth.signOut();

                if (googleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(googleApiClient);
                }
            }
        });
    }


    private void addShoppingList(String shoppingListName) {
        String shoppingListId = userShoppingListsRef.document().getId();
        //The object of the shoppingListModel
        ShoppingListModel shoppingListModel = new ShoppingListModel(shoppingListId, shoppingListName, userName);
        userShoppingListsRef.document(shoppingListId).set(shoppingListModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            // onSuccessListener message
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "The list was created!");
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        firebaseAuth.addAuthStateListener(authStateListener);
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();
        }
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_button:
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
