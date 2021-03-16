package com.hbouzidi.fiveprayers.timings.aladhan;

/**
 * @author Hicham Bouzidi Idrissi
 * Github : https://github.com/Five-Prayers/five-prayers-android
 * licenced under GPLv3 : https://www.gnu.org/licenses/gpl-3.0.en.html
 */
public class AladhanDate {

    private long timestamp;
    private AladhanDateType hijri;
    private AladhanDateType gregorian;

    public AladhanDateType getHijri() {
        return hijri;
    }

    public void setHijri(AladhanDateType hijri) {
        this.hijri = hijri;
    }

    public AladhanDateType getGregorian() {
        return gregorian;
    }

    public void setGregorian(AladhanDateType gregorian) {
        this.gregorian = gregorian;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
