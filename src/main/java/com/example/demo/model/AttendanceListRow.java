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

    public AttendanceListRow(
            String dayText,
            String rowClass,
            String workPattern,
            String startTimeText,
            String endTimeText,
            String breakTimeText,
            String actualWorkTimeText,
            boolean hasRecord,
            List<AttendanceGraphSegment> graphSegments) {

        this.dayText = dayText;
        this.rowClass = rowClass;
        this.workPattern = workPattern;
        this.startTimeText = startTimeText;
        this.endTimeText = endTimeText;
        this.breakTimeText = breakTimeText;
        this.actualWorkTimeText = actualWorkTimeText;
        this.hasRecord = hasRecord;
        this.graphSegments = graphSegments;
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
}