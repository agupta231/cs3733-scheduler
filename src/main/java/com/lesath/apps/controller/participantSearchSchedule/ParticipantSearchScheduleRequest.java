package com.lesath.apps.controller.participantSearchSchedule;

import com.lesath.apps.controller.model.ScheduleQuery;
import com.lesath.apps.db.MeetingDAO;
import com.lesath.apps.db.ScheduleDAO;
import com.lesath.apps.db.TimesNotAvailableDAO;
import com.lesath.apps.model.Meeting;
import com.lesath.apps.model.Schedule;
import com.lesath.apps.model.TimeNotAvailable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import static java.time.temporal.ChronoUnit.DAYS;

public class ParticipantSearchScheduleRequest {
    ScheduleQuery query;
    String scheduleId;

    ParticipantSearchScheduleRequest(ScheduleQuery query, String scheduleId) {
        this.query = query;
        this.scheduleId = scheduleId;
    }

    public ArrayList<LocalDateTime> execute() throws Exception {
        ArrayList<LocalDateTime> availableSlots = new ArrayList<>();

        ScheduleDAO sDAO = new ScheduleDAO();
        Schedule s = sDAO.getSchedule(this.scheduleId);

        MeetingDAO mDAO = new MeetingDAO();
        ArrayList<Meeting> meetings = mDAO.getAllMeetingsForSchedule(scheduleId);
        ArrayList<String> meetingTimesRaw = new ArrayList<>();

        if(meetings != null) {
            for (Meeting meeting : meetings) {
                meetingTimesRaw.add(meeting.getStart_time().toString());
            }
        }

        HashSet<String> meetingTimes = new HashSet<>(meetingTimesRaw);


        TimesNotAvailableDAO tDAO = new TimesNotAvailableDAO();
        ArrayList<TimeNotAvailable> tna = tDAO.getAllTimesNotAvailableForScheduleId(scheduleId);
        ArrayList<String> timesNotAvailableRaw = new ArrayList<>();


        if(tna != null) {
            for (TimeNotAvailable t : tna) {
                timesNotAvailableRaw.add(t.getStart_time().toString());
            }
        }

        HashSet<String> timesNotAvailable = new HashSet<>(timesNotAvailableRaw);

        System.out.println("Meetings: " + meetingTimes);
        System.out.println("TNAS: " + timesNotAvailable);

        for (LocalDateTime instant : generateAllAvailableTimeslots(s)) {
            instant = instant.minusHours(5);
            boolean valid = true;

            valid &= !(timesNotAvailable.contains(instant.toString()) || meetingTimes.contains(instant.toString()));
            valid &= this.query.getMonth() == null || instant.getMonth().getValue() == this.query.getMonth();
            valid &= this.query.getDay() == null || instant.getDayOfMonth() == this.query.getDay();
            valid &= this.query.getYear() == null || instant.getYear() == this.query.getYear();
            valid &= this.query.getDayOfTheWeek() == null || instant.getDayOfWeek().getValue() == this.query.getDayOfTheWeek();
            valid &= this.query.getEndTime() == null || instant.withSecond(0).isBefore(LocalDateTime.of(instant.toLocalDate(), this.query.getEndTime().toLocalTime().minusMinutes(2)));
            valid &= this.query.getStartTime() == null || instant.withSecond(59).isAfter(LocalDateTime.of(instant.toLocalDate(), this.query.getStartTime().toLocalTime().withSecond(0)));

            System.out.println("Instant: " + instant + " Status: " + valid);

            if(valid) availableSlots.add(instant.plusHours(5));
        }

        return availableSlots;
    }

    ArrayList<LocalDateTime> generateAllAvailableTimeslots(Schedule schedule) {
        ArrayList<LocalDateTime> spots = new ArrayList<>();

        LocalDateTime instant = schedule.getStartDateTime();

        for(int i = 0; i <= DAYS.between(schedule.getStart_date(), schedule.getEnd_date()); i++) {
            LocalDateTime endDateTime = LocalDateTime.of(schedule.getStart_date().plusDays(i), schedule.getDaily_end_time());

            while(instant.isBefore(endDateTime)) {
                spots.add(instant);

                instant = instant.plusMinutes(schedule.getDuration());
            }

            instant = instant.plusDays(1)
                    .withHour(schedule.getDaily_start_time().getHour())
                    .withMinute(schedule.getDaily_start_time().getMinute());
        }

        return spots;
    }
}
