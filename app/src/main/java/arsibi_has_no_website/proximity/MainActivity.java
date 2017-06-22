package arsibi_has_no_website.proximity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Sensor mProximitySensor;
    SensorManager manager;
    Uri notification;
    MediaPlayer mp;
    TextView tv;
    MyTask task;
    String s="";
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            tv.setText(s);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        task.cancel(true);
        task=new MyTask();
        mp.stop();
        mp.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor=manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mp = MediaPlayer.create(getApplicationContext(), notification);
        tv=(TextView) findViewById(R.id.timer);
        task=new MyTask();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
            if(event.values[0]<event.sensor.getMaximumRange()){
                Log.d("moo",task.getStatus().toString());
                if(!mp.isPlaying()) {
                    if(!task.getStatus().equals( AsyncTask.Status.RUNNING))
                        task.execute();
                }
                Log.d("moo",task.getStatus().toString());
            }
            else {
                Log.d("moo",task.getStatus().toString());
                if (task.getStatus()== AsyncTask.Status.RUNNING||task.getStatus()== AsyncTask.Status.FINISHED) {
                    task.cancel(true);
                    Toast.makeText(getApplicationContext(), "Interrupted", Toast.LENGTH_LONG).show();
                    task =new MyTask();
                    s="10.000";
                    handler.sendEmptyMessage(0);
                }
                if (mp.isPlaying()) {
                    mp.pause();
                    s="10.000";
                    handler.sendEmptyMessage(0);
                }
            }
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        manager.registerListener(this,mProximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void onPause(){
        super.onPause();
        manager.registerListener(this,mProximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    class MyTask extends AsyncTask<Void, Object, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            long curr = System.currentTimeMillis();
            long delay = 0;
            while (delay < 10000 && !isCancelled()) {
                //Log.d("moo",String.format("%d",delay));
                //Log.d("moo",String.valueOf(isCancelled()));
                delay = System.currentTimeMillis() - curr;
                if (delay > 10000)
                    delay = 10000;
                publishProgress(10 - (float) delay / 1000);
                try{
                    Thread.sleep(10);
                }catch (InterruptedException e){}
                //return null;
            }
            return null;
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            s="10.000";
            handler.sendEmptyMessage(0);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!isCancelled()){
                mp.start();
                mp.seekTo(0);
            }
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            Log.d("moo","fault");
            s=String.format("%.3f",(float)values[0]);
            handler.sendEmptyMessage(0);
        }
    }
}
