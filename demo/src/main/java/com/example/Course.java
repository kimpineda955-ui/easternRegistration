package com.example;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a university course as returned by the registration service.
 * This record captures the identifying information, seat availability, and meeting data
 * needed to display course details in the registration UI.
 *
 * @param id the unique course identifier
 * @param subject the academic subject code, such as "CS"
 * @param courseNumber the course number, such as "101"
 * @param courseReferenceNumber the registration reference number for the course
 * @param sequenceNumber the sequencing value for the course offering
 * @param courseTitle the official title of the course
 * @param creditHourLow the low end of the credit-hour range for the course
 * @param openSection whether the section is currently open for enrollment
 * @param seatsAvailable the number of open seats remaining
 * @param faculty the faculty members assigned to the course
 * @param meetingsFaculty the meeting schedules associated with the course
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Course(
        String id,
        String subject,
        String courseNumber,
        String courseReferenceNumber,
        String sequenceNumber,
        String courseTitle,
        Double creditHourLow,
        Boolean openSection,
        Integer seatsAvailable,
        List<Faculty> faculty,
        List<MeetingFaculty> meetingsFaculty) {

    /**
     * Represents a faculty member assigned to a course.
     *
     * @param displayName the display name of the instructor or faculty member
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Faculty(String displayName) {
    }

    /**
     * Wraps a course meeting and its associated faculty details.
     *
     * @param meetingTime the time and day information for the meeting
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MeetingFaculty(MeetingTime meetingTime) {
        @Override
        public String toString() {
            return meetingTime == null ? "Meeting time unavailable" : meetingTime.toString();
        }
    }

    /**
     * Represents a single meeting time for a course section.
     *
     * @param monday whether the course meets on Monday
     * @param tuesday whether the course meets on Tuesday
     * @param wednesday whether the course meets on Wednesday
     * @param thursday whether the course meets on Thursday
     * @param friday whether the course meets on Friday
     * @param saturday whether the course meets on Saturday
     * @param sunday whether the course meets on Sunday
     * @param beginTime the meeting start time in a recognized time format
     * @param endTime the meeting end time in a recognized time format
     * @param room the room location for the meeting
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MeetingTime(
            boolean monday,
            boolean tuesday,
            boolean wednesday,
            boolean thursday,
            boolean friday,
            boolean saturday,
            boolean sunday,
            String beginTime,
            String endTime,
            String room) {

        @Override
        public String toString() {
            StringBuilder days = new StringBuilder();
            if (monday) days.append("M");
            if (tuesday) days.append("T");
            if (wednesday) days.append("W");
            if (thursday) days.append("R");
            if (friday) days.append("F");
            if (saturday) days.append("S");
            if (sunday) days.append("U");

            String dayText = days.length() == 0 ? "Days unavailable" : days.toString();
            return formatTime(beginTime) + " - " + formatTime(endTime) + ", " + dayText;
        }

        private String formatTime(String time) {
    if (time == null || time.isBlank()) {
        return "Time unavailable";
    }

    String[] patterns = {
        "HHmm",
        "HH:mm",
        "h:mm a",
        "hh:mm a"
    };

    for (String pattern : patterns) {
        try {
            java.time.LocalTime parsedTime =
                    java.time.LocalTime.parse(
                            time.trim(),
                            java.time.format.DateTimeFormatter
                                    .ofPattern(pattern,
                                            java.util.Locale.US));

            return parsedTime.format(
                    java.time.format.DateTimeFormatter
                            .ofPattern("h:mm a",
                                    java.util.Locale.US));
        } catch (java.time.format.DateTimeParseException e) {
        
        }
    }

    return time;
}
    }

    /**
     * Returns the lowest value in the course's credit-hour range.
     *
     * @return the number of credits, or {@code 0} when the credit-hour value is unavailable
     */
    public int credits() {
        return creditHourLow == null ? 0 : creditHourLow.intValue();
    }

    /**
     * Returns the display name of the first faculty member associated with this course.
     *
     * @return the instructor name, or {@code "Unknown"} when no faculty information is present
     */
    public String instructorName() {
        if (faculty == null || faculty.isEmpty()
                || faculty.get(0) == null || faculty.get(0).displayName() == null) {
            return "Unknown";
        }
        return faculty.get(0).displayName();
    }

    /**
     * Builds a human-readable summary of the course, including subject, number, reference,
     * credits, title, and scheduled meeting times.
     *
     * @return a formatted description of this course
     */
    public String courseDescription() {
        StringBuilder result = new StringBuilder();
        result.append(subject == null ? "Unknown" : subject)
                .append(" ")
                .append(courseNumber == null ? "Unknown" : courseNumber)
                .append("-")
                .append(courseReferenceNumber == null ? "Unknown" : courseReferenceNumber)
                .append(", ")
                .append(credits())
                .append(" credits, ")
                .append(courseTitle == null ? "Title unavailable" : courseTitle);

        if (meetingsFaculty != null) {
            for (MeetingFaculty meeting : meetingsFaculty) {
                if (meeting != null && meeting.meetingTime() != null) {
                    result.append(", ").append(meeting.meetingTime());
                }
            }
        }
        return result.toString();
    }

    /**
     * Determines whether the section is full based on remaining seat availability.
     *
     * @return {@code true} if the course has no remaining seats; {@code false} otherwise
     */
    public boolean isFull() {
        return seatsAvailable != null && seatsAvailable <= 0;
    }

    @Override
    public String toString() {
        return subject + "-" + courseNumber;
    }

    /**
     * Returns the credit value using the JavaBean-style getter name.
     *
     * @return the course credits, or {@code 0} when unavailable
     */
    public int getCredits() {
        return credits();
    }
}
    

   

