
package com.example.contactapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager.LoaderCallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    // My launcher to ask for permission
    private lateinit var permissionLauncher: ActivityResultLauncher<String>;
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>;
    private var selectedContact : ContactModel = ContactModel("", "", "");

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data


                val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
                )

                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        val hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        // Process the contact data as needed
                        // You can also use the contactId to retrieve additional details if necessary
                        // For example, retrieve phone numbers associated with the contact
                        if (hasPhoneNumber > 0) {
                            val phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(contactId),
                                null
                            )
                            phoneCursor?.use { phoneCursor ->
                                if (phoneCursor.moveToNext()) {
                                    val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    val contactName = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                                    var company : String = "";

                                    // Retrieve organization data (company name)
                                    val organizationCursor = contentResolver.query(
                                        ContactsContract.Data.CONTENT_URI,
                                        null,
                                        "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                        arrayOf(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                                        null
                                    )

                                    organizationCursor?.use { orgCursor ->
                                        if (orgCursor.moveToFirst()) {
                                            company = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY))
                                            // Process the company name here
                                        }
                                    }

                                    selectedContact = ContactModel(contactName, phoneNumber, company)
                                    updateCompanyName(selectedContact)

                                    organizationCursor?.close();
                                }
                            }
                            phoneCursor?.close();
                        }
                    }

                    cursor.close()
                }

            }
            updateCompanyName(selectedContact);
        }

        // Assigning value to the launcher variable
        permissionLauncher =  registerForActivityResult(ActivityResultContracts.RequestPermission()){ success ->
            if(!success){
                // In case the user have denied to give permission
                Toast.makeText(this, "Need permissions to access your contact list", Toast.LENGTH_LONG).show()

                return@registerForActivityResult
            }
            getContacts();
        };


        // Creating Button Instance
        val importContactsButton = findViewById<Button>(R.id.import_contacts_button);
        importContactsButton.setOnClickListener  {
            requestPermission();


//            val intent = Intent(this, ContactsScreen::class.java)
//            startActivity(intent)


//            var text = findViewById<TextView>(R.id.des)
//            text.text = dataString;
        }

    }


    private fun updateCompanyName(contact: ContactModel) {
//        Toast.makeText(this, "name: ${contact.name}, phone: ${contact.number}, company: ${contact.compnayName}", Toast.LENGTH_LONG).show();
        var text = findViewById<TextView>(R.id.des);
        text.text = contact.compnayName;
    }


    private fun requestPermission()  {
        try{
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS);
        } catch (e : Error){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }

    }
    private fun fetchContactsInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            val contacts = getContacts()
            // Process the fetched contacts as needed
            withContext(Dispatchers.Main) {

                // Update UI or perform further actions with fetched contacts
                // For example, update your contact list or adapter here
            }
        }
    }

  @SuppressLint("Range")
    private suspend fun getContacts2() : MutableList<ContactModel> {

        var contactList : MutableList<ContactModel>  = mutableListOf<ContactModel>();

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                // Process the contact data as needed
                // You can also use the contactId to retrieve additional details if necessary
                // For example, retrieve phone numbers associated with the contact
                if (hasPhoneNumber > 0) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )
                    phoneCursor?.use { phoneCursor ->
                        while (phoneCursor.moveToNext()) {
                            val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val contactName = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            var company : String = "";

                            // Retrieve organization data (company name)
                            val organizationCursor = contentResolver.query(
                                ContactsContract.Data.CONTENT_URI,
                                null,
                                "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                arrayOf(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                                null
                            )

                            organizationCursor?.use { orgCursor ->
                                if (orgCursor.moveToFirst()) {
                                    company = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY))
                                    // Process the company name here
                                }
                            }


                            if(company.toString().toLowerCase() == "inaequo solutions") {
                                if(contactList.isNotEmpty() ){
                                    if(contactList.last().name != contactName){
                                        contactList.add(ContactModel(contactName, phoneNumber, company));
                                    }
                                } else {
                                    contactList.add(ContactModel(contactName, phoneNumber, company));
                                }

                            }

                            organizationCursor?.close();
                        }
                    }
                    phoneCursor?.close();
                }
            }

            cursor.close()
            println(contactList);
        }

      return contactList;
    }

    @SuppressLint("Range")
    private fun getContacts(){
        var intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        resultLauncher.launch(intent)
    }



//        var contacts = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null)
////        Log.d("contacts are", contacts?.count.toString());
//        dataString = "";
//       if(contacts != null){
//           Toast.makeText(this, "Total Contacts imported are ${contacts.count}", Toast.LENGTH_SHORT).show()
//           var count : Int = 0;
//           while(contacts.moveToNext() && count <=15){
//               count++;
////               var name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));
//               var number = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//               var name = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
////               var phonetic_name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHONETIC_NAME));
//               dataString += "S.no $count, name ${name}, number number\n";
//           }
//       }





}