package arsibi_has_no_website.proximity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Sensor mProximitySensor;
    SensorManager manager;
    Uri notification;
    MediaPlayer mp;
    Runnable r;
    Thread t;
    TextView tv;
    String s="";
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            tv.setText(s);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor=manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mp = MediaPlayer.create(getApplicationContext(), notification);
        tv=(TextView) findViewById(R.id.timer);
        r=new Runnable() {
            @Override
            public void run() {
                long curr = System.currentTimeMillis();
                long delay = 0;
                while (delay < 10000&&!Thread.interrupted()) {
                  delay = System.currentTimeMillis() - curr;
                  if(delay>10000)
                    delay=10000;
                  s=String.format("%.3f",10-(float)delay/1000);
                  try {
                  Thread.sleep(10);
                  }catch (InterruptedException e){
                  t=new Thread(this);
                  return;
                  }
                  handler.sendEmptyMessage(0);
                }
                if(!Thread.interrupted()){
                mp.start();
                mp.seekTo(1000);
                }
                t=new Thread(this);
            }
        };
        t=new Thread(r);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
            if(event.values[0]<event.sensor.getMaximumRange()){
                if(!mp.isPlaying()) {
                    if(!t.isAlive())
                        t.start();
                }
            }
            else {

                if (t.isAlive()) {
                    t.interrupt();
                    Toast.makeText(getApplicationContext(), "Interrupted", Toast.LENGTH_LONG).show();
                    t = new Thread(r);
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
}
