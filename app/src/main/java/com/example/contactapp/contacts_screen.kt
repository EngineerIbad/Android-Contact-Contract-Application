package com.example.contactapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ContactsScreen : AppCompatActivity() {

    var contactList : MutableList<ContactModel>?  = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts_screen)
    }


}