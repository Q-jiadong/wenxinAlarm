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
	/*�����µ�ͨ����������*/
	private Button addBtn, confirmBtn;
	/*�ı���������ͨѶ¼�����֣�������������ֲ���绰���������ֻ�������ô�죿*/
	private EditText fName;
	/*�ı������趨��绰��ʱ��*/
	private EditText dtPicker;
	/*ȷ������View*/
	private View layout1, layout2;
	/*�����ж�add_member�Ƿ��������flag*/
	private boolean layout2load;
	SimpleAdapter adapter;
	/*����Calendar*/
	final Calendar calendar = Calendar.getInstance();
	/*����ִ�����ӵ�BroadCastReceiver��calendar*/
	static Calendar cldr = Calendar.getInstance();
	//��õ�ǰʱ��
	private int mHour = calendar.get(Calendar.HOUR_OF_DAY);
	private int mMinute = calendar.get(Calendar.MINUTE);
	/*�������ν������ʱ��*/
	static String aName="";
	static String aTime="";
	static String aPhone="";
	//�༭֮ǰ��ʱ��
	static String timebefore="";
	static int id = 0;
	/*�����ܰ�绰�����б�*/
	//SharedPreferences preference;
	//SharedPreferences.Editor editor; 
	SQLiteDatabase db;
	//����PendingIntent
	private PendingIntent malarmPendingintent;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//ͨ��inflaterʹlayout����view
		LayoutInflater inflater = getLayoutInflater();
		layout1 = inflater.inflate(R.layout.upacall, null);
		layout2 = inflater.inflate(R.layout.add_member, null);
		/**
		 * ��Ӧ�õ����ݿ������洢�����б�
		 */
		db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString() + "/alarm.db3", null);
		
		showLayout1();
	}
	
	OnClickListener l1 = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			//��ʾ������ͨ���������
			setContentView(layout2);
			find();
			//�Ƚ�timebefore������գ������Ǹ��²�������updateData()����ִ�в���
			timebefore = "";
			getContacts();//��ǰ������ϵ���б�������layout2��ʾ��ʱ�������һЩ
		}
	};
	
	OnTouchListener l2 = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			//��ȡͨѶ¼��ϵ��
			showContacts();
			return true;
		}
	};
	
	OnTouchListener l3 = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			//����-ʱ��ѡ����
			getDtime();
			return true;
		}
	};
	
	OnClickListener l4 = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			//�ó������ж�fName�Ƿ������ݣ�����û������������
			if(fName.getText().length()!=0)
			{
				//��ʱ�䴫�ݸ�ȫ�ֱ�������Ϊ��������ʱ��
				aTime = dtPicker.getText().toString().trim();
				//ͬһʱ�䲻�������������,�������Ҫ����id
				if(timebefore.length()>1)
				{
					//�ж��Ǹ��»����½�
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
					Toast.makeText(getApplicationContext(), "����ʱ���Ѵ��ڣ�", Toast.LENGTH_LONG).show();
				}
			}
			else
			{
				Toast.makeText(getApplicationContext(), "���ﲻ��Ϊ�գ�", Toast.LENGTH_LONG).show();
			}
		}
	};
	
	private void showLayout1()
	{
		setContentView(layout1);
		layout2load=false;
		addBtn = (Button)findViewById(R.id.addCall);
		//���ؼ�ֻ��һ�β��Ұ󶨣��л�View��Ӱ��
		addBtn.setOnClickListener(l1);
		
		//ͨ�����ݿ��ѯ������Ŀ
		try
		{
			Cursor cursor = db.rawQuery("select * from alarm_info", null);
			//��listװ��adapter����ʾ
			redrawList(cursor);
		}
		catch(SQLiteException se)
		{
			//�������ݱ�
			db.execSQL("create table alarm_info(_id integer primary key autoincrement, alarm_time varchar(50), alarm_name varchar(50))");
			//��������
			Cursor cursor = db.rawQuery("select * from alarm_info", null);
			//��listװ��adapter����ʾ
			redrawList(cursor);
		}
	}
	private void find()
	{
		//�ж�layout2�Ƿ��Ѽ��أ����غ���Ϊtrue
		if(!layout2load)
		{
			//���Ұ�
			fName = (EditText)findViewById(R.id.textname);
			dtPicker = (EditText)findViewById(R.id.dtpicker);
			confirmBtn =(Button)findViewById(R.id.confirm);
			//����־����
			layout2load=true;
			fName.setOnTouchListener(l2);
			dtPicker.setOnTouchListener(l3);
			confirmBtn.setOnClickListener(l4);
			//��ʾ��ǰʱ��
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
		/*�ֻ�ͨѶ¼��URI*/
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		/*ͨѶ¼��ϵ�˵ĳ�ν*/
		String cName;
		/*ͨѶ¼��ϵ�˵ĵ绰*/
		String phoneNumber;
		/*���ͨѶ��string*/
		
		//�洢��ϵ�˵�����
		ArrayList<HashMap<String, String>> contactsList = new ArrayList<HashMap<String, String>>();
		 //�õ�ContentResolver����
		ContentResolver resolver = getContentResolver();
		//ȡ�õ绰���п�ʼһ��Ĺ��
		Cursor cursor = resolver.query(uri, null, null, null, null);
		while(cursor.moveToNext())
		{
			//ȡ����ϵ��
			int columnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);

            cName = cursor.getString(columnIndex);
            //ȡ�õ绰����
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //����ͨ��������ѯ�绰��ֱ����ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            Cursor phone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
            		ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            
            while(phone.moveToNext())
            {
            	//ȡ�õ绰����
                phoneNumber = phone.getString(
                		phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //����Map
                HashMap<String, String> contactMap = new HashMap<String, String>();
                //���ռ�ֵ"name"��"number"��ֵ����Map��
                contactMap.put("name", cName);
                contactMap.put("number", phoneNumber);
                contactsList.add(contactMap);
            }

        }
        cursor.close();
		
		//����һ������������ʾListView
		adapter = new SimpleAdapter(this, 
				contactsList,
				R.layout.contactsinfo,                                 //ÿһ����ϵ����Ϣ�Ĳ���
				new String[]{"name", "number"},                        //Map����ÿһ����Ϣ�����֣��������ݱ������
				new int[]{R.id.contactname, R.id.phonenumber}          //�Զ��岼���и����ؼ���id
		);
		//�����и���Ҫ�����⣬����֮ǰ�ĳ�����Ҫ����ʾ�����ȡͨѶ¼��ʱ���ִ�����getContacts()������ʾͨѶ¼�б�
		//���ǣ���Ϊÿ��ͨѶ¼�б���ʾ��̫�������԰���ʾ�ò��ַŵ���showContacts()���棬����������ʾ������ӽ����ص�ʱ��
		//�Ѿ�������ͨѶ�б�view���ȵ����ȡ��ϵ�˵�ʱ��ֱ����ʾ�ͺ��ˣ���ú�����
	}
	
	private void showContacts()
	{
		/*ͨѶ¼view*/
		ListView listview;
		//ListvViewҪ������ʵ��������whileǰʵ����û���ã�ListView����ʾnull
        //��ʾ��ϵ���б�
        setContentView(R.layout.contactslist);
		listview = (ListView)findViewById(R.id.lview);
		//ΪListView��������
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long itemId)
			{
				//�ڲ�������final
				final TextView tvName, tvPhone;
				//��ǰ��view���沼�ֵĸ����ؼ�����id�ģ������android:id�ĺô�
				tvName = (TextView)view.findViewById(R.id.contactname);
				tvPhone = (TextView)view.findViewById(R.id.phonenumber);
				String getname= (String) tvName.getText();
				//�����ϵ�˵绰
				aPhone = tvPhone.getText().toString().trim();
				fName.setText(getname);
				//�������ƺ����ݸ�ȫ�ֱ�������Ϊ����������ϵ��
				aName = getname;
				//�ص�ǰһ������
				setContentView(layout2);
			}
		});		
	}
	
	private void getDtime()
	{
		/**
		 * ʵ����һ��DatePickerDialog�Ķ���
		 * �ڶ���������һ��DatePickerDialog.OnDateSetListener�����ڲ���
		 * ���û�ѡ������ڵ��done����������onDateSet����
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
						 * �������õ�ʱ�䷢��broadcastreceiver
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
		//sqlite���ݿ���÷���Ȼ��Java��mysql��̫һ��������˼���࣬�����á��������������λ��
		db.execSQL("delete from alarm_info where alarm_time = ?", new String[]{time});
	}
	
	private void updateData(SQLiteDatabase db, String time, String name, String timebefore)
	{
		//���޸ĸ��µ����ݿ�
		db.execSQL("update alarm_info set alarm_time = ?, alarm_name = ? where alarm_time = ?", new String[]{time, name, timebefore});
	}
	
	private void redrawList(Cursor cursor)
	{
		/**
		 * �绰����list
		 * adapter��������ʾ�б�
		 */
		ListView wenxinview = (ListView)findViewById(R.id.wenxinlist);
		SimpleCursorAdapter sAdapter = new SimpleCursorAdapter(this,
				R.layout.callalarm,
				cursor,
				new String[]{"alarm_time", "alarm_name"},
				new int[]{R.id.alarmtime, R.id.alarmcontent},
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
				);
		
		wenxinview.setAdapter(sAdapter);//���б���ʾ����
		//���ÿ����Ŀ���¼���������������ɾ������
		wenxinview.setOnItemLongClickListener(new OnItemLongClickListener()
		{  
			@Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
				final View view2;
				view2 = view;
				//�˵��Ի���
				AlertDialog.Builder builder = new Builder(UpCall.this);
				builder.setItems(getResources().getStringArray(R.array.ItemArray), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int itemcode)
					{
						// TODO Auto-generated method stub
						//itemcode=0 �ǲ˵�����ġ��༭��ܰ���ӡ��itemcode=1�ǲ˵�����ġ����б�ɾ������
						final TextView tvv = (TextView)view2.findViewById(R.id.alarmtime);
						switch(itemcode)
						{
						case 0:
							//����������Ϊ����ʾ�����ϵ�˽���,Ӧ�ð�������������Ϊһ�ַ���
							//��ø���ǰ��ʱ�䣬��Ϊʱ�������ݿ������ָ�����ġ�id��
							timebefore = tvv.getText().toString().trim();
							setContentView(layout2);
							find();
							break;
						case 1:
							//ɾ���б���Ĳ���
							
							String findtime = tvv.getText().toString().trim();
							deleteData(db, findtime);
							String[] a = findtime.split(":");
							int alarmid =Integer.valueOf(a[0])*60 + Integer.valueOf(a[1]);
							setoffAlarmMngr(alarmid);
							/*ɾ����Ӧ����*/
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
		//��������	
		//id�������ڶ����������ֿ���
		/**
		 * ע�����ӹ�����alarmManager
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
		//�ر�����
		//id�������ڶ����������ֿ���
		Intent mAlarmIntent = new Intent(this, AlarmReceiver.class);
		malarmPendingintent = PendingIntent.getBroadcast(getApplicationContext(), id, mAlarmIntent, 0);
		AlarmManager mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		mAlarmManager.cancel(malarmPendingintent);
		Toast.makeText(getApplicationContext(), "������ɾ����", Toast.LENGTH_SHORT).show();
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