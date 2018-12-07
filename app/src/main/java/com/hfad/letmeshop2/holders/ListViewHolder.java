package com.hfad.letmeshop2.holders;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.letmeshop2.MainActivity;
import com.hfad.letmeshop2.R;
import com.hfad.letmeshop2.ListActivity;
import com.hfad.letmeshop2.models.ShoppingListModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ListViewHolder extends RecyclerView.ViewHolder {
    private TextView listNameTextView, createdByTextView, dateTextView;
    private ShoppingListModel shoppingListModel;


    //get the TextView Id
    public ListViewHolder(View itemView) {
        super(itemView);
        listNameTextView = itemView.findViewById(R.id.listName_TextView);
        createdByTextView = itemView.findViewById(R.id.createdBy_TextView);
        dateTextView = itemView.findViewById(R.id.date_TextView);
    }


    public void displayList(Context context, String userEmail, ShoppingListModel shoppingListModel) {
        String listName = shoppingListModel.getShoppingListName();
        String listId = shoppingListModel.getShoppingListId();
        listNameTextView.setText(listName);

        String createdBy = "By: " + shoppingListModel.getCreatedBy();
        createdByTextView.setText(createdBy);

        Date date = shoppingListModel.getDate();
        if (date != null) {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
            String creationDate = dateFormat.format(date);
            dateTextView.setText(creationDate);
        }

        //Move inside the list
        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ListActivity.class);
            intent.putExtra("shoppingListModel", shoppingListModel);
            v.getContext().startActivity(intent);
        });


        //Update the list with long press
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Use builder to create a new item
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit the name");

                //EditText to provide the input / text field
                EditText editText = new EditText(context);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                editText.setText(listName);
                //display the list name into the input
                editText.setSelection(editText.getText().length());
                editText.setHint("Type the name");
                editText.setHintTextColor(Color.GRAY);
                builder.setView(editText);


                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                Map<String, Object> map = new HashMap<>();

                //Update button
                builder.setPositiveButton("Update", (dialog, which) -> {
                    String newShoppingListName = editText.getText().toString().trim();
                    //Put the new name into the map
                    map.put("shoppingListName", newShoppingListName);
                    //Update the shoppingList name into Firestore
                    rootRef.collection("shoppingLists").document(userEmail)
                            .collection("userShoppingLists")
                            .document(listId).update(map);
                });

                // Cancel button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                //Feedback for the created list
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });
    }
}
