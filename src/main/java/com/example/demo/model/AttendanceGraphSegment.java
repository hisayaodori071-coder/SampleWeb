package com.example.demo.model;

import java.util.Locale;

/**
 * 勤務時間グラフの1区間を表すクラス。
 *
 * 例:
 *   08:00〜10:00 勤務
 *   10:00〜11:00 休憩
 *   11:00〜18:00 勤務
 */
public class AttendanceGraphSegment {

    // グラフ左端から何%の位置に表示するか
    private final double leftPercent;

    // グラフ全体に対して何%の幅で表示するか
    private final double widthPercent;

    // work または break
    private final String type;

    public AttendanceGraphSegment(double leftPercent, double widthPercent, String type) {
        this.leftPercent = leftPercent;
        this.widthPercent = widthPercent;
        this.type = type;
    }

    public double getLeftPercent() {
        return leftPercent;
    }

    public double getWidthPercent() {
        return widthPercent;
    }

    public String getType() {
        return type;
    }

    /**
     * Thymeleafの th:style で使う文字列。
     */
    public String getStyle() {
        return String.format(Locale.US, "left: %.4f%%; width: %.4f%%;", leftPercent, widthPercent);
    }
}