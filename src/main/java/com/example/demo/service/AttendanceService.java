package com.example.demo.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.AttendanceDailyEntity;
import com.example.demo.entity.AttendancePunchEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.AttendanceGraphSegment;
import com.example.demo.model.AttendanceListRow;
import com.example.demo.model.AttendanceRecord;
import com.example.demo.repository.AttendanceDailyRepository;
import com.example.demo.repository.AttendancePunchRepository;
import com.example.demo.repository.UserRepository;

@Service
public class AttendanceService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final long SECONDS_PER_DAY = 24 * 60 * 60;

    private static final int NOT_DELETED = 0;

    private static final String STATUS_WORKING = "WORKING";
    private static final String STATUS_BREAK = "BREAK";
    private static final String STATUS_FINISHED = "FINISHED";

    private static final String PUNCH_START = "START";
    private static final String PUNCH_END = "END";
    private static final String PUNCH_BREAK_START = "BREAK_START";
    private static final String PUNCH_BREAK_END = "BREAK_END";

    private static final String PUNCH_METHOD_WEB_INTERNAL = "WEB_INTERNAL";

    private final AttendanceDailyRepository attendanceDailyRepository;
    private final AttendancePunchRepository attendancePunchRepository;
    private final UserRepository userRepository;

    public AttendanceService(
            AttendanceDailyRepository attendanceDailyRepository,
            AttendancePunchRepository attendancePunchRepository,
            UserRepository userRepository) {
        this.attendanceDailyRepository = attendanceDailyRepository;
        this.attendancePunchRepository = attendancePunchRepository;
        this.userRepository = userRepository;
    }

    public void record(String loginId, String action) {

        Optional<UserEntity> optionalUser = findActiveUser(loginId);
        if (optionalUser.isEmpty()) {
            throw new IllegalStateException("ユーザーが存在しません。");
        }

        UserEntity user = optionalUser.get();
        Long userId = user.getUserId();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        AttendanceDailyEntity daily = attendanceDailyRepository
                .findByUserIdAndWorkDate(userId, today)
                .orElseGet(() -> createNewDaily(userId, today, now));

        switch (action) {
            case "出勤":
                daily.setStartAt(now);
                daily.setAttendanceStatus(STATUS_WORKING);
                daily.setUpdatedAt(now);
                daily = attendanceDailyRepository.save(daily);
                savePunch(userId, daily.getAttendanceDailyId(), PUNCH_START, now);
                break;

            case "休憩開始":
                daily.setAttendanceStatus(STATUS_BREAK);
                daily.setUpdatedAt(now);
                daily = attendanceDailyRepository.save(daily);
                savePunch(userId, daily.getAttendanceDailyId(), PUNCH_BREAK_START, now);
                break;

            case "休憩終了":
                daily.setAttendanceStatus(STATUS_WORKING);
                daily.setUpdatedAt(now);
                daily = attendanceDailyRepository.save(daily);
                savePunch(userId, daily.getAttendanceDailyId(), PUNCH_BREAK_END, now);
                break;

            case "退勤":
                daily.setEndAt(now);
                daily.setAttendanceStatus(STATUS_FINISHED);
                daily.setUpdatedAt(now);
                daily = attendanceDailyRepository.save(daily);
                savePunch(userId, daily.getAttendanceDailyId(), PUNCH_END, now);
                break;

            default:
                throw new IllegalArgumentException("不正な打刻種別です。");
        }
    }

    public List<AttendanceRecord> getRecords(String loginId) {

        Optional<UserEntity> optionalUser = findActiveUser(loginId);
        if (optionalUser.isEmpty()) {
            return List.of();
        }

        Long userId = optionalUser.get().getUserId();

        List<AttendancePunchEntity> punches =
                attendancePunchRepository.findByUserIdOrderByPunchedAtDesc(userId);

        List<AttendanceRecord> records = new ArrayList<>();

        for (AttendancePunchEntity punch : punches) {
            records.add(new AttendanceRecord(
                    loginId,
                    toActionLabel(punch.getPunchType()),
                    punch.getPunchedAt()));
        }

        return records;
    }

    public boolean hasRecordedToday(String loginId, String action) {

        Optional<UserEntity> optionalUser = findActiveUser(loginId);
        if (optionalUser.isEmpty()) {
            return false;
        }

        Long userId = optionalUser.get().getUserId();

        Optional<AttendanceDailyEntity> optionalDaily =
                attendanceDailyRepository.findByUserIdAndWorkDate(userId, LocalDate.now());

        if (optionalDaily.isEmpty()) {
            return false;
        }

        String punchType = toPunchType(action);
        if (punchType == null) {
            return false;
        }

        return attendancePunchRepository.existsByAttendanceDailyIdAndPunchType(
                optionalDaily.get().getAttendanceDailyId(),
                punchType);
    }

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

    public List<AttendanceListRow> getMonthlyRows(String loginId, YearMonth yearMonth) {

        Optional<UserEntity> optionalUser = findActiveUser(loginId);
        if (optionalUser.isEmpty()) {
            return List.of();
        }

        Long userId = optionalUser.get().getUserId();

        List<AttendanceDailyEntity> dailyList =
                attendanceDailyRepository.findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(
                        userId,
                        yearMonth.atDay(1),
                        yearMonth.atEndOfMonth());

        Map<LocalDate, AttendanceDailyEntity> dailyMap = new HashMap<>();
        for (AttendanceDailyEntity daily : dailyList) {
            dailyMap.put(daily.getWorkDate(), daily);
        }

        List<AttendanceListRow> rows = new ArrayList<>();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            AttendanceDailyEntity daily = dailyMap.get(date);
            rows.add(createAttendanceListRow(date, daily));
        }

        return rows;
    }

    private AttendanceListRow createAttendanceListRow(LocalDate date, AttendanceDailyEntity daily) {

        String dayText = date.getDayOfMonth() + "（" + getDayOfWeekText(date.getDayOfWeek()) + "）";
        String rowClass = getRowClass(date.getDayOfWeek());
        String workPattern = "通常";

        if (daily == null) {
            return new AttendanceListRow(
                    dayText,
                    rowClass,
                    workPattern,
                    "",
                    "",
                    "",
                    "",
                    false,
                    List.of(),
                    date.equals(LocalDate.now()),
                    null,
                    null,
                    null,
                    null);
        }

        List<AttendancePunchEntity> punches =
                attendancePunchRepository.findByAttendanceDailyIdOrderByPunchedAtAsc(
                        daily.getAttendanceDailyId());

        AttendancePunchEntity breakStartPunch = findPunch(punches, PUNCH_BREAK_START);
        AttendancePunchEntity breakEndPunch = findPunch(punches, PUNCH_BREAK_END);

        String startTimeText = formatTime(daily.getStartAt());
        String endTimeText = formatTime(daily.getEndAt());

        Duration breakDuration = Duration.ZERO;
        String breakTimeText = "";

        if (daily.getStartAt() != null) {
            if (breakStartPunch == null) {
                breakTimeText = "0:00";
            } else if (breakEndPunch != null) {
                breakDuration = safeDuration(
                        breakStartPunch.getPunchedAt(),
                        breakEndPunch.getPunchedAt());
                breakTimeText = formatDuration(breakDuration);
            }
        }

        String actualWorkTimeText = "";

        if (daily.getStartAt() != null && daily.getEndAt() != null) {
            Duration totalDuration = safeDuration(daily.getStartAt(), daily.getEndAt());
            Duration actualWorkDuration = totalDuration.minus(breakDuration);

            if (actualWorkDuration.isNegative()) {
                actualWorkDuration = Duration.ZERO;
            }

            actualWorkTimeText = formatDuration(actualWorkDuration);
        }

        List<AttendanceGraphSegment> graphSegments = createGraphSegments(
                date,
                daily.getStartAt(),
                breakStartPunch == null ? null : breakStartPunch.getPunchedAt(),
                breakEndPunch == null ? null : breakEndPunch.getPunchedAt(),
                daily.getEndAt());

        return new AttendanceListRow(
                dayText,
                rowClass,
                workPattern,
                startTimeText,
                endTimeText,
                breakTimeText,
                actualWorkTimeText,
                true,
                graphSegments,
                date.equals(LocalDate.now()),
                toMinuteOfDay(daily.getStartAt()),
                toMinuteOfDay(breakStartPunch == null ? null : breakStartPunch.getPunchedAt()),
                toMinuteOfDay(breakEndPunch == null ? null : breakEndPunch.getPunchedAt()),
                toMinuteOfDay(daily.getEndAt()));
    }

    private AttendanceDailyEntity createNewDaily(Long userId, LocalDate workDate, LocalDateTime now) {
        AttendanceDailyEntity daily = new AttendanceDailyEntity();
        daily.setUserId(userId);
        daily.setWorkDate(workDate);
        daily.setWorkPatternId(null);
        daily.setAttendanceStatus(STATUS_WORKING);
        daily.setRemarks(null);
        daily.setCreatedAt(now);
        daily.setUpdatedAt(now);
        return daily;
    }

    private void savePunch(Long userId, Long attendanceDailyId, String punchType, LocalDateTime now) {
        AttendancePunchEntity punch = new AttendancePunchEntity();
        punch.setUserId(userId);
        punch.setAttendanceDailyId(attendanceDailyId);
        punch.setPunchType(punchType);
        punch.setPunchedAt(now);
        punch.setPunchMethod(PUNCH_METHOD_WEB_INTERNAL);
        punch.setPunchNote(null);
        punch.setCreatedAt(now);
        attendancePunchRepository.save(punch);
    }

    private Optional<UserEntity> findActiveUser(String loginId) {
        return userRepository.findByLoginIdAndDeletedFlag(loginId, NOT_DELETED);
    }

    private AttendancePunchEntity findPunch(List<AttendancePunchEntity> punches, String punchType) {
        for (AttendancePunchEntity punch : punches) {
            if (punchType.equals(punch.getPunchType())) {
                return punch;
            }
        }
        return null;
    }

    private String toPunchType(String action) {
        switch (action) {
            case "出勤":
                return PUNCH_START;
            case "退勤":
                return PUNCH_END;
            case "休憩開始":
                return PUNCH_BREAK_START;
            case "休憩終了":
                return PUNCH_BREAK_END;
            default:
                return null;
        }
    }

    private String toActionLabel(String punchType) {
        switch (punchType) {
            case PUNCH_START:
                return "出勤";
            case PUNCH_END:
                return "退勤";
            case PUNCH_BREAK_START:
                return "休憩開始";
            case PUNCH_BREAK_END:
                return "休憩終了";
            default:
                return punchType;
        }
    }

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

        if (graphEndAt == null && date.equals(today)) {
            graphEndAt = LocalDateTime.now();
        }

        if (graphEndAt == null || !graphEndAt.isAfter(startAt)) {
            return segments;
        }

        if (breakStartAt != null
                && breakStartAt.isAfter(startAt)
                && breakStartAt.isBefore(graphEndAt)) {

            addSegment(segments, date, startAt, breakStartAt, "work");

            if (breakEndAt != null && breakEndAt.isAfter(breakStartAt)) {
                LocalDateTime breakSegmentEndAt = breakEndAt.isBefore(graphEndAt)
                        ? breakEndAt
                        : graphEndAt;

                addSegment(segments, date, breakStartAt, breakSegmentEndAt, "break");

                if (breakEndAt.isBefore(graphEndAt)) {
                    addSegment(segments, date, breakEndAt, graphEndAt, "work");
                }
            } else {
                addSegment(segments, date, breakStartAt, graphEndAt, "break");
            }

        } else {
            addSegment(segments, date, startAt, graphEndAt, "work");
        }

        return segments;
    }

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

    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIME_FORMATTER);
    }

    private Integer toMinuteOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.getHour() * 60 + dateTime.getMinute();
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