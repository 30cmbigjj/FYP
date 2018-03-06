package com.findmyelderly.findmyelderly.Constant;

import com.google.firebase.auth.FirebaseAuth;
import java.io.Serializable;


/**
 * Created by Isabella on 25/2/2018.
 */

public class Edit implements Serializable{

    private String editId;
    private String familyId;
    private String elderlyId;
    private String email;
    private String userName ="未設定";


    public Edit(){

    }
    public Edit(String email) {
        this.email = email;
        this.elderlyId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.familyId="";
        this.userName = userName;
    }

    public String getEditId() {
        return editId;
    }
    public String getElderlyId() {
        return elderlyId;
    }
    public String getFamilyId() {
        return familyId;
    }
    public String getEmail() {
        return email;
    }
    public String getUserName() {
        return userName;
    }

}