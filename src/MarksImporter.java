import java.io.*;
import java.util.*;
import java.util.logging.*;

public class MarksImporter {

    // Logger for recording technical error details
    private static final Logger logger =
            Logger.getLogger(MarksImporter.class.getName());

    // Custom exception for duplicate student IDs
    static class DuplicateStudentException extends RuntimeException {

        public DuplicateStudentException(String message) {
            super(message);
        }
    }

    // Configure technical logging
    static void configureLogger() {
        try {
            logger.setUseParentHandlers(false);

            FileHandler fileHandler =
                    new FileHandler("import-errors.log", true);

            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

        } catch (IOException e) {
            System.out.println(
                    "Technical logging is currently unavailable.");
        }
    }

    // Import student marks from a text file
    static void importMarks(String filename) throws IOException {

        Set<String> seen = new HashSet<>();

        // Try-with-resources automatically closes the reader
        try (BufferedReader br =
                     new BufferedReader(new FileReader(filename))) {

            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {

                lineNumber++;

                try {

                    String[] parts = line.split(",", -1);

                    // Check exactly two fields
                    if (parts.length != 2) {
                        throw new IllegalArgumentException(
                                "Record must contain exactly two fields.");
                    }

                    String studentID = parts[0].trim();
                    String markText = parts[1].trim();

                    // Check that student ID is not empty
                    if (studentID.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Student ID cannot be empty.");
                    }

                    // Convert mark into integer
                    int mark = Integer.parseInt(markText);

                    // Validate mark range (0-100)
                    if (mark < 0 || mark > 100) {
                        throw new IllegalArgumentException(
                                "Mark must be between 0 and 100.");
                    }

                    // Detect duplicate student IDs
                    if (!seen.add(studentID)) {
                        throw new DuplicateStudentException(
                                "Duplicate student ID: " + studentID);
                    }

                    // Display accepted record
                    System.out.println(
                            "Accepted: " + studentID + " - " + mark);

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Invalid record on line " + lineNumber +
                                    ": Mark must be a number.");

                    logger.log(Level.WARNING,
                            "Non-numeric mark on line " + lineNumber, e);

                } catch (DuplicateStudentException e) {

                    System.out.println(
                            "Invalid record on line " + lineNumber +
                                    ": " + e.getMessage());

                    logger.log(Level.WARNING,
                            "Duplicate student record on line " +
                                    lineNumber, e);

                } catch (IllegalArgumentException e) {

                    System.out.println(
                            "Invalid record on line " + lineNumber +
                                    ": " + e.getMessage());

                    logger.log(Level.WARNING,
                            "Invalid student record on line " +
                                    lineNumber, e);
                }
            }
        }
    }

    // Main method
    public static void main(String[] args) {

        configureLogger();

        try {

            // Normal execution using the original marks.txt file
            importMarks("marks.txt");

        } catch (FileNotFoundException e) {

            System.out.println(
                    "Marks file not found; check the file name.");

            logger.log(Level.SEVERE,
                    "FileNotFoundException: Marks file is missing.", e);

        } catch (DuplicateStudentException e) {

            System.out.println(
                    "Import failed: A duplicate student ID was found.");

            logger.log(Level.SEVERE,
                    "DuplicateStudentException: " + e.getMessage(), e);

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "Import failed: Invalid student data.");

            logger.log(Level.SEVERE,
                    "IllegalArgumentException: " + e.getMessage(), e);

        } catch (IOException e) {

            System.out.println(
                    "Unable to read the marks file. Please try again.");

            logger.log(Level.SEVERE,
                    "IOException: File reading failed.", e);

        } finally {

            System.out.println("Import attempt finished");
        }
    }
}