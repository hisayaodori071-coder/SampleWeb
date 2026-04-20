package com.example.demo.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.demo.model.AttendanceGraphSegment;
import com.example.demo.model.AttendanceListRow;
import com.example.demo.model.AttendanceRecord;

@Service
public class AttendanceService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final long SECONDS_PER_DAY = 24 * 60 * 60;

    /*
     * DB未使用のため、打刻履歴はメモリ上で保持する。
     *
     * キー:
     *   loginId
     *
     * 値:
     *   そのユーザーの打刻履歴一覧
     *
     * 注意:
     *   Spring Bootを再起動すると履歴は消える。
     */
    private final Map<String, List<AttendanceRecord>> records = new ConcurrentHashMap<>();

    /**
     * 打刻を記録する。
     */
    public void record(String loginId, String action) {
        List<AttendanceRecord> userRecords = records.computeIfAbsent(
                loginId,
                key -> Collections.synchronizedList(new ArrayList<>()));

        synchronized (userRecords) {
            // 新しい履歴が画面上部に来るよう、先頭に追加
            userRecords.add(0, new AttendanceRecord(loginId, action, LocalDateTime.now()));
        }
    }

    /**
     * 指定ユーザーの打刻履歴を返す。
     */
    public List<AttendanceRecord> getRecords(String loginId) {
        List<AttendanceRecord> userRecords = records.get(loginId);

        if (userRecords == null) {
            return List.of();
        }

        synchronized (userRecords) {
            // 内部リストを直接返さず、コピーして返す
            return new ArrayList<>(userRecords);
        }
    }

    /**
     * 今日すでに同じ打刻をしているか判定する。
     */
    public boolean hasRecordedToday(String loginId, String action) {
        List<AttendanceRecord> userRecords = records.get(loginId);

        if (userRecords == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        synchronized (userRecords) {
            for (AttendanceRecord record : userRecords) {
                if (record.getRecordedAt().toLocalDate().equals(today)
                        && record.getAction().equals(action)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 打刻順が正しいかを判定する。
     *
     * 戻り値:
     *   問題なし → null
     *   問題あり → 画面に表示するエラーメッセージ
     *
     * 想定する正常パターン:
     *   1. 出勤 → 休憩開始 → 休憩終了 → 退勤
     *   2. 出勤 → 退勤
     */
    public String validateActionOrder(String loginId, String action) {

        boolean started = hasRecordedToday(loginId, "出勤");
        boolean breakStarted = hasRecordedToday(loginId, "休憩開始");
        boolean breakEnded = hasRecordedToday(loginId, "休憩終了");
        boolean ended = hasRecordedToday(loginId, "退勤");

        switch (action) {
            case "出勤":
                if (started) {
                    return "出勤は本日すでに記録済みです。";
                }
                if (ended) {
                    return "本日はすでに退勤済みです。";
                }
                return null;

            case "休憩開始":
                if (!started) {
                    return "まだ出勤していません。";
                }
                if (ended) {
                    return "本日はすでに退勤済みです。";
                }
                if (breakStarted) {
                    return "休憩開始は本日すでに記録済みです。";
                }
                if (breakEnded) {
                    return "本日の休憩はすでに終了しています。";
                }
                return null;

            case "休憩終了":
                if (!breakStarted) {
                    return "まだ休憩を開始していません。";
                }
                if (breakEnded) {
                    return "休憩終了は本日すでに記録済みです。";
                }
                if (ended) {
                    return "本日はすでに退勤済みです。";
                }
                return null;

            case "退勤":
                if (!started) {
                    return "まだ出勤していません。";
                }
                if (ended) {
                    return "退勤は本日すでに記録済みです。";
                }
                if (breakStarted && !breakEnded) {
                    return "休憩が終了していません。";
                }
                return null;

            default:
                return "不正な操作です。";
        }
    }

    /**
     * 勤怠状況一覧画面用に、指定月の全日分の表示データを作る。
     */
    public List<AttendanceListRow> getMonthlyRows(String loginId, YearMonth yearMonth) {

        List<AttendanceRecord> userRecords = getRecords(loginId);

        Map<LocalDate, List<AttendanceRecord>> monthlyRecords = new HashMap<>();

        for (AttendanceRecord record : userRecords) {
            LocalDate recordDate = record.getRecordedAt().toLocalDate();

            if (YearMonth.from(recordDate).equals(yearMonth)) {
                monthlyRecords
                        .computeIfAbsent(recordDate, key -> new ArrayList<>())
                        .add(record);
            }
        }

        List<AttendanceListRow> rows = new ArrayList<>();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            List<AttendanceRecord> dayRecords = monthlyRecords.getOrDefault(date, List.of());

            rows.add(createAttendanceListRow(date, dayRecords));
        }

        return rows;
    }

    /**
     * 1日分の勤怠一覧行を作る。
     */
    private AttendanceListRow createAttendanceListRow(LocalDate date, List<AttendanceRecord> dayRecords) {

        AttendanceRecord startRecord = findRecord(dayRecords, "出勤");
        AttendanceRecord endRecord = findRecord(dayRecords, "退勤");
        AttendanceRecord breakStartRecord = findRecord(dayRecords, "休憩開始");
        AttendanceRecord breakEndRecord = findRecord(dayRecords, "休憩終了");

        String dayText = date.getDayOfMonth() + "（" + getDayOfWeekText(date.getDayOfWeek()) + "）";
        String rowClass = getRowClass(date.getDayOfWeek());

        // 現時点では全日「通常」
        String workPattern = "通常";

        String startTimeText = formatTime(startRecord);
        String endTimeText = formatTime(endRecord);

        Duration breakDuration = Duration.ZERO;
        String breakTimeText = "";

        if (startRecord != null) {
            if (breakStartRecord == null) {
                breakTimeText = "0:00";
            } else if (breakEndRecord != null) {
                breakDuration = safeDuration(
                        breakStartRecord.getRecordedAt(),
                        breakEndRecord.getRecordedAt());
                breakTimeText = formatDuration(breakDuration);
            }
        }

        String actualWorkTimeText = "";

        if (startRecord != null && endRecord != null) {
            Duration totalDuration = safeDuration(
                    startRecord.getRecordedAt(),
                    endRecord.getRecordedAt());

            Duration actualWorkDuration = totalDuration.minus(breakDuration);

            if (actualWorkDuration.isNegative()) {
                actualWorkDuration = Duration.ZERO;
            }

            actualWorkTimeText = formatDuration(actualWorkDuration);
        }

        List<AttendanceGraphSegment> graphSegments = createGraphSegments(
                date,
                startRecord == null ? null : startRecord.getRecordedAt(),
                breakStartRecord == null ? null : breakStartRecord.getRecordedAt(),
                breakEndRecord == null ? null : breakEndRecord.getRecordedAt(),
                endRecord == null ? null : endRecord.getRecordedAt());

        boolean hasRecord = !dayRecords.isEmpty();

        return new AttendanceListRow(
                dayText,
                rowClass,
                workPattern,
                startTimeText,
                endTimeText,
                breakTimeText,
                actualWorkTimeText,
                hasRecord,
                graphSegments);
    }

    /**
     * 指定された打刻種別のレコードを探す。
     */
    private AttendanceRecord findRecord(List<AttendanceRecord> records, String action) {
        for (AttendanceRecord record : records) {
            if (record.getAction().equals(action)) {
                return record;
            }
        }

        return null;
    }

    /**
     * 勤務時間グラフ用の区間データを作る。
     *
     * 退勤済み:
     *   出勤〜退勤まで表示
     *
     * 退勤前の今日:
     *   出勤〜現在時刻まで表示
     *
     * 休憩中の今日:
     *   出勤〜休憩開始を勤務色
     *   休憩開始〜現在時刻を休憩色
     */
    private List<AttendanceGraphSegment> createGraphSegments(
            LocalDate date,
            LocalDateTime startAt,
            LocalDateTime breakStartAt,
            LocalDateTime breakEndAt,
            LocalDateTime endAt) {

        List<AttendanceGraphSegment> segments = new ArrayList<>();

        if (startAt == null) {
            return segments;
        }

        LocalDate today = LocalDate.now();

        LocalDateTime graphEndAt = endAt;

        // 退勤していない今日のデータは、現在時刻までグラフを伸ばす
        if (graphEndAt == null && date.equals(today)) {
            graphEndAt = LocalDateTime.now();
        }

        // 退勤していない過去日・未来日はグラフを表示しない
        if (graphEndAt == null || !graphEndAt.isAfter(startAt)) {
            return segments;
        }

        if (breakStartAt != null
                && breakStartAt.isAfter(startAt)
                && breakStartAt.isBefore(graphEndAt)) {

            // 出勤〜休憩開始
            addSegment(segments, date, startAt, breakStartAt, "work");

            if (breakEndAt != null && breakEndAt.isAfter(breakStartAt)) {
                LocalDateTime breakSegmentEndAt = breakEndAt.isBefore(graphEndAt)
                        ? breakEndAt
                        : graphEndAt;

                // 休憩開始〜休憩終了
                addSegment(segments, date, breakStartAt, breakSegmentEndAt, "break");

                // 休憩終了〜退勤または現在時刻
                if (breakEndAt.isBefore(graphEndAt)) {
                    addSegment(segments, date, breakEndAt, graphEndAt, "work");
                }
            } else {
                // 休憩開始済み、休憩終了前の場合
                addSegment(segments, date, breakStartAt, graphEndAt, "break");
            }

        } else {
            // 休憩なし
            addSegment(segments, date, startAt, graphEndAt, "work");
        }

        return segments;
    }

    /**
     * グラフ区間を追加する。
     */
    private void addSegment(
            List<AttendanceGraphSegment> segments,
            LocalDate date,
            LocalDateTime from,
            LocalDateTime to,
            String type) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        LocalDateTime clampedFrom = clamp(from, startOfDay, endOfDay);
        LocalDateTime clampedTo = clamp(to, startOfDay, endOfDay);

        if (!clampedTo.isAfter(clampedFrom)) {
            return;
        }

        long leftSeconds = Duration.between(startOfDay, clampedFrom).toSeconds();
        long widthSeconds = Duration.between(clampedFrom, clampedTo).toSeconds();

        double leftPercent = leftSeconds * 100.0 / SECONDS_PER_DAY;
        double widthPercent = widthSeconds * 100.0 / SECONDS_PER_DAY;

        segments.add(new AttendanceGraphSegment(leftPercent, widthPercent, type));
    }

    private LocalDateTime clamp(LocalDateTime value, LocalDateTime min, LocalDateTime max) {
        if (value.isBefore(min)) {
            return min;
        }

        if (value.isAfter(max)) {
            return max;
        }

        return value;
    }

    private String formatTime(AttendanceRecord record) {
        if (record == null) {
            return "";
        }

        return record.getRecordedAt().format(TIME_FORMATTER);
    }

    private Duration safeDuration(LocalDateTime from, LocalDateTime to) {
        Duration duration = Duration.between(from, to);

        if (duration.isNegative()) {
            return Duration.ZERO;
        }

        return duration;
    }

    private String formatDuration(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return hours + ":" + String.format(Locale.US, "%02d", minutes);
    }

    private String getDayOfWeekText(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "月";
            case TUESDAY:
                return "火";
            case WEDNESDAY:
                return "水";
            case THURSDAY:
                return "木";
            case FRIDAY:
                return "金";
            case SATURDAY:
                return "土";
            case SUNDAY:
                return "日";
            default:
                return "";
        }
    }

    private String getRowClass(DayOfWeek dayOfWeek) {
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return "saturday";
        }

        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return "sunday";
        }

        return "";
    }
}