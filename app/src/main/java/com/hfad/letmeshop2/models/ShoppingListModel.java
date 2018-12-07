package com.hfad.letmeshop2.models;


import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.io.Serializable;

public class ShoppingListModel implements Serializable {
    private String shoppingListId, shoppingListName, createdBy;
    @ServerTimestamp
    private Date date;

    public ShoppingListModel() { }

    public ShoppingListModel(String shoppingListId, String shoppingListName, String createdBy) {
        this.shoppingListId = shoppingListId;
        this.shoppingListName = shoppingListName;
        this.createdBy = createdBy;
    }


    public String getShoppingListId() {
        return shoppingListId;
    }

    public String getShoppingListName() {
        return shoppingListName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getDate() {
        return date;
    }
}
