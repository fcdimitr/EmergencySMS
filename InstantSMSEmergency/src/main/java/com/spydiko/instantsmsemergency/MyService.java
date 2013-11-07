package com.spydiko.instantsmsemergency;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by jim on 4/11/2013.
 */
public class MyService extends Service {
	private RemoteControlReceiver mReceiver = null;
	private IntentFilter filter;
	private InstantSMSemergensy instantSMSemergensy;
	public static Vibrator v;
	//	private int count;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instantSMSemergensy = (InstantSMSemergensy) getApplication();
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new RemoteControlReceiver(instantSMSemergensy);
		registerReceiver(mReceiver, filter);
		if (InstantSMSemergensy.debugging) Log.d("service", "end of start");
		RemoteControlReceiver.countPowerOff = 4;
		instantSMSemergensy.setServiceRunning(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		else new MyAsyncTask().execute((Void[]) null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//		count = intent.getIntExtra("stringdata",0);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		instantSMSemergensy.setServiceRunning(false);
	}

	public class MyAsyncTask extends AsyncTask<Void, Void, Void> {

		int count;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			count = 0;
			RemoteControlReceiver.countPowerOff = 0;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

		}

		@Override
		protected Void doInBackground(Void... params) {
			while (instantSMSemergensy.isServiceRunning()) {
				try {
					Thread.sleep(6000);
					if (InstantSMSemergensy.debugging) Log.d("MyService","CountPowerOff = "+RemoteControlReceiver.countPowerOff+" MyCount = "+count);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (RemoteControlReceiver.sentSMS) {
					// Vibrate for 500 milliseconds
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					RemoteControlReceiver.countPowerOff = 0;
					count = 0;
				}
				if (count == RemoteControlReceiver.countPowerOff || RemoteControlReceiver.countPowerOff== count+1){
					count = 0;
					RemoteControlReceiver.countPowerOff = 0;
					RemoteControlReceiver.sentSMS = false;
					if (InstantSMSemergensy.debugging) Log.d("MyService","IF");
				}
				else {
					count = RemoteControlReceiver.countPowerOff;
					if (InstantSMSemergensy.debugging) Log.d("MyService","ELSE");
				}
			} return null;
		}
	}

}
