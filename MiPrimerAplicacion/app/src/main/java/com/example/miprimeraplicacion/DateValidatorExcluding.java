package com.example.miprimeraplicacion;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.material.datepicker.CalendarConstraints;

import java.util.List;

public class DateValidatorExcluding implements CalendarConstraints.DateValidator {
    private final List<Long> disabledDays;

    public DateValidatorExcluding(List<Long> disabledDays) {
        this.disabledDays = disabledDays;
    }

    @Override
    public boolean isValid(long date) {
        return !disabledDays.contains(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}

    public static final Parcelable.Creator<DateValidatorExcluding> CREATOR =
            new Parcelable.Creator<DateValidatorExcluding>() {
                @Override
                public DateValidatorExcluding createFromParcel(Parcel in) {
                    return new DateValidatorExcluding(null);
                }

                @Override
                public DateValidatorExcluding[] newArray(int size) {
                    return new DateValidatorExcluding[size];
                }
            };
}
