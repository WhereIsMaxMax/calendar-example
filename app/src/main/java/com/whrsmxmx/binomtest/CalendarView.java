package com.whrsmxmx.binomtest;

/**
 * Created by Max on 16.02.2017.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class CalendarView extends LinearLayout{

    private static final String TAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMMM yyyy";

    // date format
    private String mDateFormat;

    // current displayed month
    private Calendar mCurrentDate = Calendar.getInstance();

    private EventHandler mEventHandler = null;

    private HashSet<Date> mEvents = new HashSet<>();

    private LinearLayout mHeader;
    private ImageView mBtnPrev;
    private ImageView mBtnNext;
    private TextView mTextDate;
    private GridView mGridView;

    private Date selectedDate = new Date();

    // seasons' rainbow
    int[] rainbow = new int[] {
            R.color.summer,
            R.color.fall,
            R.color.winter,
            R.color.spring
    };

    // month-season association (northern hemisphere, sorry australia :)
    int[] monthSeason = new int[] {2, 2, 3, 3, 3, 0, 0, 0, 1, 1, 1, 2};

    private CalendarAdapter calendarAdapter;

    public CalendarView(Context context)
    {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        updateCalendar();
    }

    private void loadDateFormat(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);

        try {
            // try to load provided date format, and fallback to default otherwise
            mDateFormat = ta.getString(R.styleable.CalendarView_dateFormat);
            if (mDateFormat == null)
                mDateFormat = DATE_FORMAT;
        }
        finally {
            ta.recycle();
        }
    }
    private void assignUiElements(){
        // layout is inflated, assign local variables to components
        mHeader = (LinearLayout)findViewById(R.id.calendar_header);
        mBtnPrev = (ImageView)findViewById(R.id.calendar_prev_button);
        mBtnNext = (ImageView)findViewById(R.id.calendar_next_button);
        mTextDate = (TextView)findViewById(R.id.calendar_date_display);
        mGridView = (GridView)findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers(){
        // add one month and refresh UI
        mBtnNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCurrentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // subtract one month and refresh UI
        mBtnPrev.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCurrentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });

        // long-pressing a day
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick" + position);

                selectedDate = calendarAdapter.getItem(position);
                mEventHandler.onDayPress((Date)parent.getItemAtPosition(position));
                updateCalendar();
            }

        });
    }

    /**
     * Display dates correctly in mGridView
     */
    public void updateCalendar()
    {
        updateCalendar(null);
    }

    /**
     * Display dates correctly in mGridView
     */
    public void updateCalendar(HashSet<Date> events) {
        Log.d("MAIN", "updateCalendar");
        if(events!=null)
            mEvents = events;
        else
            events = mEvents;

        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar) mCurrentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK)-1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        calendarAdapter = new CalendarAdapter(getContext(), cells, events);

        // update mGridView
        mGridView.setAdapter(calendarAdapter);

        // update title
//        SimpleDateFormat sdf = new SimpleDateFormat(mDateFormat, myDateFormatSymbols);
        SimpleDateFormat sdf = new SimpleDateFormat(mDateFormat);
        mTextDate.setText(sdf.format(mCurrentDate.getTime()));

        // set mHeader color according to current season
        int month = mCurrentDate.get(Calendar.MONTH);
        int season = monthSeason[month];
        int color = rainbow[season];

        mHeader.setBackgroundColor(getResources().getColor(color));
    }


    private class CalendarAdapter extends ArrayAdapter<Date> {
        // days with events
        private HashSet<Date> eventDays;

        // for view inflation
        private LayoutInflater inflater;

        CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays) {
            super(context, R.layout.control_calendar_day, days);
            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            // day in question
            final Date date = getItem(position);
            int day = date.getDate();
            int month = date.getMonth();
            int year = date.getYear();

            // today
            Date today = new Date();

            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.control_calendar_day, parent, false);

            view.setBackgroundResource(0);

            // clear styling
            ((TextView)view).setTypeface(null, Typeface.NORMAL);
            ((TextView)view).setTextColor(Color.BLACK);

            if (month != today.getMonth() || year != today.getYear()) {
                // if this day is outside current month, grey it out
                ((TextView)view).setTextColor(getResources().getColor(R.color.greyed_out));
            }
            else if (day == today.getDate()) {
                // if it is today, set it to blue/bold
                ((TextView)view).setTypeface(null, Typeface.BOLD);
                ((TextView)view).setTextColor(getResources().getColor(R.color.today));
            }else if(day == selectedDate.getDate()&&
                    month == selectedDate.getMonth()&&
                    year == selectedDate.getYear()){
//                if this day selected
                ((TextView)view).setTextColor(getResources().getColor(R.color.colorPrimaryLight));
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_day_background));
            }else if(eventDays != null){
                for (Date eventDate : eventDays) {
                    if (eventDate.getDate() == day &&
                            eventDate.getMonth() == month &&
                            eventDate.getYear() == year) {
                        // mark this day for event
                        view.setBackgroundResource(R.drawable.day_event);
                        ((TextView)view).setTextColor(getResources().getColor(R.color.colorPrimaryLight));

//                        mark this day both event and selected
                        if(day == selectedDate.getDate()&&
                                month == selectedDate.getDate()&&
                                year == selectedDate.getYear())
                            view.setBackgroundResource(R.drawable.selected_day_event);
                        break;
                    }
                }
            }

            // set text
            ((TextView)view).setText(String.valueOf(date.getDate()));

            return view;
        }
    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler)
    {
        this.mEventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler
    {
        void onDayPress(Date date);
    }

    public Date getSelectedDate() {
        return selectedDate;
    }
}