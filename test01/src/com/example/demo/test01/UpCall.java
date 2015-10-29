package com.example.demo.test01;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class UpCall extends Activity
{
	/*创建新的通话对象的组件*/
	private Button addBtn, confirmBtn;
	/*文本框中输入通讯录的名字，并依据这个名字拨打电话，换了名字或重名怎么办？*/
	private EditText fName;
	/*文本框中设定打电话的时间*/
	private EditText dtPicker;
	/*确定几个View*/
	private View layout1, layout2;
	/*用来判断add_member是否载入过的flag*/
	private boolean layout2load;
	SimpleAdapter adapter;
	/*创建Calendar*/
	final Calendar calendar = Calendar.getInstance();
	/*发给执行闹钟的BroadCastReceiver的calendar*/
	static Calendar cldr = Calendar.getInstance();
	//获得当前时间
	private int mHour = calendar.get(Calendar.HOUR_OF_DAY);
	private int mMinute = calendar.get(Calendar.MINUTE);
	/*姓名或称谓、提醒时间*/
	static String aName="";
	static String aTime="";
	static String aPhone="";
	//编辑之前的时间
	static String timebefore="";
	static int id = 0;
	/*存放温馨电话闹钟列表*/
	//SharedPreferences preference;
	//SharedPreferences.Editor editor; 
	SQLiteDatabase db;
	//闹钟PendingIntent
	private PendingIntent malarmPendingintent;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//通过inflater使layout可以view
		LayoutInflater inflater = getLayoutInflater();
		layout1 = inflater.inflate(R.layout.upacall, null);
		layout2 = inflater.inflate(R.layout.add_member, null);
		/**
		 * 本应用的数据库用来存储闹钟列表
		 */
		db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString() + "/alarm.db3", null);
		
		showLayout1();
	}
	
	OnClickListener l1 = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			//显示创建新通话对象界面
			setContentView(layout2);
			find();
			//先将timebefore参量清空，代表不是更新操作，让updateData()方法执行不了
			timebefore = "";
			getContacts();//提前加载联系人列表，这样当layout2显示的时候会流畅一些
		}
	};
	
	OnTouchListener l2 = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			//获取通讯录联系人
			showContacts();
			return true;
		}
	};
	
	OnTouchListener l3 = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			//日期-时间选择器
			getDtime();
			return true;
		}
	};
	
	OnClickListener l4 = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			//用长度来判断fName是否有内容，还有没有其他方法？
			if(fName.getText().length()!=0)
			{
				//将时间传递给全局变量，此为最终闹钟时间
				aTime = dtPicker.getText().toString().trim();
				//同一时间不能添加两个闹钟,多个闹钟要区分id
				if(timebefore.length()>1)
				{
					//判断是更新还是新建
					updateData(db, aTime, aName, timebefore);
					setonAlarmMngr(cldr, id, aTime, aName, aPhone);
					layout2load=false;
					showLayout1();
				}
				else if(isnotExist(aTime))
				{
					insertData(db, aTime, aName);
					setonAlarmMngr(cldr, id, aTime, aName, aPhone);
					layout2load=false;
					showLayout1();
				}
				else
				{
					Toast.makeText(getApplicationContext(), "闹钟时刻已存在！", Toast.LENGTH_LONG).show();
				}
			}
			else
			{
				Toast.makeText(getApplicationContext(), "人物不能为空！", Toast.LENGTH_LONG).show();
			}
		}
	};
	
	private void showLayout1()
	{
		setContentView(layout1);
		layout2load=false;
		addBtn = (Button)findViewById(R.id.addCall);
		//本控件只需一次查找绑定，切换View不影响
		addBtn.setOnClickListener(l1);
		
		//通过数据库查询所有条目
		try
		{
			Cursor cursor = db.rawQuery("select * from alarm_info", null);
			//将list装入adapter并显示
			redrawList(cursor);
		}
		catch(SQLiteException se)
		{
			//创建数据表
			db.execSQL("create table alarm_info(_id integer primary key autoincrement, alarm_time varchar(50), alarm_name varchar(50))");
			//插入数据
			Cursor cursor = db.rawQuery("select * from alarm_info", null);
			//将list装入adapter并显示
			redrawList(cursor);
		}
	}
	private void find()
	{
		//判断layout2是否已加载，加载后置为true
		if(!layout2load)
		{
			//查找绑定
			fName = (EditText)findViewById(R.id.textname);
			dtPicker = (EditText)findViewById(R.id.dtpicker);
			confirmBtn =(Button)findViewById(R.id.confirm);
			//将标志更改
			layout2load=true;
			fName.setOnTouchListener(l2);
			dtPicker.setOnTouchListener(l3);
			confirmBtn.setOnClickListener(l4);
			//显示当前时间
			timeDisplay();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(layout2load && keyCode==KeyEvent.KEYCODE_BACK)
		{
			showLayout1();
			return false;
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
	}
	
	private void getContacts()
	{
		/*手机通讯录的URI*/
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		/*通讯录联系人的称谓*/
		String cName;
		/*通讯录联系人的电话*/
		String phoneNumber;
		/*存放通讯的string*/
		
		//存储联系人的数组
		ArrayList<HashMap<String, String>> contactsList = new ArrayList<HashMap<String, String>>();
		 //得到ContentResolver对象
		ContentResolver resolver = getContentResolver();
		//取得电话本中开始一项的光标
		Cursor cursor = resolver.query(uri, null, null, null, null);
		while(cursor.moveToNext())
		{
			//取得联系人
			int columnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);

            cName = cursor.getString(columnIndex);
            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //避免通过人名查询电话，直接用ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            Cursor phone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
            		ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            
            while(phone.moveToNext())
            {
            	//取得电话号码
                phoneNumber = phone.getString(
                		phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //构造Map
                HashMap<String, String> contactMap = new HashMap<String, String>();
                //按照键值"name"、"number"将值存入Map中
                contactMap.put("name", cName);
                contactMap.put("number", phoneNumber);
                contactsList.add(contactMap);
            }

        }
        cursor.close();
		
		//定义一个适配器来显示ListView
		adapter = new SimpleAdapter(this, 
				contactsList,
				R.layout.contactsinfo,                                 //每一条联系人信息的布局
				new String[]{"name", "number"},                        //Map里面每一项信息的名字，就像数据表的列名
				new int[]{R.id.contactname, R.id.phonenumber}          //自定义布局中各个控件的id
		);
		//这里有个重要的问题，本来之前的程序是要在显示点击获取通讯录的时候就执行这个getContacts()程序显示通讯录列表
		//但是，因为每次通讯录列表显示得太慢，所以把显示得部分放到了showContacts()里面，这样，在显示添加闹钟界面呢的时候
		//已经加载了通讯列表到view，等点击获取联系人的时候直接显示就好了，变得很流畅
	}
	
	private void showContacts()
	{
		/*通讯录view*/
		ListView listview;
		//ListvView要在这里实例化，在while前实例化没有用，ListView会显示null
        //显示联系人列表
        setContentView(R.layout.contactslist);
		listview = (ListView)findViewById(R.id.lview);
		//为ListView绑定适配器
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long itemId)
			{
				//内部类里面final
				final TextView tvName, tvPhone;
				//以前的view里面布局的各个控件是有id的，这就是android:id的好处
				tvName = (TextView)view.findViewById(R.id.contactname);
				tvPhone = (TextView)view.findViewById(R.id.phonenumber);
				String getname= (String) tvName.getText();
				//获得联系人电话
				aPhone = tvPhone.getText().toString().trim();
				fName.setText(getname);
				//将姓名称呼传递给全局变量，此为最终闹钟联系人
				aName = getname;
				//回到前一个布局
				setContentView(layout2);
			}
		});		
	}
	
	private void getDtime()
	{
		/**
		 * 实例化一个DatePickerDialog的对象
		 * 第二个参数是一个DatePickerDialog.OnDateSetListener匿名内部类
		 * 当用户选择好日期点击done会调用里面的onDateSet方法
		 */
		TpDialog tpdialog = new TpDialog(
				UpCall.this, 
				new TpDialog.OnTimeSetListener()
				{
					@Override
					public void onTimeSet(TimePicker view, int mhour, int mminute)
					{
						id = mhour*60+mminute;
						String h = mhour<10?("0"+mhour):(""+mhour);
						String m = mminute<10?("0"+mminute):(""+mminute);
						dtPicker.setText(h + ":" + m);
						/**
						 * 将所设置的时间发给broadcastreceiver
						 */
						cldr.setTimeInMillis(System.currentTimeMillis());
						cldr.set(Calendar.HOUR_OF_DAY, mhour);
						cldr.set(Calendar.MINUTE, mminute);
						//cldr.set(Calendar.SECOND, 0);
						//cldr.set(Calendar.MILLISECOND, 0);
					}
				}, mHour, mMinute, true);
		tpdialog.show();
	}
	
	private void timeDisplay()
	{
		String h = mHour<10?("0"+mHour):(""+mHour);
		String m = mMinute<10?("0"+mMinute):(""+mMinute);
		dtPicker.setText(new StringBuilder().append(h).append(":").append(m).append(" "));
	}
	
	private boolean isnotExist(String atime)
	{
		Cursor cursor = db.rawQuery("select * from alarm_info where alarm_time = ?", new String[]{atime});
		if(cursor.moveToNext())
		{
			return false;
		}
		else
			return true;
	}
	
	private void insertData(SQLiteDatabase db, String time, String name)
	{
		db.execSQL("insert into alarm_info values(null, ?, ?)", new String[]{time, name});
	}
	
	private void deleteData(SQLiteDatabase db, String time)
	{
		//sqlite数据库的用法虽然跟Java用mysql不太一样，但是思想差不多，都是用“？”代替变量的位置
		db.execSQL("delete from alarm_info where alarm_time = ?", new String[]{time});
	}
	
	private void updateData(SQLiteDatabase db, String time, String name, String timebefore)
	{
		//将修改更新到数据库
		db.execSQL("update alarm_info set alarm_time = ?, alarm_name = ? where alarm_time = ?", new String[]{time, name, timebefore});
	}
	
	private void redrawList(Cursor cursor)
	{
		/**
		 * 电话闹钟list
		 * adapter适配器显示列表
		 */
		ListView wenxinview = (ListView)findViewById(R.id.wenxinlist);
		SimpleCursorAdapter sAdapter = new SimpleCursorAdapter(this,
				R.layout.callalarm,
				cursor,
				new String[]{"alarm_time", "alarm_name"},
				new int[]{R.id.alarmtime, R.id.alarmcontent},
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
				);
		
		wenxinview.setAdapter(sAdapter);//将列表显示出来
		//添加每条项目的事件监听，长按触发删除功能
		wenxinview.setOnItemLongClickListener(new OnItemLongClickListener()
		{  
			@Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
				final View view2;
				view2 = view;
				//菜单对话框
				AlertDialog.Builder builder = new Builder(UpCall.this);
				builder.setItems(getResources().getStringArray(R.array.ItemArray), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int itemcode)
					{
						// TODO Auto-generated method stub
						//itemcode=0 是菜单里面的“编辑温馨闹钟”项，itemcode=1是菜单里面的“从列表删除”项
						final TextView tvv = (TextView)view2.findViewById(R.id.alarmtime);
						switch(itemcode)
						{
						case 0:
							//以下两句是为了显示添加联系人界面,应该把这两包起来合为一种方法
							//获得更改前的时间，因为时间是数据库中区分各个项的“id”
							timebefore = tvv.getText().toString().trim();
							setContentView(layout2);
							find();
							break;
						case 1:
							//删除列表项的操作
							
							String findtime = tvv.getText().toString().trim();
							deleteData(db, findtime);
							String[] a = findtime.split(":");
							int alarmid =Integer.valueOf(a[0])*60 + Integer.valueOf(a[1]);
							setoffAlarmMngr(alarmid);
							/*删除对应闹钟*/
							Cursor acursor = db.rawQuery("select * from alarm_info", null);
							redrawList(acursor);
							break;
						default:
							break;
						}
					}
					
				});
				builder.show();
				return true;
            }  
        });
	}
	
	private void setonAlarmMngr(Calendar c, int id, String time, String name, String phonenumber)
	{
		//开启闹钟	
		//id用来在众多闹钟中区分开来
		/**
		 * 注册闹钟管理器alarmManager
		 */
		Intent mAlarmIntent = new Intent(this, AlarmReceiver.class);
		mAlarmIntent.putExtra("time", time);
		mAlarmIntent.putExtra("name", name);
		mAlarmIntent.putExtra("phone", phonenumber);
		malarmPendingintent = PendingIntent.getBroadcast(getApplicationContext(), id, mAlarmIntent, 0);
		AlarmManager mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), malarmPendingintent);
	}
	private void setoffAlarmMngr(int id)
	{
		//关闭闹钟
		//id用来在众多闹钟中区分开来
		Intent mAlarmIntent = new Intent(this, AlarmReceiver.class);
		malarmPendingintent = PendingIntent.getBroadcast(getApplicationContext(), id, mAlarmIntent, 0);
		AlarmManager mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		mAlarmManager.cancel(malarmPendingintent);
		Toast.makeText(getApplicationContext(), "闹钟已删除！", Toast.LENGTH_SHORT).show();
	}
}

/**
class TpDialog extends TimePickerDialog
{
	@Override
	public void onStop()
	{
		//super.onStop();
	}
	public TpDialog(Context context, OnTimeSetListener callBack,  
	           int hourOfDay, int minute, boolean is24HourView)
	{
		super(context, callBack, hourOfDay, minute, is24HourView);  
	       // TODO Auto-generated constructor stub  
	}
}*/