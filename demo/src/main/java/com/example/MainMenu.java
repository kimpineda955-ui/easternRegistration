package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Provides the command-line menu for browsing courses and managing a student's registration.
 * The menu prompts the user for actions such as searching the course catalog, creating a
 * student record, adding or dropping courses, and viewing the student's schedule.
 */
public class MainMenu {

    private final EasternSchedule easternSchedule;
    private final Scanner scanner;
    private Student student;

    /**
     * Creates a menu instance wired to the course schedule data source and console input.
     */
    public MainMenu() {
        easternSchedule = new EasternSchedule();
        scanner = new Scanner(System.in);
    }

    /**
     * Starts the registration menu loop and handles user selections until the user exits.
     *
     * @throws Exception if a menu action fails while querying or updating course information
     */
    public void open() throws Exception {
        boolean running = true;

        System.out.println("Welcome to Eastern Registration!");

        while (running) {
            displayMenu();

            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        displayCoursesBySubject();
                        break;
                    case "2":
                        searchCourses();
                        break;
                    case "3":
                        createStudent();
                        break;
                    case "4":
                        addCourse();
                        break;
                    case "5":
                        dropCourse();
                        break;
                    case "6":
                        displayStudentSchedule();
                        break;
                    case "7":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println(
                                "Invalid option. Please select 1-7.");
                }
            } catch (Exception e) {
                System.out.println(
                        "Operation failed: " + e.getMessage());
            }

            System.out.println();
        }
    }

    /**
     * Displays the main menu options available to the user.
     */
    private void displayMenu() {
        System.out.println("==================================");
        System.out.println(" EASTERN REGISTRATION SYSTEM");
        System.out.println("==================================");
        System.out.println("1. Display all courses by subject");
        System.out.println("2. Search courses with filters");
        System.out.println("3. Create student");
        System.out.println("4. Add a course");
        System.out.println("5. Drop course(s)");
        System.out.println("6. Display student schedule");
        System.out.println("7. Exit");
    }

    /**
     * Lists all courses offered for a selected subject and prints basic availability details.
     *
     * @throws Exception if retrieving courses from the schedule fails
     */
    private void displayCoursesBySubject() throws Exception {
        System.out.print("Enter subject (example: CSC): ");
        String subject = scanner.nextLine().trim().toUpperCase();

        if (subject.isBlank()) {
            System.out.println("Subject cannot be blank.");
            return;
        }

        ArrayList<Course> courses =
                easternSchedule.queryCourseBySubject(subject);

        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }

        System.out.println("\nCourses for " + subject + ":");

        for (Course course : courses) {
            System.out.println(course.courseDescription());
            System.out.println("  Open: " + course.openSection());
            System.out.println("  Seats available: "
                    + (course.seatsAvailable() == null
                        ? "Unknown"
                        : course.seatsAvailable()));
        }
    }


    /**
     * Searches for matching courses by subject, course number, and optional filters such as open
     * sections and excluded meeting days.
     *
     * @throws Exception if the schedule lookup fails
     */
    private void searchCourses() throws Exception {
        System.out.print("Enter subject (example: CSC): ");
        String subject = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter course number (example: 360): ");
        String number = scanner.nextLine().trim();

        if (subject.isBlank() || number.isBlank()) {
            System.out.println(
                    "Both subject and course number are required.");
            return;
        }

        System.out.print("Only open courses? (y/n): ");
        boolean onlyOpen =
                scanner.nextLine().trim().equalsIgnoreCase("y");

        System.out.print(
                "Exclude a day? Enter M, T, W, R, F, S, U, or Enter to skip: ");
        String excludedDay =
                scanner.nextLine().trim().toUpperCase();

        ArrayList<Course> courses =
                easternSchedule.queryCourseBySubject(subject);

        List<Course> results = new ArrayList<>();

        for (Course course : courses) {
            if (!course.courseNumber().equalsIgnoreCase(number)) {
                continue;
            }

            if (onlyOpen && (!course.openSection()
                    || (course.seatsAvailable() != null
                        && course.seatsAvailable() <= 0))) {
                continue;
            }

            if (!excludedDay.isBlank()
                    && hasMeetingDay(course, excludedDay)) {
                continue;
            }

            results.add(course);
        }

        if (results.isEmpty()) {
            System.out.println("No courses match your search.");
            return;
        }

        System.out.println("\nMatching courses:");

        for (Course course : results) {
            System.out.println(course.courseDescription());
            System.out.println("  Section reference: "
                    + course.courseReferenceNumber());
            System.out.println("  Seats available: "
                    + (course.seatsAvailable() == null
                        ? "Unknown"
                        : course.seatsAvailable()));
        }
    }

    /**
     * Checks whether a course has any meeting scheduled on the given weekday code.
     *
     * @param course the course to inspect
     * @param day the weekday abbreviation to test, such as {@code M}, {@code T}, or {@code F}
     * @return {@code true} if the course includes that day in any meeting time; {@code false}
     *         otherwise
     */
    private boolean hasMeetingDay(Course course, String day) {
        if (course.meetingsFaculty() == null) {
            return false;
        }

        for (Course.MeetingFaculty meeting
                : course.meetingsFaculty()) {

            if (meeting == null || meeting.meetingTime() == null) {
                continue;
            }

            Course.MeetingTime time = meeting.meetingTime();

            switch (day) {
                case "M":
                    if (time.monday()) return true;
                    break;
                case "T":
                    if (time.tuesday()) return true;
                    break;
                case "W":
                    if (time.wednesday()) return true;
                    break;
                case "R":
                    if (time.thursday()) return true;
                    break;
                case "F":
                    if (time.friday()) return true;
                    break;
                case "S":
                    if (time.saturday()) return true;
                    break;
                case "U":
                    if (time.sunday()) return true;
                    break;
                default:
                    return false;
            }
        }

        return false;
    }

    /**
     * Collects student details from the user and stores the new student in the menu session.
     */
    private void createStudent() {
        System.out.print("Enter student ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine().trim();

        if (id.isBlank() || firstName.isBlank()
                || lastName.isBlank()) {
            System.out.println("All student fields are required.");
            return;
        }

        student = new Student(id, firstName, lastName);

        System.out.println("Student created: "
                + student.getFirstName() + " "
                + student.getLastName());
    }


    /**
     * Adds a course to the active student after the user selects a subject and section reference.
     *
     * @throws Exception if the course information cannot be loaded from the schedule
     */
    private void addCourse() throws Exception {
        if (!requireStudent()) {
            return;
        }

        System.out.print("Enter course subject (example: CSC): ");
        String subject = scanner.nextLine().trim().toUpperCase();

        if (subject.isBlank()) {
            System.out.println("Subject cannot be blank.");
            return;
        }

        ArrayList<Course> courses =
                easternSchedule.queryCourseBySubject(subject);

        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }

        for (Course course : courses) {
            System.out.println(course.courseDescription());
            System.out.println("  Section reference: "
                    + course.courseReferenceNumber());
            System.out.println();
        }

        System.out.print("Enter the section reference number: ");
        String reference = scanner.nextLine().trim();

        for (Course course : courses) {
            if (reference.equals(course.courseReferenceNumber())) {
                System.out.println(student.addCourse(course));
                return;
            }
        }

        System.out.println(
                "No course with that section reference was found.");
    }


    /**
     * Removes a single course or clears all courses from the active student's schedule.
     */
    private void dropCourse() {
        if (!requireStudent()) {
            return;
        }

        if (student.getCourses().isEmpty()) {
            System.out.println("You are not registered for any courses.");
            return;
        }

        System.out.println("Registered courses:");

        for (Course course : student.getCourses()) {
            System.out.println(course.courseDescription());
            System.out.println("  Section reference: "
                    + course.courseReferenceNumber());
        }

        System.out.println("Enter a section reference to drop one course.");
        System.out.println("Enter ALL to drop every course.");
        System.out.print("Your choice: ");

        String choice = scanner.nextLine().trim();

        if (choice.equalsIgnoreCase("ALL")) {
            student.dropAllCourses();
            System.out.println("All courses have been dropped.");
        } else {
            boolean dropped = student.getCourses().removeIf(
                    course -> choice.equals(course.courseReferenceNumber()));
            if (dropped) {
                System.out.println("Course dropped successfully.");
            } else {
                System.out.println(
                        "That course was not found in your schedule.");
            }
        }
    }

    /**
     * Displays the active student's identity details, enrolled courses, and total credit count.
     */
    private void displayStudentSchedule() {
        if (!requireStudent()) {
            return;
        }

        System.out.println("\nStudent Schedule");
        System.out.println(student.getFirstName() + " "
                + student.getLastName());
        System.out.println("Student ID: " + student.getId());

        if (student.getCourses().isEmpty()) {
            System.out.println("No registered courses.");
        } else {
            for (Course course : student.getCourses()) {
                System.out.println(course.courseDescription());
            }
        }

        System.out.println("Total credits: "
                + student.getTotalCredits() + "/"
                + Student.MAX_CREDITS);
    }

    /**
     * Verifies that a student has been created before allowing schedule-related actions.
     *
     * @return {@code true} if a student is available; {@code false} otherwise
     */
    private boolean requireStudent() {
        if (student == null) {
            System.out.println(
                    "Please create a student first using option 3.");
            return false;
        }

        return true;
    }
}
