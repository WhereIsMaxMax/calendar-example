package com.whrsmxmx.binomtest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String SHARED_PREF_KEY = "SHARED_PREF_KEY";
    private static final String KEY_EVENTS_DATES_STRING_SET = "STRING_SET_KEY";

    private LinearLayout mFoodContainer;
    private LinearLayout mSportContainer;
    private LinearLayout mMeasurementsContainer;
    private RelativeLayout mFoodPanel;
    private RelativeLayout mSportPanel;
    private RelativeLayout mMeasurementsPanel;

    private boolean isFoodOpen;
    private boolean isSportOpen;
    private boolean isMeasOpen;

    private ArrayList<MyEvent> mMyEvents = new ArrayList<>();

    private CalendarView mCalendarView;
    private HashSet<Date> mEventDaysHashSet;

    private Date mSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        actionBar.setTitle(R.string.title);

        mEventDaysHashSet = new HashSet<>();

        mCalendarView = ((CalendarView)findViewById(R.id.calendar));

        mCalendarView.setEventHandler(new CalendarView.EventHandler()
        {
            @Override
            public void onDayPress(Date date) {

                // show returned day
//                DateFormat df = SimpleDateFormat.getDateInstance();
//                Toast.makeText(MainActivity.this, df.format(date), Toast.LENGTH_SHORT).show();

                clearDisplayedEvents();
                mSelectedDate = date;
                showEventsByDate();
            }
        });

        mSelectedDate = mCalendarView.getSelectedDate();

//        bind
        mFoodPanel = (RelativeLayout)findViewById(R.id.food_title);
        mFoodPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openCloseFood();
            }
        });
        mFoodContainer = (LinearLayout)findViewById(R.id.food_container);
        mSportContainer = (LinearLayout) findViewById(R.id.sport_container);
        mSportPanel = (RelativeLayout)findViewById(R.id.sport_title);
        mSportPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openCloseSport();
            }
        });
        mMeasurementsContainer = (LinearLayout) findViewById(R.id.measurements_container);
        mMeasurementsPanel = (RelativeLayout)findViewById(R.id.measurements_title);
        mMeasurementsPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openCloseMeas();
            }
        });
    }

    private void clearDisplayedEvents() {
        mFoodContainer.removeAllViews();
        mSportContainer.removeAllViews();
        mMeasurementsContainer.removeAllViews();
    }

    private void openCloseFood() {
        isFoodOpen = !isFoodOpen;
        ((ImageView)mFoodPanel.findViewById(R.id.open_food_ic)).setImageDrawable(
                isFoodOpen?getResources().getDrawable(R.drawable.ic_up):
                        getResources().getDrawable(R.drawable.ic_down));
        mFoodContainer.setVisibility(isFoodOpen?View.VISIBLE:View.GONE);
    }

    private void openCloseSport() {
        isSportOpen = !isSportOpen;
        ((ImageView) mSportPanel.findViewById(R.id.open_sport_ic)).setImageDrawable(
                isSportOpen?getResources().getDrawable(R.drawable.ic_up):
                        getResources().getDrawable(R.drawable.ic_down));
        mSportContainer.setVisibility(isSportOpen?View.VISIBLE:View.GONE);
    }

    private void openCloseMeas() {
        isMeasOpen = !isMeasOpen;
        ((ImageView)mMeasurementsPanel.findViewById(R.id.open_meas_ic)).setImageDrawable(
                isMeasOpen?getResources().getDrawable(R.drawable.ic_up):
                        getResources().getDrawable(R.drawable.ic_down));
        mMeasurementsContainer.setVisibility(isMeasOpen?View.VISIBLE:View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_settings){
            showAlertDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAlertDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText)dialogView.findViewById(R.id.edittext);
                        TimePicker timePicker = (TimePicker)dialogView.findViewById(R.id.time);
                        if(!TextUtils.isEmpty(editText.getText())){
                            MyEvent myEvent = new MyEvent(mSelectedDate,
                                    editText.getText().toString(),
                                    ((Spinner)dialogView.findViewById(R.id.spinner)).getSelectedItemPosition(),
                                    timePicker.getCurrentHour() + "." + timePicker.getCurrentMinute(),
                                    false);
                            mMyEvents.add(myEvent);
                            if(!mMyEvents.isEmpty()){
                                addEvent(myEvent.mTitle, myEvent.mType, myEvent.mTime, myEvent.isEnabled());
                                mEventDaysHashSet.add(myEvent.getDate());
                                mCalendarView.updateCalendar(mEventDaysHashSet);
                            }
                        }}
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        ((EditText)dialogView.findViewById(R.id.edittext)).addTextChangedListener(new TextWatcher() {
                  @Override
                  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                  }

                  @Override
                  public void onTextChanged(CharSequence s, int start, int before, int count) {

                  }

                  @Override
                  public void afterTextChanged(Editable s) {
                    if(s.length()!=0){
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }
                  }
              }
        );
        dialog.show();
    }

    private void showEventsByDate() {
        for(MyEvent event : mMyEvents){
            if(event.getDate().getDate() == mSelectedDate.getDate())
                addEvent(event.mTitle, event.mType, event.mTime, event.isEnabled());
        }
    }

    private void addEvent(String title, int type, String time, boolean isEnabled) {
        switch (type){
            case 0:
                mFoodContainer.addView(createLine(title, time, isEnabled));
                break;
            case 1:
                mSportContainer.addView(createLine(title, time, isEnabled));
                break;
            case 2:
                mMeasurementsContainer.addView(createLine(title, time, isEnabled));
                break;
            default:
                Log.d("MAIN", "unhandled value selected");
        }
    }

    private RelativeLayout createLine(final String title, final String time, boolean isEmabled) {
        RelativeLayout line = (RelativeLayout) getLayoutInflater().inflate(R.layout.container_line, null);
        ((TextView)line.findViewById(R.id.line_title)).setText(title);
        ((TextView)line.findViewById(R.id.line_time)).setText(time);
        ((Switch)line.findViewById(R.id.switch_view)).setChecked(isEmabled);
        ((Switch)line.findViewById(R.id.switch_view)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(MyEvent event : mMyEvents){
                    if(event.getTitle().equals(title))
                        event.setEnabled(isChecked);
                }
            }
        });
        return line;
    }

    private class MyEvent {
        private Date mDate;
        private String mTitle;
        private int mType;
        private String mTime;
        private boolean mIsEnabled;

        MyEvent(Date date, String title, int type, String time, boolean isEnabled){
            mDate = date;
            mTime = time;
            mType = type;
            mTitle = title;
            mIsEnabled = isEnabled;
        }

        Date getDate() {
            return mDate;
        }
        String getTitle() {
            return mTitle;
        }
        String getTime() {
            return mTime;
        }
        boolean isEnabled() {
            return mIsEnabled;
        }
        int getType() {
            return mType;
        }

        void setEnabled(boolean enabled) {
            mIsEnabled = enabled;
        }
    }

    @Override
    protected void onPause() {
//        todo:store mEventDaysHashSet
        SharedPreferences sp = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE);
        sp.edit().putStringSet(KEY_EVENTS_DATES_STRING_SET, convertDaysHashSet(mEventDaysHashSet)).apply();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE);
        if(sp.contains(KEY_EVENTS_DATES_STRING_SET))
            mEventDaysHashSet = convertDaysStringHashSet(sp.getStringSet(KEY_EVENTS_DATES_STRING_SET, null));
    }

    private HashSet<String> convertDaysHashSet(HashSet<Date> eventDaysHashSet) {
        HashSet<String> hashSet = new HashSet<>();
        for(Date s : eventDaysHashSet){
            hashSet.add(String.valueOf(s.getTime()));
        }
        return hashSet;
    }

    private HashSet<Date> convertDaysStringHashSet(Set<String> eventDaysStringHashSet) {
        HashSet<Date> hashSet = new HashSet<>();
        for(String s : eventDaysStringHashSet){
            hashSet.add(new Date(Long.valueOf(s)));
        }
        return hashSet;
    }
}
