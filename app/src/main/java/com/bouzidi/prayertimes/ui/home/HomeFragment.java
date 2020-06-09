package com.bouzidi.prayertimes.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bouzidi.prayertimes.MainActivity;
import com.bouzidi.prayertimes.R;
import com.bouzidi.prayertimes.network.NetworkUtil;
import com.bouzidi.prayertimes.notifier.NotifierHelper;
import com.bouzidi.prayertimes.timings.ComplementaryTimingEnum;
import com.bouzidi.prayertimes.timings.DayPrayer;
import com.bouzidi.prayertimes.timings.PrayerEnum;
import com.bouzidi.prayertimes.ui.AlertHelper;
import com.bouzidi.prayertimes.ui.clock.ClockView;
import com.bouzidi.prayertimes.utils.PrayerUtils;
import com.bouzidi.prayertimes.utils.TimingUtils;
import com.bouzidi.prayertimes.utils.UiUtils;
import com.faltenreich.skeletonlayout.Skeleton;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private final int disabledColor = 0xFFD81B60;
    private final int enabledColor = 0xFF00C167;
    private LocalDateTime todayDate;
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
    private String adhanCallsPreferences;
    private String adhanCallKeyPart;
    private Skeleton skeleton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        todayDate = LocalDateTime.now();

        adhanCallsPreferences = mainActivity.getResources().getString(R.string.adthan_calls_shared_preferences);
        adhanCallKeyPart = mainActivity.getResources().getString(R.string.adthan_call_enabled_key);

        HomeViewModel dashboardViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(rootView);

        skeleton.showSkeleton();

        dashboardViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            AlertHelper.displayAlert(mainActivity, "Error", error);
        });

        dashboardViewModel.getDayPrayers().observe(getViewLifecycleOwner(), dayPrayer -> {
            updateNextPrayerViews(dayPrayer);
            updateDatesTextViews(dayPrayer);
            updateTimingsTextViews(dayPrayer);
            NotifierHelper.scheduleNextPrayerAlarms(mainActivity, dayPrayer);
            skeleton.showOriginal();
        });

        ViewTreeObserver observer = rootView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (!NetworkUtil.hasNetwork(mainActivity)) {
                    AlertHelper.displayAlert(mainActivity, "Network Unavailable", "Please turn on network to get prayer timings");
                }
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }

    private void initializeViews(View rootView) {
        skeleton = rootView.findViewById(R.id.skeletonLayout);

        locationTextView = rootView.findViewById(R.id.location_text_view);
        countryTextView = rootView.findViewById(R.id.country_text_view);
        hijriTextView = rootView.findViewById(R.id.hijriTextView);
        gregorianTextView = rootView.findViewById(R.id.gregorianTextView);
        prayerNametextView = rootView.findViewById(R.id.prayerNametextView);
        prayerTimetextView = rootView.findViewById(R.id.prayerTimetextView);
        timeRemainingTextView = rootView.findViewById(R.id.timeRemainingTextView);
        circularProgressBar = rootView.findViewById(R.id.circularProgressBar);

        fajrClock = rootView.findViewById(R.id.farj_clock_view);
        dohrClock = rootView.findViewById(R.id.dohr_clock_view);
        asrClock = rootView.findViewById(R.id.asr_clock_view);
        maghribClock = rootView.findViewById(R.id.maghreb_clock_view);
        ichaClock = rootView.findViewById(R.id.ichaa_clock_view);

        fajrTimingTextView = rootView.findViewById(R.id.fajr_timing_text_view);

        ImageView fajrCallImageView = rootView.findViewById(R.id.fajr_call_image_view);
        initializeImageViewIcon(fajrCallImageView, PrayerEnum.FAJR);

        ImageView dohrCallImageView = rootView.findViewById(R.id.dohr_call_image_view);
        initializeImageViewIcon(dohrCallImageView, PrayerEnum.DHOHR);

        ImageView asrCallImageView = rootView.findViewById(R.id.asr_call_image_view);
        initializeImageViewIcon(asrCallImageView, PrayerEnum.ASR);

        ImageView maghrebCallImageView = rootView.findViewById(R.id.maghrib_call_image_view);
        initializeImageViewIcon(maghrebCallImageView, PrayerEnum.MAGHRIB);

        ImageView ichaCallImageView = rootView.findViewById(R.id.icha_call_image_view);
        initializeImageViewIcon(ichaCallImageView, PrayerEnum.ICHA);


        dohrTimingTextView = rootView.findViewById(R.id.dohr_timing_text_view);
        asrTimingTextView = rootView.findViewById(R.id.asr_timing_text_view);
        maghribTimingTextView = rootView.findViewById(R.id.maghreb_timing_text_view);
        ichaTimingTextView = rootView.findViewById(R.id.icha_timing_text_view);

        sunriseTimingTextView = rootView.findViewById(R.id.sunrise_timing_text_view);
        sunsetTimingTextView = rootView.findViewById(R.id.sunset_timing_text_view);

        fajrLabel = rootView.findViewById(R.id.fajr_label_text_view);
        dohrLabel = rootView.findViewById(R.id.dohr_label_text_view);
        asrLabel = rootView.findViewById(R.id.asr_label_text_view);
        maghribLabel = rootView.findViewById(R.id.maghrib_label_text_view);
        ichaLabel = rootView.findViewById(R.id.icha_label_text_view);
    }

    private void updateTimingsTextViews(DayPrayer dayPrayer) {
        Map<PrayerEnum, LocalDateTime> timings = dayPrayer.getTimings();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDateTime fajrTiming = timings.get(PrayerEnum.FAJR);
        LocalDateTime dohrTiming = timings.get(PrayerEnum.DHOHR);
        LocalDateTime asrTiming = timings.get(PrayerEnum.ASR);
        LocalDateTime maghribTiming = timings.get(PrayerEnum.MAGHRIB);
        LocalDateTime ichaTiming = timings.get(PrayerEnum.ICHA);

        updateClockTime(fajrClock, fajrTiming.getHour(), fajrTiming.getMinute());
        updateClockTime(dohrClock, dohrTiming.getHour(), dohrTiming.getMinute());
        updateClockTime(asrClock, asrTiming.getHour(), asrTiming.getMinute());
        updateClockTime(maghribClock, maghribTiming.getHour(), maghribTiming.getMinute());
        updateClockTime(ichaClock, ichaTiming.getHour(), ichaTiming.getMinute());


        fajrTimingTextView.setText(fajrTiming.format(formatter));
        dohrTimingTextView.setText(dohrTiming.format(formatter));
        asrTimingTextView.setText(asrTiming.format(formatter));
        maghribTimingTextView.setText(maghribTiming.format(formatter));
        ichaTimingTextView.setText(ichaTiming.format(formatter));

        LocalDateTime sunriseTiming = dayPrayer.getComplementaryTiming().get(ComplementaryTimingEnum.SUNRISE);
        LocalDateTime sunsetTiming = dayPrayer.getComplementaryTiming().get(ComplementaryTimingEnum.SUNSET);

        sunriseTimingTextView.setText(sunriseTiming.format(formatter));
        sunsetTimingTextView.setText(sunsetTiming.format(formatter));

        fajrLabel.setText(R.string.FAJR);
        dohrLabel.setText(R.string.DHOHR);
        asrLabel.setText(R.string.ASR);
        maghribLabel.setText(R.string.MAGHRIB);
        ichaLabel.setText(R.string.ICHA);
    }

    private String[] getTimingPart(String timing) {
        return timing.split(":");
    }

    private void updateClockTime(ClockView clock, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        clock.setColor(0xFF17C5FF);
        clock.setCalendar(calendar);
    }

    private void updateNextPrayerViews(DayPrayer dayPrayer) {
        Map<PrayerEnum, LocalDateTime> timings = dayPrayer.getTimings();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        PrayerEnum nextPrayerKey = PrayerUtils.getNextPrayer(timings, LocalDateTime.now());
        PrayerEnum previousPrayerKey = PrayerUtils.getPreviousPrayerKey(nextPrayerKey);

        long timeRemaining = TimingUtils.getTimeBetween(todayDate, Objects.requireNonNull(timings.get(nextPrayerKey)));
        long timeBetween = TimingUtils.getTimeBetween(Objects.requireNonNull(timings.get(previousPrayerKey)), Objects.requireNonNull(timings.get(nextPrayerKey)));

        String prayerName = mainActivity.getResources().getString(
                getResources().getIdentifier(nextPrayerKey.toString(), "string", mainActivity.getPackageName()));

        prayerNametextView.setText(prayerName);
        prayerTimetextView.setText(timings.get(nextPrayerKey).format(formatter));
        timeRemainingTextView.setText(UiUtils.formatTimeForTimer(timeRemaining));

        startAnimationTimer(timeRemaining, timeBetween, dayPrayer);
    }

    private void updateDatesTextViews(DayPrayer dayPrayer) {
        String hijriMonth = mainActivity.getResources().getString(
                getResources().getIdentifier("hijri_month_" + dayPrayer.getHijriMonthNumber(), "string", mainActivity.getPackageName()));

        String hijriDate = UiUtils.formatHijriDate(
                dayPrayer.getHijriDay(),
                hijriMonth,
                dayPrayer.getHijriYear()
        );

        SimpleDateFormat timeZoneFormat = new SimpleDateFormat("ZZZZZ", Locale.getDefault());

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(dayPrayer.getTimestamp() * 1000);
        String gregorianDate = DateFormat.format("EEE dd MMMM, yyyy", cal).toString();

        hijriTextView.setText(StringUtils.capitalize(hijriDate));
        gregorianTextView.setText(StringUtils.capitalize(gregorianDate));
        String locationText = dayPrayer.getCity();
        String country = dayPrayer.getCountry() + " (" + timeZoneFormat.format(cal.getTime()) + ")";
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
                timeRemainingTextView.setText(UiUtils.formatTimeForTimer(millisUntilFinished));
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

    private void initializeImageViewIcon(ImageView fajrCallImageView, PrayerEnum prayerEnum) {
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(adhanCallsPreferences, MODE_PRIVATE);
        String callPreferenceKey = prayerEnum.toString() + adhanCallKeyPart;

        boolean fajrCallEnabled = sharedPreferences.getBoolean(callPreferenceKey, true);

        fajrCallImageView.setImageResource(fajrCallEnabled ? R.drawable.ic_notifications_24dp : R.drawable.ic_notifications_off_24dp);
        fajrCallImageView.setColorFilter(fajrCallEnabled ? enabledColor : disabledColor);

        setOnClickListener(fajrCallImageView, callPreferenceKey);
    }

    private void setOnClickListener(ImageView imageView, String callPreferenceKey) {
        imageView.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(adhanCallsPreferences, MODE_PRIVATE);

            Vibrator vibe = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibe.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibe.vibrate(10);
            }

            boolean adhanCallEnabled = sharedPreferences.getBoolean(callPreferenceKey, true);

            imageView.setImageResource(adhanCallEnabled ? R.drawable.ic_notifications_off_24dp : R.drawable.ic_notifications_24dp);
            imageView.setColorFilter(adhanCallEnabled ? disabledColor : enabledColor);

            SharedPreferences.Editor edit = sharedPreferences.edit();

            edit.putBoolean(callPreferenceKey, !adhanCallEnabled);
            edit.apply();
        });
    }
}