package com.example.demo.model;

import java.util.List;

/**
 * 勤怠状況一覧画面の1日分の表示データ。
 */
public class AttendanceListRow {

    private final String dayText;
    private final String rowClass;
    private final String workPattern;
    private final String startTimeText;
    private final String endTimeText;
    private final String breakTimeText;
    private final String actualWorkTimeText;
    private final boolean hasRecord;
    private final List<AttendanceGraphSegment> graphSegments;

    // JavaScriptでリアルタイムグラフ更新に使う値
    private final boolean today;
    private final Integer startMinute;
    private final Integer breakStartMinute;
    private final Integer breakEndMinute;
    private final Integer endMinute;

    public AttendanceListRow(
            String dayText,
            String rowClass,
            String workPattern,
            String startTimeText,
            String endTimeText,
            String breakTimeText,
            String actualWorkTimeText,
            boolean hasRecord,
            List<AttendanceGraphSegment> graphSegments,
            boolean today,
            Integer startMinute,
            Integer breakStartMinute,
            Integer breakEndMinute,
            Integer endMinute) {

        this.dayText = dayText;
        this.rowClass = rowClass;
        this.workPattern = workPattern;
        this.startTimeText = startTimeText;
        this.endTimeText = endTimeText;
        this.breakTimeText = breakTimeText;
        this.actualWorkTimeText = actualWorkTimeText;
        this.hasRecord = hasRecord;
        this.graphSegments = graphSegments;

        this.today = today;
        this.startMinute = startMinute;
        this.breakStartMinute = breakStartMinute;
        this.breakEndMinute = breakEndMinute;
        this.endMinute = endMinute;
    }

    public String getDayText() {
        return dayText;
    }

    public String getRowClass() {
        return rowClass;
    }

    public String getWorkPattern() {
        return workPattern;
    }

    public String getStartTimeText() {
        return startTimeText;
    }

    public String getEndTimeText() {
        return endTimeText;
    }

    public String getBreakTimeText() {
        return breakTimeText;
    }

    public String getActualWorkTimeText() {
        return actualWorkTimeText;
    }

    public boolean isHasRecord() {
        return hasRecord;
    }

    public List<AttendanceGraphSegment> getGraphSegments() {
        return graphSegments;
    }

    public boolean isToday() {
        return today;
    }

    public Integer getStartMinute() {
        return startMinute;
    }

    public Integer getBreakStartMinute() {
        return breakStartMinute;
    }

    public Integer getBreakEndMinute() {
        return breakEndMinute;
    }

    public Integer getEndMinute() {
        return endMinute;
    }
}