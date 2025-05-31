package com.example.demo1;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class HelloApplication extends Application {

    // Database connection info - change accordingly
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/learningManagement";
    private static final String DB_USER = "postgres"; // <-- replace
    private static final String DB_PASS = "Taittmndell210702"; // <-- replace

    private Stage primaryStage;
    private Scene homeScene, courseScene, dashboardScene, profileScene, lecturerDashboardScene;
    private String loggedInUser;

    // Data models
    private static class User {
        String userId, username, password, fullName, role, faculty;
        User(String userId, String username, String password, String fullName, String role, String faculty) {
            this.userId = userId; this.username = username; this.password = password;
            this.fullName = fullName; this.role = role; this.faculty = faculty;
        }
    }

    private static class Course {
        String name, code, faculty;
        Course(String name, String code, String faculty) {
            this.name = name; this.code = code; this.faculty = faculty;
        }
    }

    private static class Announcement {
        String id, courseCode, content, postedBy;
        Announcement(String id, String courseCode, String content, String postedBy) {
            this.id = id; this.courseCode = courseCode; this.content = content; this.postedBy = postedBy;
        }
    }

    private static class Content {
        String id, title, type, data;
        Content(String id, String title, String type, String data) {
            this.id = id; this.title = title; this.type = type; this.data = data;
        }
    }

    private static class Message {
        String id, courseCode, sender, content;
        long timestamp;
        Message(String id, String courseCode, String sender, String content) {
            this.id = id; this.courseCode = courseCode; this.sender = sender; this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static class Assessment {
        String id, courseCode, title, type;
        double weight;
        List<String> questions;
        String postedBy;
        Assessment(String id, String courseCode, String title, String type, double weight, List<String> questions, String postedBy) {
            this.id = id; this.courseCode = courseCode; this.title = title; this.type = type;
            this.weight = weight; this.questions = questions; this.postedBy = postedBy;
        }
    }

    private static class Submission {
        String assessmentId, studentUsername;
        List<String> answers;
        double score;
        Submission(String assessmentId, String studentUsername, List<String> answers, double score) {
            this.assessmentId = assessmentId; this.studentUsername = studentUsername;
            this.answers = answers; this.score = score;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createDatabase(); // Ensure schema exists
        initSampleData(); // Optional: add sample data

        homeScene = new Scene(createHomeRoot(), 1000, 700);
        primaryStage.setScene(homeScene);
        primaryStage.setTitle("UCT LMS");
        primaryStage.show();
    }

    // --- Database setup ---
    private void createDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id VARCHAR(20) PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "full_name VARCHAR(100)," +
                "role VARCHAR(10)," +
                "faculty VARCHAR(50)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS courses (" +
                "code VARCHAR(10) PRIMARY KEY," +
                "name VARCHAR(100)," +
                "faculty VARCHAR(50)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS enrollments (" +
                "username VARCHAR(50)," +
                "course_code VARCHAR(10)," +
                "PRIMARY KEY (username, course_code)," +
                "FOREIGN KEY (username) REFERENCES users(username)," +
                "FOREIGN KEY (course_code) REFERENCES courses(code)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS announcements (" +
                "id SERIAL PRIMARY KEY," +
                "course_code VARCHAR(10)," +
                "content TEXT," +
                "posted_by VARCHAR(50)," +
                "posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (course_code) REFERENCES courses(code)," +
                "FOREIGN KEY (posted_by) REFERENCES users(username)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS contents (" +
                "id SERIAL PRIMARY KEY," +
                "course_code VARCHAR(10)," +
                "title VARCHAR(100)," +
                "type VARCHAR(10)," +
                "data TEXT," +
                "FOREIGN KEY (course_code) REFERENCES courses(code)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS messages (" +
                "id SERIAL PRIMARY KEY," +
                "course_code VARCHAR(10)," +
                "sender VARCHAR(50)," +
                "content TEXT," +
                "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (course_code) REFERENCES courses(code)," +
                "FOREIGN KEY (sender) REFERENCES users(username)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS assessments (" +
                "id SERIAL PRIMARY KEY," +
                "course_code VARCHAR(10)," +
                "title VARCHAR(100)," +
                "type VARCHAR(20)," +
                "weight NUMERIC(3,2)," +
                "questions TEXT," +
                "posted_by VARCHAR(50)," +
                "FOREIGN KEY (course_code) REFERENCES courses(code)," +
                "FOREIGN KEY (posted_by) REFERENCES users(username)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS submissions (" +
                "id SERIAL PRIMARY KEY," +
                "assessment_id INTEGER," +
                "student_username VARCHAR(50)," +
                "answers TEXT," +
                "score NUMERIC(5,2)," +
                "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (assessment_id) REFERENCES assessments(id)," +
                "FOREIGN KEY (student_username) REFERENCES users(username)" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            for (String sqlStmt : sql.split(";")) {
                if (!sqlStmt.trim().isEmpty()) {
                    stmt.execute(sqlStmt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initSampleData() {
        // Insert sample users and courses if not exist
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            if (getUserByUsername("student") == null) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (user_id, username, password, full_name, role, faculty) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setString(1, "STU-1001");
                ps.setString(2, "student");
                ps.setString(3, "pass123");
                ps.setString(4, "Student User");
                ps.setString(5, "Student");
                ps.setString(6, null);
                ps.executeUpdate();
            }
            if (getUserByUsername("lecturer") == null) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (user_id, username, password, full_name, role, faculty) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setString(1, "LEC-1001");
                ps.setString(2, "lecturer");
                ps.setString(3, "pass456");
                ps.setString(4, "Lecturer User");
                ps.setString(5, "Lecturer");
                ps.setString(6, "Design");
                ps.executeUpdate();
            }
            if (getCourseByCode("ARC101") == null) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO courses (code, name, faculty) VALUES (?, ?, ?)");
                ps.setString(1, "ARC101");
                ps.setString(2, "Architecture");
                ps.setString(3, "Design");
                ps.executeUpdate();
            }
            if (getCourseByCode("BUS201") == null) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO courses (code, name, faculty) VALUES (?, ?, ?)");
                ps.setString(1, "BUS201");
                ps.setString(2, "Business Management");
                ps.setString(3, "Business");
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get user by username
    private User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getString("user_id"), rs.getString("username"),
                            rs.getString("password"), rs.getString("full_name"),
                            rs.getString("role"), rs.getString("faculty"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Get course by code
    private Course getCourseByCode(String code) {
        String sql = "SELECT * FROM courses WHERE code=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(rs.getString("name"), rs.getString("code"), rs.getString("faculty"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs=stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(new Course(rs.getString("name"), rs.getString("code"), rs.getString("faculty")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return courses;
    }

    private List<String> getEnrolledCourses(String username) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT course_code FROM enrollments WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()) list.add(rs.getString("course_code"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private List<Announcement> getAnnouncementsForCourse(String courseCode) {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT * FROM announcements WHERE course_code=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    list.add(new Announcement(rs.getString("id"), rs.getString("course_code"), rs.getString("content"), rs.getString("posted_by")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private List<Content> getContentsForCourse(String courseCode) {
        List<Content> list = new ArrayList<>();
        String sql = "SELECT * FROM contents WHERE course_code=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    list.add(new Content(rs.getString("id"), rs.getString("title"), rs.getString("type"), rs.getString("data")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private List<Message> getMessagesForCourse(String courseCode) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE course_code=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    list.add(new Message(rs.getString("id"), rs.getString("course_code"), rs.getString("sender"), rs.getString("content")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // CRUD for enrollment
    private void enrollCourse(String username, String courseCode) {
        String sql = "INSERT INTO enrollments (username, course_code) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, courseCode);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void unenrollCourse(String username, String courseCode) {
        String sql = "DELETE FROM enrollments WHERE username=? AND course_code=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, courseCode);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- GUI: Create root panes ---
    private BorderPane createHomeRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Home", true);
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #1a252f; -fx-padding: 20;");
        Label lbl = new Label("Welcome to UCT LMS");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lbl.setTextFill(Color.WHITE);
        Label subtitle = new Label("Explore our courses across faculties");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#cccccc"));
        header.getChildren().addAll(lbl, subtitle);

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(20));
        // Buttons
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);
        Button btnSignIn = new Button("Sign In");
        btnSignIn.setStyle("-fx-background-color:#00c4b4; -fx-text-fill: white;");
        btnSignIn.setOnAction(e -> showSignInDialog());
        Button btnSignUp = new Button("Sign Up");
        btnSignUp.setStyle("-fx-background-color:#fff; -fx-text-fill:#00c4b4; -fx-border-color:#00c4b4;");
        btnSignUp.setOnAction(e -> showSignUpDialog());
        btnBox.getChildren().addAll(btnSignIn, btnSignUp);
        mainBox.getChildren().addAll(header, btnBox);
        ScrollPane sp = new ScrollPane(mainBox);
        sp.setFitToWidth(true);
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(sp);
        return root;
    }

    private Scene createCourseRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Courses", false);
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        List<Course> allCourses = getAllCourses();
        int col=0, row=0;
        for (Course c : allCourses) {
            VBox card = createCourseCard(c.name, c.code);
            grid.add(card, col, row);
            col++;
            if (col > 2) { col=0; row++; }
        }
        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(sp);
        return new Scene(root, 1000, 700);
    }

    private BorderPane createDashboardRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Dashboard", false);
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(20));
        Label lbl = new Label("Dashboard");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lbl.setTextFill(Color.web("#1a252f"));
        mainBox.getChildren().add(lbl);
        List<String> userCourses = getEnrolledCourses(loggedInUser);
        HBox summary = new HBox(10);
        summary.getChildren().addAll(
                createSummaryCard("Enrolled", String.valueOf(userCourses.size())),
                createSummaryCard("Completed", "0"),
                createSummaryCard("Ongoing", String.valueOf(userCourses.size()))
        );
        mainBox.getChildren().add(summary);

        // Announcements
        VBox annBox = new VBox(10);
        annBox.setPadding(new Insets(10));
        Label annLbl = new Label("Announcements");
        annLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox annList = new VBox(5);
        boolean hasAnn = false;
        for (String code : userCourses) {
            List<Announcement> anns = getAnnouncementsForCourse(code);
            if (!anns.isEmpty()) {
                hasAnn = true;
                Label courseLbl = new Label(getCourseName(code));
                courseLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                courseLbl.setTextFill(Color.web("#1a252f"));
                annList.getChildren().add(courseLbl);
                for (Announcement a : anns) {
                    Label lblAnn = new Label("• " + a.content + " (by " + a.postedBy + ")");
                    lblAnn.setWrapText(true);
                    lblAnn.setFont(Font.font("Arial", 12));
                    lblAnn.setTextFill(Color.web("#666666"));
                    annList.getChildren().add(lblAnn);
                }
            }
        }
        if (!hasAnn) {
            annList.getChildren().add(new Label("No announcements."));
        }
        ScrollPane annScroll = new ScrollPane(annList);
        annScroll.setFitToWidth(true);
        annBox.getChildren().addAll(annLbl, annScroll);
        mainBox.getChildren().add(annBox);

        // Content
        VBox contentBox = new VBox(10);
        Label contentLbl = new Label("Content");
        contentLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox contentList = new VBox(5);
        boolean hasContent = false;
        for (String code : userCourses) {
            List<Content> cList = getContentsForCourse(code);
            if (!cList.isEmpty()) {
                hasContent = true;
                Label courseLbl = new Label(getCourseName(code));
                courseLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                courseLbl.setTextFill(Color.web("#1a252f"));
                contentList.getChildren().add(courseLbl);
                for (Content c : cList) {
                    Label lblC = new Label("• " + c.title + " (" + c.type + ")");
                    lblC.setFont(Font.font("Arial", 12));
                    lblC.setTextFill(Color.web("#666666"));
                    lblC.setWrapText(true);
                    contentList.getChildren().add(lblC);
                }
            }
        }
        if (!hasContent) {
            contentList.getChildren().add(new Label("No content."));
        }
        ScrollPane contentScroll = new ScrollPane(contentList);
        contentScroll.setFitToWidth(true);
        contentBox.getChildren().addAll(contentLbl, contentScroll);
        mainBox.getChildren().add(contentBox);

        // Assessments
        VBox assessBox = new VBox(10);
        Label assessLbl = new Label("Assessments");
        assessLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox assessList = new VBox(5);
        boolean hasAssess = false;
        for (String code : userCourses) {
            List<Assessment> aList = getAssessmentsForCourse(code);
            if (!aList.isEmpty()) {
                hasAssess = true;
                Label courseLbl = new Label(getCourseName(code));
                courseLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                courseLbl.setTextFill(Color.web("#1a252f"));
                assessList.getChildren().add(courseLbl);
                for (Assessment a : aList) {
                    HBox item = new HBox(10);
                    Label lblA = new Label(a.title + " (" + a.type + ")");
                    lblA.setFont(Font.font("Arial", 12));
                    lblA.setTextFill(Color.web("#666666"));
                    Button submitBtn = new Button("Submit");
                    submitBtn.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
                    final Assessment finalA = a;
                    submitBtn.setOnAction(e -> showAssessmentSubmission(finalA));
                    item.getChildren().addAll(lblA, submitBtn);
                    assessList.getChildren().add(item);
                }
            }
        }
        if (!hasAssess) {
            assessList.getChildren().add(new Label("No assessments."));
        }
        ScrollPane assessScroll = new ScrollPane(assessList);
        assessScroll.setFitToWidth(true);
        assessBox.getChildren().addAll( assessLbl, assessScroll);
        mainBox.getChildren().add(assessBox);

        // Discussion Forum
        VBox forumBox = new VBox(10);
        Label forumLbl = new Label("Discussion Forum");
        forumLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox forumList = new VBox(5);
        boolean hasMsg = false;
        for (String code : userCourses) {
            List<Message> msgs = getMessagesForCourse(code);
            if (!msgs.isEmpty()) {
                hasMsg = true;
                Label courseLbl = new Label(getCourseName(code));
                courseLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                courseLbl.setTextFill(Color.web("#1a252f"));
                forumList.getChildren().add(courseLbl);
                for (Message m : msgs) {
                    Label lblMsg = new Label(m.sender + ": " + m.content);
                    lblMsg.setWrapText(true);
                    lblMsg.setFont(Font.font("Arial", 12));
                    lblMsg.setTextFill(Color.web("#666666"));
                    forumList.getChildren().add(lblMsg);
                }
            }
        }
        Button btnPostMsg = new Button("Post Message");
        btnPostMsg.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnPostMsg.setOnAction(e -> showPostMessageDialog(null));
        forumList.getChildren().add(btnPostMsg);
        if (!hasMsg && !userCourses.isEmpty()) {
            forumList.getChildren().add(new Label("No messages."));
        }
        ScrollPane forumScroll = new ScrollPane(forumList);
        forumScroll.setFitToWidth(true);
        forumBox.getChildren().addAll(forumLbl, forumScroll);
        mainBox.getChildren().add(forumBox);

        ScrollPane mainScroll = new ScrollPane(mainBox);
        mainScroll.setFitToWidth(true);
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());
        root.setLeft(createSidebar("Dashboard", false));
        root.setCenter(mainScroll);
        return root;
    }

    private BorderPane createProfileRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Profile", false);
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(20));
        Label lbl = new Label("Your Profile");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lbl.setTextFill(Color.web("#1a252f"));
        User currentUser = getCurrentUser();
        Label userIdLbl = new Label("User ID: " + currentUser.userId);
        Label usernameLbl = new Label("Username: " + currentUser.username);
        Label fullNameLbl = new Label("Full Name: " + currentUser.fullName);
        Label roleLbl = new Label("Role: " + currentUser.role);
        Label facultyLbl = new Label("Faculty: " + (currentUser.faculty != null ? currentUser.faculty : "N/A"));
        mainBox.getChildren().addAll(lbl, userIdLbl, usernameLbl, fullNameLbl, roleLbl, facultyLbl);

        // Enrolled courses
        VBox enrolledBox = new VBox(10);
        enrolledBox.setPadding(new Insets(10));
        Label enrolledLbl = new Label("Enrolled Courses");
        enrolledLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        List<String> userCourses = getEnrolledCourses(loggedInUser);
        if (userCourses.isEmpty()) {
            grid.add(new Label("No enrolled courses."),0,0);
        } else {
            int col=0, row=0;
            for (String ccode : userCourses) {
                Course c = getCourseByCode(ccode);
                if (c != null) {
                    VBox card = new VBox(5);
                    card.setStyle("-fx-background-color:#fff; -fx-border-color:#e0e0e0; -fx-padding:10;");
                    Label lblCourse = new Label(c.name + "\n(" + c.code + ")");
                    Button btnUnenroll = new Button("Unenroll");
                    btnUnenroll.setStyle("-fx-background-color:#ff6f61; -fx-text-fill:#fff;");
                    final String cc = c.code;
                    btnUnenroll.setOnAction(e -> {
                        unenrollCourse(loggedInUser, cc);
                        refreshScenes();
                    });
                    card.getChildren().addAll(lblCourse, btnUnenroll);
                    grid.add(card, col, row);
                    col++;
                    if (col > 2) { col=0; row++; }
                }
            }
        }
        enrolledBox.getChildren().addAll(enrolledLbl, grid);
        ScrollPane enrollScroll = new ScrollPane(enrolledBox);
        enrollScroll.setFitToWidth(true);
        mainBox.getChildren().add(enrollScroll);
        ScrollPane sp = new ScrollPane(mainBox);
        sp.setFitToWidth(true);
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(sp);
        return root;
    }

    private BorderPane createLecturerDashboardRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Lecturer Dashboard", false);
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(20));
        Label lbl = new Label("Lecturer Dashboard");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lbl.setTextFill(Color.web("#1a252f"));
        mainBox.getChildren().add(lbl);
        // Manage Courses
        Label lblManage = new Label("Manage Courses");
        lblManage.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20);
        List<Course> facultyCourses = getCoursesByFaculty(getCurrentUser().faculty);
        int col=0, row=0;
        for (Course c : facultyCourses) {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color:#fff; -fx-border-color:#e0e0e0; -fx-padding:10;");
            Label lblCourse = new Label(c.name + "\n(" + c.code + ")");
            Button btnEdit = new Button("Edit");
            btnEdit.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
            final Course cc = c;
            btnEdit.setOnAction(e -> showEditCourseDialog(cc));
            Button btnDelete = new Button("Delete");
            btnDelete.setStyle("-fx-background-color:#ff6f61; -fx-text-fill:#fff;");
            btnDelete.setOnAction(e -> {
                deleteCourse(cc);
                refreshScenes();
            });
            card.getChildren().addAll(lblCourse, btnEdit, btnDelete);
            grid.add(card, 0, row);
            row++;
        }
        mainBox.getChildren().addAll(lblManage, grid);
        ScrollPane sp = new ScrollPane(mainBox);
        sp.setFitToWidth(true);
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(sp);
        return root;
    }

    private MenuBar createMenuBar() {
        MenuBar mb = new MenuBar();
        Menu home = new Menu("Home");
        MenuItem homeItem = new MenuItem("Go to Home");
        homeItem.setOnAction(e -> primaryStage.setScene(homeScene));
        home.getItems().add(homeItem);
        Menu courses = new Menu("Courses");
        MenuItem viewCourses = new MenuItem("View Courses");
        viewCourses.setOnAction(e -> primaryStage.setScene(createCourseRoot()));
        courses.getItems().add(viewCourses);
        Menu account = new Menu("Account");
        MenuItem signIn = new MenuItem("Sign In");
        signIn.setOnAction(e -> showSignInDialog());
        MenuItem signUp = new MenuItem("Sign Up");
        signUp.setOnAction(e -> showSignUpDialog());
        MenuItem dashboard = new MenuItem("Dashboard");
        dashboard.setOnAction(e -> {
            if (loggedInUser != null) {
                if (getCurrentUser().role.equals("Lecturer"))
                    primaryStage.setScene(lecturerDashboardScene);
                else
                    primaryStage.setScene(dashboardScene);
            } else showAlert("Sign in first");
        });
        MenuItem profile = new MenuItem("Profile");
        profile.setOnAction(e -> {
            if (loggedInUser != null) primaryStage.setScene(profileScene);
            else showAlert("Sign in first");
        });
        MenuItem signOut = new MenuItem("Sign Out");
        signOut.setOnAction(e -> {
            loggedInUser = null;
            primaryStage.setScene(homeScene);
        });
        account.getItems().addAll(signIn, signUp, new SeparatorMenuItem(), dashboard, profile, signOut);
        mb.getMenus().addAll(home, courses, account);
        return mb;
    }

    // --- Dialogs ---
    private void showSignInDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(primaryStage);
        dlg.setTitle("Sign In");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        TextField tfUser = new TextField(); tfUser.setPromptText("Username");
        PasswordField pfPass = new PasswordField(); pfPass.setPromptText("Password");
        Button btnSignIn = new Button("Sign In");
        btnSignIn.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnSignIn.setOnAction(e -> {
            String u = tfUser.getText().trim();
            String p = pfPass.getText().trim();
            if (u.isEmpty() || p.isEmpty()) { showAlert("Fill all fields"); return; }
            User user = getUserByUsername(u);
            if (user == null) { showAlert("User not found"); return; }
            if (!user.password.equals(p)) { showAlert("Incorrect password"); return; }
            loggedInUser = u;
            showAlert("Signed in as " + user.role);
            dlg.close();
            refreshScenes();
            if (user.role.equals("Lecturer")) primaryStage.setScene(lecturerDashboardScene);
            else primaryStage.setScene(dashboardScene);
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnSignIn, btnCancel);
        hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Sign In"), tfUser, pfPass, hb);
        dlg.setScene(new Scene(box, 300, 200));
        dlg.showAndWait();
    }

    private void showSignUpDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(primaryStage);
        dlg.setTitle("Sign Up");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        TextField tfUser = new TextField(); tfUser.setPromptText("Username");
        PasswordField pfPass = new PasswordField(); pfPass.setPromptText("Password");
        TextField tfFull = new TextField(); tfFull.setPromptText("Full Name");
        ComboBox<String> cbRole = new ComboBox<>(); cbRole.getItems().addAll("Student","Lecturer");
        cbRole.setPromptText("Role");
        ComboBox<String> cbFaculty = new ComboBox<>(); cbFaculty.getItems().addAll("Design","Business","IT","Communication");
        cbFaculty.setPromptText("Faculty (for Lecturer)");
        cbRole.valueProperty().addListener((obs,old,newVal) -> {
            cbFaculty.setDisable(!"Lecturer".equals(newVal));
        });
        Button btnSignUp = new Button("Sign Up");
        btnSignUp.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnSignUp.setOnAction(e -> {
            String u = tfUser.getText().trim();
            String p = pfPass.getText().trim();
            String f = tfFull.getText().trim();
            String role = cbRole.getValue();
            String faculty = cbFaculty.getValue();
            if (u.isEmpty() || p.isEmpty() || f.isEmpty() || role == null) { showAlert("Fill all fields"); return; }
            if (getUserByUsername(u) != null) { showAlert("Username exists"); return; }
            String uid = (role.equals("Student") ? "STU-" : "LEC-") + (1000 + new Random().nextInt(9000));
            saveUser(new User(uid, u, p, f, role, "Lecturer".equals(role)?faculty:null));
            showAlert("Account created");
            dlg.close();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnSignUp, btnCancel);
        hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Sign Up"), tfUser, pfPass, tfFull, cbRole, cbFaculty, hb);
        dlg.setScene(new Scene(box, 350, 350));
        dlg.showAndWait();
    }

    private void saveUser(User user) {
        String sql = "INSERT INTO users (user_id, username, password, full_name, role, faculty) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.userId);
            ps.setString(2, user.username);
            ps.setString(3, user.password);
            ps.setString(4, user.fullName);
            ps.setString(5, user.role);
            ps.setString(6, user.faculty);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private User getCurrentUser() {
        return getUserByUsername(loggedInUser);
    }

    private void deleteCourse(Course c) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE code=?");
            ps.setString(1, c.code);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        // delete related data
        deleteCourseRelatedData(c.code);
        refreshScenes();
    }

    private void deleteCourseRelatedData(String code) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.createStatement().executeUpdate("DELETE FROM announcements WHERE course_code='" + code + "'");
            conn.createStatement().executeUpdate("DELETE FROM contents WHERE course_code='" + code + "'");
            conn.createStatement().executeUpdate("DELETE FROM assessments WHERE course_code='" + code + "'");
            conn.createStatement().executeUpdate("DELETE FROM messages WHERE course_code='" + code + "'");
            conn.createStatement().executeUpdate("DELETE FROM enrollments WHERE course_code='" + code + "'");
            conn.createStatement().executeUpdate("DELETE FROM submissions WHERE assessment_id IN (SELECT id FROM assessments WHERE course_code='" + code + "')");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateCourseCodeInCollections(String oldCode, String newCode) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Update assessments
            PreparedStatement psAssessments = conn.prepareStatement("UPDATE assessments SET course_code=? WHERE course_code=?");
            psAssessments.setString(1, newCode);
            psAssessments.setString(2, oldCode);
            psAssessments.executeUpdate();

            // Update contents
            PreparedStatement psContents = conn.prepareStatement("UPDATE contents SET course_code=? WHERE course_code=?");
            psContents.setString(1, newCode);
            psContents.setString(2, oldCode);
            psContents.executeUpdate();

            // Update messages
            PreparedStatement psMessages = conn.prepareStatement("UPDATE messages SET course_code=? WHERE course_code=?");
            psMessages.setString(1, newCode);
            psMessages.setString(2, oldCode);
            psMessages.executeUpdate();

            // Update enrollments
            PreparedStatement psEnrollments = conn.prepareStatement("UPDATE enrollments SET course_code=? WHERE course_code=?");
            psEnrollments.setString(1, newCode);
            psEnrollments.setString(2, oldCode);
            psEnrollments.executeUpdate();

            // Update submissions' assessment_id if necessary
            // This part might need adjustment depending on your schema
            // For safety, fetching assessments IDs before and updating submissions accordingly
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Reports ---
    private void showClassPerformanceReport() {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Class Performance");

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));

        Label lbl = new Label("Class Performance");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        ComboBox<String> cbCourse = new ComboBox<>();
        for (Course c : getCoursesByFaculty(getCurrentUser().faculty))
            cbCourse.getItems().add(c.name + " (" + c.code + ")");
        cbCourse.setPromptText("Select Course");

        VBox resultBox = new VBox(10);

        cbCourse.setOnAction(e -> {
            resultBox.getChildren().clear();
            String sel = cbCourse.getValue();
            if (sel != null) {
                String code = sel.substring(sel.lastIndexOf("(") + 1, sel.length() - 1);
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String sql = "SELECT student_username, AVG(score) as avg_score FROM submissions WHERE assessment_id IN (SELECT id FROM assessments WHERE course_code=?) GROUP BY student_username";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, code);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Label studentScoreLabel = new Label(rs.getString("student_username") + ": " + String.format("%.2f", rs.getDouble("avg_score")));
                        resultBox.getChildren().add(studentScoreLabel);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button btnClose = new Button("Close");
        btnClose.setOnAction(e -> dlg.close());

        box.getChildren().addAll(lbl, cbCourse, resultBox, btnClose);
        dlg.setScene(new Scene(box, 600, 400));
        dlg.showAndWait();
    }

    private void showCertificationDialog() {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Issue Certifications");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        Label lbl = new Label("Issue Certification");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        ComboBox<String> cbCourse = new ComboBox<>();
        for (Course c : getCoursesByFaculty(getCurrentUser().faculty))
            cbCourse.getItems().add(c.name + " (" + c.code + ")");
        TextField tfStudent = new TextField(); tfStudent.setPromptText("Student Username");
        Button btnIssue = new Button("Issue");
        btnIssue.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnIssue.setOnAction(e -> {
            String courseSel = cbCourse.getValue();
            String studentUser = tfStudent.getText().trim();
            if (courseSel == null || studentUser.isEmpty()) { showAlert("Fill all"); return; }
            User student = getUserByUsername(studentUser);
            if (student == null || !student.role.equals("Student")) { showAlert("Invalid student"); return; }
            String courseCode = courseSel.substring(courseSel.lastIndexOf("(")+1, courseSel.length()-1);
            List<String> enrolled = getEnrolledCourses(studentUser);
            if (!enrolled.contains(courseCode)) { showAlert("Student not enrolled"); return; }
            // For now, just alert
            showAlert("Certificate issued to " + studentUser);
            dlg.close();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnIssue, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(lbl, cbCourse, tfStudent, hb);
        dlg.setScene(new Scene(box, 350, 200));
        dlg.showAndWait();
    }

    private void showEditAnnouncementDialog(Announcement a) {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Edit Announcement");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        TextArea taContent = new TextArea(a.content);
        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnSave.setOnAction(e -> {
            String newContent = taContent.getText().trim();
            if (newContent.isEmpty()) { showAlert("Content cannot be empty"); return; }
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement("UPDATE announcements SET content=? WHERE id=?");
                ps.setString(1, newContent);
                ps.setInt(2, Integer.parseInt(a.id));
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert("Updated");
            dlg.close();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnSave, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Edit Announcement"), taContent, hb);
        dlg.setScene(new Scene(box, 400, 300));
        dlg.showAndWait();
    }

    private void showPostAnnouncementDialog() {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Post Announcement");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        ComboBox<String> cbCourse = new ComboBox<>();
        for (Course c : getCoursesByFaculty(getCurrentUser().faculty))
            cbCourse.getItems().add(c.name + " (" + c.code + ")");
        TextArea taContent = new TextArea(); taContent.setPromptText("Content");
        Button btnPost = new Button("Post");
        btnPost.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnPost.setOnAction(e -> {
            String sel = cbCourse.getValue();
            String content = taContent.getText().trim();
            if (sel == null || content.isEmpty()) { showAlert("Fill all"); return; }
            String courseCode = sel.substring(sel.lastIndexOf("(")+1, sel.length()-1);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO announcements (course_code, content, posted_by) VALUES (?, ?, ?)");
                ps.setString(1, courseCode);
                ps.setString(2, content);
                ps.setString(3, getCurrentUser().username);
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert("Posted");
            dlg.close();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnPost, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Post Announcement"), cbCourse, taContent, hb);
        dlg.setScene(new Scene(box, 400, 300));
        dlg.showAndWait();
    }

    private void showUploadContentDialog() {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Upload Content");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        ComboBox<String> cbCourse = new ComboBox<>();
        for (Course c : getCoursesByFaculty(getCurrentUser().faculty))
            cbCourse.getItems().add(c.name + " (" + c.code + ")");
        TextField tfTitle = new TextField(); tfTitle.setPromptText("Content Title");
        ComboBox<String> cbType = new ComboBox<>(); cbType.getItems().addAll("Video","PDF");
        cbType.setPromptText("Content Type");
        Button btnUpload = new Button("Upload");
        btnUpload.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnUpload.setOnAction(e -> {
            String sel = cbCourse.getValue();
            String title = tfTitle.getText().trim();
            String type = cbType.getValue();
            if (sel == null || title.isEmpty() || type == null) { showAlert("Fill all"); return; }
            String courseCode = sel.substring(sel.lastIndexOf("(")+1, sel.length()-1);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO contents (course_code, title, type, data) VALUES (?, ?, ?, ?)");
                ps.setString(1, courseCode);
                ps.setString(2, title);
                ps.setString(3, type);
                ps.setString(4, "Sample data");
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert("Content uploaded");
            dlg.close();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnUpload, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Upload Content"), cbCourse, tfTitle, cbType, hb);
        dlg.setScene(new Scene(box, 400, 300));
        dlg.showAndWait();
    }

    private void showCreateAssessmentDialog() {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Create Assessment");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        ComboBox<String> cbCourse = new ComboBox<>();
        for (Course c : getCoursesByFaculty(getCurrentUser().faculty))
            cbCourse.getItems().add(c.name + " (" + c.code + ")");
        TextField tfTitle = new TextField(); tfTitle.setPromptText("Assessment Title");
        ComboBox<String> cbType = new ComboBox<>(); cbType.getItems().addAll("Multiple Choice","Short Answer");
        cbType.setPromptText("Assessment Type");
        TextField tfWeight = new TextField(); tfWeight.setPromptText("Weight (0-1)");
        TextArea taQuestions = new TextArea(); taQuestions.setPromptText("Questions (one per line)\nFor MCQ: Question|Opt1|Opt2|Opt3|Opt4|Correct");
        Button btnCreate = new Button("Create");
        btnCreate.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnCreate.setOnAction(e -> {
            if (cbCourse.getValue() == null || tfTitle.getText().trim().isEmpty() || cbType.getValue() == null || tfWeight.getText().trim().isEmpty() || taQuestions.getText().trim().isEmpty()) {
                showAlert("Fill all");
                return;
            }
            double weight;
            try { weight=Double.parseDouble(tfWeight.getText()); }
            catch (Exception ex) { showAlert("Invalid weight"); return; }
            if (weight<0 || weight>1) { showAlert("Weight between 0 and 1"); return; }
            String courseCode = cbCourse.getValue().substring(cbCourse.getValue().lastIndexOf("(")+1, cbCourse.getValue().length()-1);
            List<String> questions = Arrays.asList(taQuestions.getText().split("\n"));
            for (String q : questions) {
                if (cbType.getValue().equals("Multiple Choice") && q.split("\\|").length != 6) {
                    showAlert("MCQ format: Question|Opt1|Opt2|Opt3|Opt4|Correct");
                    return;
                }
            }
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO assessments (course_code, title, type, weight, questions, posted_by) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, courseCode);
                ps.setString(2, tfTitle.getText().trim());
                ps.setString(3, cbType.getValue());
                ps.setDouble(4, weight);
                ps.setString(5, String.join("||", questions));
                ps.setString(6, getCurrentUser().username);
                ps.executeUpdate();
                refreshScenes();
                showAlert("Assessment created");
                dlg.close();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnCreate, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Create Assessment"), cbCourse, tfTitle, cbType, tfWeight, taQuestions, hb);
        dlg.setScene(new Scene(box, 400, 400));
        dlg.showAndWait();
    }

    private void showEditAssessmentDialog(Assessment a) {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Edit Assessment");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        TextField tfTitle = new TextField(a.title);
        TextField tfWeight = new TextField(String.valueOf(a.weight));
        TextArea taQuestions = new TextArea(String.join("||", a.questions));
        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnSave.setOnAction(e -> {
            if (tfTitle.getText().trim().isEmpty() || tfWeight.getText().trim().isEmpty() || taQuestions.getText().trim().isEmpty()) {
                showAlert("Fill all");
                return;
            }
            double weight;
            try { weight=Double.parseDouble(tfWeight.getText()); }
            catch (Exception ex) { showAlert("Invalid weight"); return; }
            if (weight<0 || weight>1) { showAlert("Weight between 0 and 1"); return; }
            List<String> questions = Arrays.asList(taQuestions.getText().split("\\|\\|"));
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement("UPDATE assessments SET title=?, weight=?, questions=? WHERE id=?");
                ps.setString(1, tfTitle.getText().trim());
                ps.setDouble(2, weight);
                ps.setString(3, String.join("||", questions));
                ps.setInt(4, Integer.parseInt(a.id));
                ps.executeUpdate();
                refreshScenes();
                showAlert("Assessment updated");
                dlg.close();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnSave, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Edit Assessment"), tfTitle, tfWeight, taQuestions, hb);
        dlg.setScene(new Scene(box, 400, 300));
        dlg.showAndWait();
    }

    private void deleteAssessment(Assessment a) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM assessments WHERE id=?");
            ps.setInt(1, Integer.parseInt(a.id));
            ps.executeUpdate();
            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM submissions WHERE assessment_id=?");
            ps2.setInt(1, Integer.parseInt(a.id));
            ps2.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        refreshScenes();
    }

    // --- Submit Assessment ---
    private void showAssessmentSubmission(Assessment a) {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Submit " + a.title);
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        List<String> answers = new ArrayList<>();
        VBox questionsBox = new VBox(10);
        for (int i=0; i<a.questions.size(); i++) {
            String q = a.questions.get(i);
            String[] parts = q.split("\\|");
            String questionText = parts[0];
            Label lblQ = new Label((i+1)+". "+questionText);
            lblQ.setWrapText(true);
            if (a.type.equals("Multiple Choice")) {
                ToggleGroup tg = new ToggleGroup();
                VBox optBox = new VBox(5);
                for (int j=1;j<=4;j++) {
                    RadioButton rb = new RadioButton(parts[j]);
                    rb.setToggleGroup(tg);
                    optBox.getChildren().add(rb);
                }
                int idx = i;
                tg.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                    if (answers.size() <= idx) answers.add(null);
                    answers.set(idx, newVal == null ? null : ((RadioButton)newVal).getText());
                });
                questionsBox.getChildren().addAll(lblQ, optBox);
            } else {
                TextArea ta = new TextArea();
                ta.setPromptText("Your answer");
                int idx = i;
                ta.textProperty().addListener((obs, old, newVal) -> {
                    if (answers.size() <= idx) answers.add(null);
                    answers.set(idx, newVal.trim());
                });
                questionsBox.getChildren().addAll(lblQ, ta);
            }
        }
        Button btnSubmit = new Button("Submit");
        btnSubmit.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnSubmit.setOnAction(e -> {
            if (answers.contains(null) || answers.size() != a.questions.size()) {
                showAlert("Please answer all questions");
                return;
            }
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO submissions (assessment_id, student_username, answers, score) VALUES (?, ?, ?, ?)");
                ps.setInt(1, Integer.parseInt(a.id));
                ps.setString(2, loggedInUser);
                ps.setString(3, String.join("||", answers));
                ps.setDouble(4, 0.0);
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert("Submitted");
            dlg.close();
            refreshScenes();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnSubmit, btnCancel);
        hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Answer Questions"), questionsBox, hb);
        dlg.setScene(new Scene(box, 500, 400));
        dlg.showAndWait();
    }

    // --- Grade assessment ---
    private void showGradeAssessmentDialog(Assessment a) {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Grade " + a.title);
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        Label lbl = new Label("Submissions for " + a.title);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox subsBox = new VBox(10);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT * FROM submissions WHERE assessment_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(a.id));
            ResultSet rs = ps.executeQuery();
            boolean hasSub = false;
            while (rs.next()) {
                hasSub = true;
                String studentUser = rs.getString("student_username");
                String ansStr = rs.getString("answers");
                double score = rs.getDouble("score");
                List<String> answers = Arrays.asList(ansStr.split("\\|\\|"));
                VBox subBox = new VBox(5);
                Label lblStudent = new Label("Student: " + studentUser);
                lblStudent.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                Label lblAnswers = new Label("Answers: " + String.join("; ", answers));
                Label lblScore = new Label("Score: " + score);
                TextField tfScore = new TextField(String.valueOf(score));
                tfScore.setPromptText("Enter score");
                Button btnSave = new Button("Save");
                btnSave.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
                btnSave.setOnAction(ev -> {
                    try {
                        double sc = Double.parseDouble(tfScore.getText());
                        if (sc < 0 || sc > 100) { showAlert("Score 0-100"); return; }
                        try (Connection c2 = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                            PreparedStatement ps2 = c2.prepareStatement("UPDATE submissions SET score=? WHERE id=?");
                            ps2.setDouble(1, sc);
                            ps2.setInt(2, rs.getInt("id"));
                            ps2.executeUpdate();
                        }
                        showAlert("Score saved");
                        refreshScenes();
                    } catch (Exception ex) { showAlert("Invalid score"); }
                });
                subBox.getChildren().addAll(lblStudent, lblAnswers, lblScore, tfScore, btnSave);
                subsBox.getChildren().add(subBox);
            }
            if (!hasSub) {
                subsBox.getChildren().add(new Label("No submissions."));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        Button btnClose = new Button("Close");
        btnClose.setStyle("-fx-background-color:#fff; -fx-border-color:#00c4b4; -fx-text-fill:#00c4b4;");
        btnClose.setOnAction(e -> dlg.close());
        box.getChildren().addAll(lbl, new ScrollPane(subsBox), btnClose);
        dlg.setScene(new Scene(box, 600, 600));
        dlg.showAndWait();
    }

    private void showTranscriptReport() {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Student Transcripts");

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));

        Label lbl = new Label("Student Transcripts");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TextField tfStudent = new TextField();
        tfStudent.setPromptText("Enter Student Username");

        VBox resultBox = new VBox(10);

        tfStudent.textProperty().addListener((obs, old, newVal) -> {
            resultBox.getChildren().clear();
            String username = newVal.trim();
            if (username.isEmpty()) return;

            User user = getUserByUsername(username);
            if (user == null || !user.role.equals("Student")) {
                resultBox.getChildren().add(new Label("Invalid student"));
                return;
            }

            List<String> courses = getEnrolledCourses(username);
            for (String ccode : courses) {
                Label courseLabel = new Label(getCourseName(ccode));
                courseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                resultBox.getChildren().add(courseLabel);
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String sql = "SELECT a.title, s.score FROM assessments a JOIN submissions s ON a.id=s.assessment_id WHERE s.student_username=? AND a.course_code=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, username);
                    ps.setString(2, ccode);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Label assessmentLabel = new Label(rs.getString("title") + ": " + rs.getDouble("score"));
                        assessmentLabel.setFont(Font.font("Arial", 12));
                        resultBox.getChildren().add(assessmentLabel);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button btnClose = new Button("Close");
        btnClose.setOnAction(e -> dlg.close());

        ScrollPane scrollPane = new ScrollPane(resultBox);
        scrollPane.setFitToWidth(true);

        box.getChildren().addAll(lbl, tfStudent, scrollPane, btnClose);
        dlg.setScene(new Scene(box, 600, 600));
        dlg.showAndWait();
    }

    private String getCourseName(String courseCode) {
        Course c = getCourseByCode(courseCode);
        return c != null ? c.name : "Unknown Course";
    }

    private void showPostMessageDialog(String courseCode) {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Post Message");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        TextArea taMsg = new TextArea(); taMsg.setPromptText("Your message");
        Button btnPost = new Button("Post");
        btnPost.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnPost.setOnAction(e -> {
            String msg = taMsg.getText().trim();
            if (msg.isEmpty()) { showAlert("Message cannot be empty"); return; }
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO messages (course_code, sender, content) VALUES (?, ?, ?)");
                ps.setString(1, courseCode);
                ps.setString(2, loggedInUser);
                ps.setString(3, msg);
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert("Message posted");
            dlg.close();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnPost, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Post Message"), taMsg, hb);
        dlg.setScene(new Scene(box, 400, 300));
        dlg.showAndWait();
    }

    private void showCreateCourseDialog() {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Create Course");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        TextField tfName = new TextField(); tfName.setPromptText("Course Name");
        TextField tfCode = new TextField(); tfCode.setPromptText("Course Code");
        Button btnCreate = new Button("Create");
        btnCreate.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnCreate.setOnAction(e -> {
            String name = tfName.getText().trim();
            String code = tfCode.getText().trim().toUpperCase();
            String faculty = getCurrentUser().faculty;
            if (name.isEmpty() || code.isEmpty()) { showAlert("Fill all"); return; }
            if (getCourseByCode(code) != null) { showAlert("Code exists"); return; }
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO courses (code, name, faculty) VALUES (?, ?, ?)");
                ps.setString(1, code); ps.setString(2, name); ps.setString(3, faculty);
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }
            refreshScenes(); dlg.close();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnCreate, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Create Course"), tfName, tfCode, hb);
        dlg.setScene(new Scene(box, 300, 200));
        dlg.showAndWait();
    }

    private void showEditCourseDialog(Course c) {
        Stage dlg = new Stage();
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Edit Course");
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        TextField tfName = new TextField(c.name);
        TextField tfCode = new TextField(c.code);
        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnSave.setOnAction(e -> {
            String newName = tfName.getText().trim();
            String newCode = tfCode.getText().trim().toUpperCase();
            if (newName.isEmpty() || newCode.isEmpty()) { showAlert("Fill all"); return; }
            if (!newCode.equals(c.code) && getCourseByCode(newCode) != null) { showAlert("Code exists"); return; }
            String oldCode = c.code;
            c.name = newName; c.code = newCode;
            updateCourseCodeInCollections(oldCode, newCode);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement("UPDATE courses SET name=?, code=? WHERE code=?");
                ps.setString(1, newName); ps.setString(2, newCode); ps.setString(3, oldCode);
                ps.executeUpdate();
            } catch (SQLException ex) { ex.printStackTrace(); }
            refreshScenes(); dlg.close();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> dlg.close());
        HBox hb = new HBox(10, btnSave, btnCancel); hb.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Edit Course"), tfName, tfCode, hb);
        dlg.setScene(new Scene(box, 300, 200));
        dlg.showAndWait();
    }

    // --- Assessment CRUD ---
    private List<Assessment> getAssessmentsForCourse(String courseCode) {
        List<Assessment> list = new ArrayList<>();
        String sql = "SELECT * FROM assessments WHERE course_code=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    String qStr = rs.getString("questions");
                    List<String> questions = Arrays.asList(qStr.split("\\|\\|"));
                    list.add(new Assessment(rs.getString("id"), rs.getString("course_code"), rs.getString("title"), rs.getString("type"), rs.getDouble("weight"), questions, rs.getString("posted_by")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private VBox createSidebar(String title, boolean isHome) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color:#f0f0f0;");

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitle.setTextFill(Color.web("#1a252f"));

        Button btnHome = new Button("Home");
        btnHome.setMaxWidth(Double.MAX_VALUE);
        btnHome.setOnAction(e -> primaryStage.setScene(homeScene));

        Button btnCourses = new Button("Courses");
        btnCourses.setMaxWidth(Double.MAX_VALUE);
        btnCourses.setOnAction(e -> primaryStage.setScene(createCourseRoot()));

        Button btnDashboard = new Button("Dashboard");
        btnDashboard.setMaxWidth(Double.MAX_VALUE);
        btnDashboard.setOnAction(e -> {
            if (loggedInUser != null) {
                if (getCurrentUser().role.equals("Lecturer"))
                    primaryStage.setScene(lecturerDashboardScene);
                else
                    primaryStage.setScene(dashboardScene);
            } else {
                showAlert("Sign in first");
            }
        });

        Button btnProfile = new Button("Profile");
        btnProfile.setMaxWidth(Double.MAX_VALUE);
        btnProfile.setOnAction(e -> {
            if (loggedInUser != null) primaryStage.setScene(profileScene);
            else showAlert("Sign in first");
        });

        sidebar.getChildren().addAll(lblTitle, btnHome, btnCourses, btnDashboard, btnProfile);
        return sidebar;
    }

    // --- Utility ---
    private List<Course> getCoursesByFaculty(String faculty) {
        return getAllCourses().stream().filter(c -> c.faculty.equals(faculty)).collect(Collectors.toList());
    }

    private void refreshScenes() {
        homeScene = new Scene(createHomeRoot(), 1000, 700);
        courseScene = new Scene(createCourseRoot().getRoot(), 1000, 700);
        dashboardScene = new Scene(createDashboardRoot(), 1000, 700);
        profileScene = new Scene(createProfileRoot(), 1000, 700);
        lecturerDashboardScene = new Scene(createLecturerDashboardRoot(), 1000, 700);
    }

    private VBox createSummaryCard(String title, String value) {
        VBox v = new VBox(10);
        v.setStyle("-fx-background-color:#fff; -fx-border-color:#e0e0e0; -fx-padding:10;");
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label lblVal = new Label(value);
        lblVal.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblVal.setTextFill(Color.web("#00c4b4"));
        v.getChildren().addAll(lblTitle, lblVal);
        return v;
    }

    private VBox createCourseCard(String name, String code) {
        VBox v = new VBox(10);
        v.setPadding(new Insets(10));
        v.setStyle("-fx-background-color:#fff; -fx-border-color:#e0e0e0; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        Label lbl = new Label(name + "\n(" + code + ")");
        lbl.setWrapText(true);
        Button btnEnroll = new Button("Enroll");
        btnEnroll.setStyle("-fx-background-color:#00c4b4; -fx-text-fill:#fff;");
        btnEnroll.setOnAction(e -> {
            if (loggedInUser == null) { showAlert("Sign in first"); return; }
            if (getUserByUsername(loggedInUser).role.equals("Lecturer")) { showAlert("Lecturers can't enroll"); return; }
            enrollCourse(loggedInUser, code);
            refreshScenes();
        });
        v.getChildren().addAll(lbl, btnEnroll);
        return v;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
