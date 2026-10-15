package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Student {

     public static final int MAX_CREDITS = 17;

    private String id;
    private String firstName;
    private String lastName;
    private List<Course> courses;


    public Student() {
        this("", "", "");
    }

    
    public Student(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.courses = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Course> getCourses() {
        if (courses == null) {
            courses = new ArrayList<>();
        }

        return Collections.unmodifiableList(courses);
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses == null
                ? new ArrayList<>()
                : new ArrayList<>(courses);
    }

   
    public int getTotalCredits() {
        int total = 0;

        for (Course course : getCourses()) {
            total += course.getCredits();
        }

        return total;
    }

    public String addCourse(Course course) {
        if (course == null) {
            return "Invalid course.";
        }

        for (Course registered : getCourses()) {
            if (sameCourse(registered, course)) {
                return "You are already registered for this course.";
            }
        }

        if (getTotalCredits() + course.getCredits() > MAX_CREDITS) {
            return "Registration failed: the "
                    + MAX_CREDITS + "-credit limit would be exceeded.";
        }

        for (Course registered : getCourses()) {
            if (hasTimeConflict(registered, course)) {
                return "Registration failed: the course conflicts "
                        + "with " + registered.subject() + " "
                        + registered.courseNumber() + ".";
            }
        }

        if (courses == null) {
            courses = new ArrayList<>();
        }

        courses.add(course);
        return "Course added successfully.";
    }

   
    public boolean dropCourse(String referenceNumber) {
        if (referenceNumber == null) {
            return false;
        }

        return getMutableCourses().removeIf(course ->
                referenceNumber.equals(course.courseReferenceNumber()));
    }


    public void dropAllCourses() {
        getMutableCourses().clear();
    }


    private boolean sameCourse(Course first, Course second) {
        if (first.courseReferenceNumber() != null
                && second.courseReferenceNumber() != null) {
            return first.courseReferenceNumber()
                    .equals(second.courseReferenceNumber());
        }

        return first.id() != null
                && first.id().equals(second.id());
    }

    /**
     * Checks whether two courses overlap on a shared meeting day.
     */
    private boolean hasTimeConflict(Course first, Course second) {
        for (Course.MeetingFaculty firstMeeting : safeMeetings(first)) {
            for (Course.MeetingFaculty secondMeeting : safeMeetings(second)) {
                Course.MeetingTime a = firstMeeting.meetingTime();
                Course.MeetingTime b = secondMeeting.meetingTime();

                if (a == null || b == null) {
                    continue;
                }

                if (!shareDay(a, b)) {
                    continue;
                }

                int aStart = toMinutes(a.startTime());
                int aEnd = toMinutes(a.endTime());
                int bStart = toMinutes(b.startTime());
                int bEnd = toMinutes(b.endTime());

               
                if (aStart < 0 || aEnd < 0 || bStart < 0 || bEnd < 0) {
                    continue;
                }

                if (aStart < bEnd && bStart < aEnd) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<Course.MeetingFaculty> safeMeetings(Course course) {
        return course.meetingsFaculty() == null
                ? Collections.emptyList()
                : course.meetingsFaculty();
    }

    private boolean shareDay(
            Course.MeetingTime a, Course.MeetingTime b) {
        return (a.monday() && b.monday())
                || (a.tuesday() && b.tuesday())
                || (a.wednesday() && b.wednesday())
                || (a.thursday() && b.thursday())
                || (a.friday() && b.friday())
                || (a.saturday() && b.saturday())
                || (a.sunday() && b.sunday());
    }

   
    private int toMinutes(String value) {
        if (value == null || !value.matches("\\d{4}")) {
            return -1;
        }

        try {
            int hours = Integer.parseInt(value.substring(0, 2));
            int minutes = Integer.parseInt(value.substring(2, 4));

            if (hours > 23 || minutes > 59) {
                return -1;
            }

            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private List<Course> getMutableCourses() {
        if (courses == null) {
            courses = new ArrayList<>();
        }

        return courses;
    }
}
