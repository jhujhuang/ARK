package com.example.ark;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnMenuItemClickListener {

	static final int MENU_CAMERA = Menu.FIRST;
	static final int MENU_GALLERY = Menu.FIRST + 1;
	
    final Messages messages = new Messages(); // Messages Object that generates random message
	static final int PICK_CONTACT_REQUEST = 1;  // The request code
	static final int SELECT_IMAGE = 2;
	static final int IMAGE_CAPTURE = 3;
	static final int SEND_PHOTO_MESSAGE = 4;
	private PopupMenu popupMenu;
	
	private String recipient_number;
	Uri selectedImage;
	
	ImageView image;	
	EditText textfield;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.textfield = (EditText) findViewById(R.id.input_text);
        ((TextView) findViewById(R.id.show_recipient)).setMovementMethod(new ScrollingMovementMethod());
        
        popupMenu = new PopupMenu(this, findViewById(R.id.photos_choose));
        popupMenu.getMenu().add(Menu.NONE, MENU_CAMERA, Menu.NONE, "Take a Picture");
        popupMenu.getMenu().add(Menu.NONE, MENU_GALLERY, Menu.NONE, "Choose From Gallery");
        popupMenu.setOnMenuItemClickListener(this);
        findViewById(R.id.photos_choose).setOnClickListener(this);
        
        this.image = (ImageView) findViewById(R.id.image);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void randomMessage(View view) {
    	this.textfield.setText(this.messages.getRandomMessage());
    }
    
    public void randomRecipient(View view) {
    	/**
    	Cursor managedCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
    	int size = managedCursor.getCount();
    	
    	boolean found = false;
    	Random rnd = new Random();
    	while(!found) {
    		int index = rnd.nextInt(size);    
    		managedCursor.moveToPosition(index);
    		String name = managedCursor.getString(managedCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
    		found = Boolean.parseBoolean(managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
    		found = true; // TODO: delete later
    		if (found) {
    			Cursor phones = getContentResolver().query( 
    					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
    					ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = "+ name, null, null); 
    			while (phones.moveToNext()) { 
    				String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
    				Log.d("Phone found:", phoneNumber);
    				this.recipient_number = phoneNumber;
    			} 
    			phones.close();
    		}
    	}
    	**/
    	ArrayList<Contact> allContacts = new ArrayList<Contact>();
    	ContentResolver contResv = getContentResolver();
    	Cursor cursor = contResv.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
    	if(cursor.moveToFirst())
    	{
    	    do
    	    {
    	        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

    	        if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
    	        {
    	            Cursor pCur = contResv.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
    	            while (pCur.moveToNext()) 
    	            {
    	                String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    	                String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
    	                allContacts.add(new Contact(contactNumber, contactName));
    	                break;
    	            }
    	            pCur.close();
    	        }

    	    } while (cursor.moveToNext()) ;
    	}
    	
    	Random rd = new Random();
		int i = rd.nextInt(allContacts.size());
		
		Contact randomContact = allContacts.get(i);
		
        TextView recipient = (TextView) findViewById(R.id.show_recipient);
        recipient.setText(randomContact.getName());

        this.recipient_number = randomContact.getNum();
    }
    
    public void onClickContacts(View view) {
    	this.pickContact();
    }
    
    private void pickContact() {
    	Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }
    
    private void setRecipient(Uri contactUri) {
    	
    	// We only need the NUMBER column, because there will be only one row in the result
        String[] projection = {Phone.NUMBER, ContactsContract.Contacts.DISPLAY_NAME};

        // Perform the query on the contact to get the NUMBER column
        // We don't need a selection or sort order (there's only one result for the given URI)
        // CAUTION: The query() method should be called from a separate thread to avoid blocking
        // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
        // Consider using CursorLoader to perform the query.
        Cursor cursor = getContentResolver()
                .query(contactUri, projection, null, null, null);
        cursor.moveToFirst();

        // Retrieve the phone number from the NUMBER column
        int column = cursor.getColumnIndex(Phone.NUMBER);
        this.recipient_number = cursor.getString(column);
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

        // Do something with the phone number...
        TextView recipient = (TextView) findViewById(R.id.show_recipient);
        recipient.setText(name);

    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	super.onActivityResult(requestCode, resultCode, data);
    	// Check which request we're responding to
    	if (requestCode == PICK_CONTACT_REQUEST) {
    		// Make sure the request was successful
    		if (resultCode == RESULT_OK) {
    			// Get the URI that points to the selected contact
    			Uri contactUri = data.getData();
    			this.setRecipient(contactUri);
    		}
    	} else if (requestCode == SELECT_IMAGE) {
    		if (resultCode == Activity.RESULT_OK) {
    			this.selectedImage = data.getData();
    			this.image.setImageURI(this.selectedImage);
    			// TODO maximize when clicked?
    		} 
    	} else if (requestCode == IMAGE_CAPTURE) {
    		if (resultCode == Activity.RESULT_OK) {
    	        Bundle extras = data.getExtras();
    	        Bitmap imageBitmap = (Bitmap) extras.get("data");
    	        this.image.setImageBitmap(imageBitmap);
    	        this.selectedImage = data.getData();
    		}
    	} else if (requestCode == SEND_PHOTO_MESSAGE) {
    		
			Context context = getApplicationContext();
			CharSequence text;
			
			text = "Content forwared to a messages app.";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
    		this.refresh();
    	}
    }
    
    public Uri getImageUri(Context inContext, Bitmap inImage) {
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    	inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    	String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
    	return Uri.parse(path);
    } 
    
    public void send(View view) {
    	
        //text message
		String message_text = this.textfield.getText().toString();
		
		// for toast
		Context context = getApplicationContext();
		CharSequence text;
		
		if (this.recipient_number == null) {
			text = "You have to choose someone to send your kindness :(";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			return; // Not sending anything.
		}
	
		if (message_text.length() <= 0 && this.selectedImage == null) {
			text = "You have to fill something before sending it to someone :(";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			return; // Not sending anything
		}
		
		if (message_text.length() > 0 && this.selectedImage == null) {
			// send text message
			try {
				SmsManager.getDefault().sendTextMessage(recipient_number, null, message_text, null, null);

				text = "Text Message Sent!";
				int duration = Toast.LENGTH_LONG;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				// successful
				this.refresh();

			} catch (Exception e) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				AlertDialog dialog = alertDialogBuilder.create();
				dialog.setMessage(e.getMessage() + " (Couldn't send text message.)");
				dialog.show();
			}
			return; // Only send the text message sent
		}
		

		if (this.selectedImage != null) {
			Intent sendIntent = new Intent(Intent.ACTION_SEND);  
			if (message_text.length() > 0)
				sendIntent.putExtra("sms_body", message_text);
			
			sendIntent.putExtra(Intent.EXTRA_STREAM, this.selectedImage);   
			sendIntent.setType("image/jpg");    
				
			sendIntent.putExtra("address", recipient_number);
			sendIntent.putExtra("exit_on_send", true);
			startActivityForResult(sendIntent, SEND_PHOTO_MESSAGE);
			// the rest done in onActivityResult
		}
    }

	private void refresh() {

		this.textfield.setText("");
		this.image.setImageResource(android.R.color.transparent);
		this.selectedImage = null;
		
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CAMERA:
			this.dispatchTakePictureIntent();
		break;
		case MENU_GALLERY:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);
		break;
		}
		return false;

	}

	@Override
	public void onClick(View arg0) {
		popupMenu.show();
	}
    
	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(takePictureIntent, IMAGE_CAPTURE);
	    }
	}
    
}
