package nl.giovanniterlingen.whatsapp;

import java.util.List;
import java.util.Timer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class Conversations extends AppCompatActivity {

	public static final String SET_NOTIFY = "set_notify";
	public static final IntentFilter INTENT_FILTER = createIntentFilter();

	private setNotifyReceiver setNotifyReceiver = new nl.giovanniterlingen.whatsapp.Conversations.setNotifyReceiver();
	private SQLiteDatabase newDB;
	ImageButton sButton;
	String nEdit;
	EditText mEdit;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversations);

		RelativeLayout relativelayout = (RelativeLayout) findViewById(R.id.relativelayout);
		Drawable drawable = getResources().getDrawable(R.drawable.background);

		relativelayout.setBackground(drawable);
		
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		sButton = (ImageButton) findViewById(R.id.send_button);
		mEdit = (EditText) findViewById(R.id.message_text);

		Intent intent = getIntent();
		if (intent.hasExtra("numberpass")) {
			String number = intent.getExtras().getString("numberpass");
			nEdit = number;
		}

		String contactname = ContactsHelper.getContactName(Conversations.this,
				nEdit);

		if (contactname != null) {
			setTitle(contactname);
		} else {
			setTitle(nEdit);
		}
		
		getMessages();

		sButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				String to = nEdit.toString();
				String message = mEdit.getText().toString();

				if (message.isEmpty()) {
					return;

				} else {
					Intent i = new Intent();
					i.setAction(MessageService.ACTION_SEND_MSG);
					i.putExtra("to", to);
					i.putExtra("msg", message);
					sendBroadcast(i);
					mEdit.setText("");
				}
			}
		});

		mEdit.addTextChangedListener(new TextWatcher() {

			String to = nEdit.toString();
			String str = to.replaceAll("\\D+", "");

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				Intent i = new Intent();
				i.setAction(MessageService.ACTION_START_COMPOSING);
				i.putExtra("to", str);
				sendBroadcast(i);

			}

			@Override
			public void afterTextChanged(Editable s) {

				Intent i = new Intent();
				i.setAction(MessageService.ACTION_STOP_COMPOSING);
				i.putExtra("to", str);
				sendBroadcast(i);
			}

		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; goto parent activity.
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
	
	public void getMessages() {

		DatabaseHelper dbHelper = new DatabaseHelper(
				this.getApplicationContext());
		newDB = dbHelper.getWritableDatabase();

		List<String> messages = dbHelper.getMessages(newDB, nEdit);

		String[] array = messages.toArray(new String[messages.size()]);
		
		ListView lv = (ListView) findViewById(R.id.listview);
		
		lv.setDivider(null);
		
		ChatAdapter adapter = new ChatAdapter(Conversations.this, array);
		 
		lv.setAdapter(adapter);

	}

	private static IntentFilter createIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(SET_NOTIFY);
		return filter;
	}

	protected void registerSetTitleReceiver() {
		registerReceiver(setNotifyReceiver, INTENT_FILTER);
	}

	protected void unRegisterSetTitleReceiver() {
		unregisterReceiver(setNotifyReceiver);
	}

	public class setNotifyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SET_NOTIFY)) {
				getMessages();
			}
		}
	}

	protected void onResume() {
		super.onResume();
		registerSetTitleReceiver();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_ONLINE);
		sendBroadcast(i);
	}

	protected void onPause() {
		super.onPause();
		unRegisterSetTitleReceiver();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_OFFLINE);
		sendBroadcast(i);
	}

	public void onBackPressed() {
		Intent intent = new Intent(this, Main.class);
		startActivity(intent);
		finish();
	}

}
