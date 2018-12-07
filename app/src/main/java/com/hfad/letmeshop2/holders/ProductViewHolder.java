package com.hfad.letmeshop2.holders;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.letmeshop2.R;
import com.hfad.letmeshop2.models.ProductModel;
import com.hfad.letmeshop2.models.ShoppingListModel;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


import java.util.HashMap;
import java.util.Map;


public class ProductViewHolder extends RecyclerView.ViewHolder {
    private TextView productNameTextView;

    //get the TextView Id
    public ProductViewHolder(View itemView) {
        super(itemView);
        productNameTextView = itemView.findViewById(R.id.productName_text_view);
    }

    public void setProduct (Context context, View listViewFragment, String userEmail, ShoppingListModel shoppingListModel, ProductModel productModel) {
        String listName = shoppingListModel.getShoppingListName();
        String listId = shoppingListModel.getShoppingListId();
        String productName = productModel.getProductName();
        String productId = productModel.getProductId();
        Boolean izInShoppingList = productModel.getIzInShoppingList();
        productNameTextView.setText(productName);

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference productIdRef = rootRef.collection("products").document(listId)
                .collection("shoppingListProducts").document(productId);


        //Move inside the list
        itemView.setOnClickListener(v -> {
            Map<String, Object> map = new HashMap<>();
            if (izInShoppingList) {
                map.put("izInShoppingList", false);
            } else {
                map.put("izInShoppingList", true);
            }
            productIdRef.update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (izInShoppingList){
                        //Notification
                    }
                }
            });
        });


        //Update the list with long press
        itemView.setOnLongClickListener(v -> {
            //Use builder to create a new item
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit / Delete Product");

            //EditText to provide the input / text field
            EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            editText.setText(productName);
            //display the list name into the input
            editText.setSelection(editText.getText().length());
            editText.setHint("Type the name");
            editText.setHintTextColor(Color.GRAY);
            builder.setView(editText);

            //Update button
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newProductName = editText.getText().toString().trim();
                    Map<String, Object> map = new HashMap<>();
                    map.put("productName", newProductName);
                    productIdRef.update(map);
                }
            });

            // Cancel button
            builder.setNegativeButton("Delete", (dialogInterface, i) -> {
                        productIdRef.delete().addOnSuccessListener(aVoid -> Snackbar.make(listViewFragment, "Product deleted!", Snackbar.LENGTH_LONG).show());
                    });

            //Feedback for the created list
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return true;
        });
    }
}
