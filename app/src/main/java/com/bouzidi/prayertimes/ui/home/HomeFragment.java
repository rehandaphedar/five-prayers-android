package com.bouzidi.prayertimes.ui.home;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bouzidi.prayertimes.MainActivity;
import com.bouzidi.prayertimes.R;
import com.bouzidi.prayertimes.notifier.NotifierHelper;
import com.bouzidi.prayertimes.timings.ComplementaryTimingEnum;
import com.bouzidi.prayertimes.timings.DayPrayer;
import com.bouzidi.prayertimes.timings.PrayerEnum;
import com.bouzidi.prayertimes.ui.clock.ClockView;
import com.bouzidi.prayertimes.utils.PrayerUtils;
import com.bouzidi.prayertimes.utils.TimingUtils;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class HomeFragment extends Fragment {

    private Date todayDate;
    private CountDownTimer TimeRemainingCTimer;
    private MainActivity mainActivity;

    private TextView countryTextView;
    private TextView locationTextView;
    private TextView hijriTextView;
    private TextView gregorianTextView;
    private TextView prayerNametextView;
    private TextView prayerTimetextView;
    private TextView timeRemainingTextView;
    private TextView fajrTimingTextView;
    private TextView dohrTimingTextView;
    private TextView asrTimingTextView;
    private TextView maghribTimingTextView;
    private TextView ichaTimingTextView;
    private TextView sunriseTimingTextView;
    private TextView sunsetTimingTextView;
    private TextView fajrLabel;
    private TextView dohrLabel;
    private TextView asrLabel;
    private TextView maghribLabel;
    private TextView ichaLabel;

    private ClockView fajrClock;
    private ClockView dohrClock;
    private ClockView asrClock;
    private ClockView maghribClock;
    private ClockView ichaClock;

    private CircularProgressBar circularProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel dashboardViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(root);

        dashboardViewModel.getDayPrayers().observe(getViewLifecycleOwner(), dayPrayer -> {
            updateNextPrayerViews(dayPrayer);
            updateDatesTextViews(dayPrayer);
            updateTimingsTextViews(dayPrayer);
            NotifierHelper.scheduleNextPrayerAlarms(mainActivity, dayPrayer);
        });
        return root;
    }

    private void initializeViews(View root) {
        mainActivity = (MainActivity) getActivity();

        todayDate = Calendar.getInstance().getTime();

        locationTextView = root.findViewById(R.id.location_text_view);
        countryTextView = root.findViewById(R.id.country_text_view);
        hijriTextView = root.findViewById(R.id.hijriTextView);
        gregorianTextView = root.findViewById(R.id.gregorianTextView);
        prayerNametextView = root.findViewById(R.id.prayerNametextView);
        prayerTimetextView = root.findViewById(R.id.prayerTimetextView);
        timeRemainingTextView = root.findViewById(R.id.timeRemainingTextView);
        circularProgressBar = root.findViewById(R.id.circularProgressBar);

        fajrClock = root.findViewById(R.id.farj_clock_view);
        dohrClock = root.findViewById(R.id.dohr_clock_view);
        asrClock = root.findViewById(R.id.asr_clock_view);
        maghribClock = root.findViewById(R.id.maghreb_clock_view);
        ichaClock = root.findViewById(R.id.ichaa_clock_view);

        fajrTimingTextView = root.findViewById(R.id.fajr_timing_text_view);
        dohrTimingTextView = root.findViewById(R.id.dohr_timing_text_view);
        asrTimingTextView = root.findViewById(R.id.asr_timing_text_view);
        maghribTimingTextView = root.findViewById(R.id.maghrib_timing_text_view);
        ichaTimingTextView = root.findViewById(R.id.icha_timing_text_view);

        sunriseTimingTextView = root.findViewById(R.id.sunrise_timing_text_view);
        sunsetTimingTextView = root.findViewById(R.id.sunset_timing_text_view);

        fajrLabel = root.findViewById(R.id.fajr_label_text_view);
        dohrLabel = root.findViewById(R.id.dohr_label_text_view);
        asrLabel = root.findViewById(R.id.asr_label_text_view);
        maghribLabel = root.findViewById(R.id.maghrib_label_text_view);
        ichaLabel = root.findViewById(R.id.icha_label_text_view);
    }

    private void updateTimingsTextViews(DayPrayer dayPrayer) {
        Map<PrayerEnum, String> timings = dayPrayer.getTimings();

        String fajrTiming = timings.get(PrayerEnum.FAJR);
        String dohrTiming = timings.get(PrayerEnum.DHOHR);
        String asrTiming = timings.get(PrayerEnum.ASR);
        String maghribTiming = timings.get(PrayerEnum.MAGHRIB);
        String ichaTiming = timings.get(PrayerEnum.ICHA);

        updateClockTime(fajrClock, getTimingPart(Objects.requireNonNull(fajrTiming))[0], getTimingPart(Objects.requireNonNull(fajrTiming))[1]);
        updateClockTime(dohrClock, getTimingPart(Objects.requireNonNull(dohrTiming))[0], getTimingPart(Objects.requireNonNull(dohrTiming))[1]);
        updateClockTime(asrClock, getTimingPart(Objects.requireNonNull(asrTiming))[0], getTimingPart(Objects.requireNonNull(asrTiming))[1]);
        updateClockTime(maghribClock, getTimingPart(Objects.requireNonNull(maghribTiming))[0], getTimingPart(Objects.requireNonNull(maghribTiming))[1]);
        updateClockTime(ichaClock, getTimingPart(Objects.requireNonNull(ichaTiming))[0], getTimingPart(Objects.requireNonNull(ichaTiming))[1]);

        fajrTimingTextView.setText(fajrTiming);
        dohrTimingTextView.setText(dohrTiming);
        asrTimingTextView.setText(asrTiming);
        maghribTimingTextView.setText(maghribTiming);
        ichaTimingTextView.setText(ichaTiming);

        sunriseTimingTextView.setText(dayPrayer.getComplementaryTiming().get(ComplementaryTimingEnum.SUNRISE));
        sunsetTimingTextView.setText(dayPrayer.getComplementaryTiming().get(ComplementaryTimingEnum.SUNSET));

        fajrLabel.setText(R.string.FAJR);
        dohrLabel.setText(R.string.DHOHR);
        asrLabel.setText(R.string.ASR);
        maghribLabel.setText(R.string.MAGHRIB);
        ichaLabel.setText(R.string.ICHA);
    }

    private String[] getTimingPart(String timing) {
        return timing.split(":");
    }

    private void updateClockTime(ClockView clock, String hour, String minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
        clock.setColor(0xFF17C5FF);
        clock.setCalendar(calendar);
    }

    @Override
    public void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }

    private void updateNextPrayerViews(DayPrayer dayPrayer) {
        Map<PrayerEnum, String> timings = dayPrayer.getTimings();

        PrayerEnum nextPrayerKey = PrayerUtils.getNextPrayer(timings, todayDate);
        PrayerEnum previousPrayerKey = PrayerUtils.getPreviousPrayerKey(nextPrayerKey);

        long timeRemaining = TimingUtils.getRemainingTiming(todayDate, Objects.requireNonNull(timings.get(nextPrayerKey)));
        long timeBetween = TimingUtils.getTimingBetween(Objects.requireNonNull(timings.get(previousPrayerKey)), Objects.requireNonNull(timings.get(nextPrayerKey)));

        String prayerName = mainActivity.getResources().getString(
                getResources().getIdentifier(nextPrayerKey.toString(), "string", mainActivity.getPackageName()));

        prayerNametextView.setText(prayerName);
        prayerTimetextView.setText(timings.get(nextPrayerKey));
        timeRemainingTextView.setText(TimingUtils.formatTimeForTimer(timeRemaining));

        startAnimationTimer(timeRemaining, timeBetween, dayPrayer);
    }

    private void updateDatesTextViews(DayPrayer dayPrayer) {
        String hijriMonth = mainActivity.getResources().getString(
                getResources().getIdentifier("hijri_month_" + dayPrayer.getHijriMonthNumber(), "string", mainActivity.getPackageName()));

        String hijriDate = TimingUtils.formatDate(
                dayPrayer.getHijriDay(),
                hijriMonth,
                dayPrayer.getHijriYear()
        );

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd MMMM, yyyy", Locale.getDefault());
        SimpleDateFormat TimeZoneFormat = new SimpleDateFormat("ZZZZZ", Locale.getDefault());

        String gregorianDate = simpleDateFormat.format(new Date(dayPrayer.getTimestamp()));

        hijriTextView.setText(StringUtils.capitalize(hijriDate));
        gregorianTextView.setText(StringUtils.capitalize(gregorianDate));
        String locationText = dayPrayer.getCity();
        String country = dayPrayer.getCountry() + " (" + TimeZoneFormat.format(todayDate) + ")";
        countryTextView.setText(StringUtils.capitalize(country));
        locationTextView.setText(StringUtils.capitalize(locationText));
    }

    private float getProgressBarPercentage(long timeRemaining, long timeBetween) {
        return 100 - ((float) (timeRemaining * 100) / (timeBetween));
    }

    private void startAnimationTimer(final long timeRemaining, final long timeBetween, final DayPrayer dayPrayer) {
        circularProgressBar.setProgressWithAnimation(getProgressBarPercentage(timeRemaining, timeBetween), 1000L);
        TimeRemainingCTimer = new CountDownTimer(timeRemaining, 1000L) {
            public void onTick(long millisUntilFinished) {
                timeRemainingTextView.setText(TimingUtils.formatTimeForTimer(millisUntilFinished));
                circularProgressBar.setProgress(getProgressBarPercentage(timeRemaining, timeBetween));
            }

            public void onFinish() {
                updateNextPrayerViews(dayPrayer);
            }
        };
        TimeRemainingCTimer.start();
    }

    private void cancelTimer() {
        if (TimeRemainingCTimer != null)
            TimeRemainingCTimer.cancel();
    }
}