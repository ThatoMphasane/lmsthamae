package com.example.demo1;


import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Circle;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.*;
import java.util.stream.Collectors;

public class HelloApplication extends Application {
    private Stage primaryStage;
    private Scene homeScene;
    private Scene courseScene;
    private Scene dashboardScene;
    private Scene profileScene;
    private Scene lecturerDashboardScene;
    private Map<String, User> users; // username -> User
    private Map<String, List<String>> enrolledCourses; // username -> enrolled course codes
    private List<Course> allCourses; // All courses with codes and faculty
    private Map<String, List<Announcement>> announcements; // courseCode -> announcements
    private List<Notification> notifications; // System-wide notifications
    private Map<String, List<Content>> courseContent; // courseCode -> videos/PDFs
    private Map<String, List<Message>> messages; // courseCode -> message threads
    private Map<String, List<Assessment>> assessments; // courseCode -> assessments
    private Map<String, Map<String, List<Submission>>> submissions; // courseCode -> student -> submissions
    private String loggedInUser;
    private Random random = new Random();

    // Data classes
    private static class User {
        String userId; // e.g., STU-1234, LEC-1234
        String username;
        String password;
        String fullName;
        String role; // Student or Lecturer
        String faculty; // Faculty for lecturers (null for students)

        User(String userId, String username, String password, String fullName, String role, String faculty) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.role = role;
            this.faculty = faculty;
        }
    }

    private static class Course {
        String name;
        String code; // e.g., ARC101
        String faculty;

        Course(String name, String code, String faculty) {
            this.name = name;
            this.code = code;
            this.faculty = faculty;
        }
    }

    private static class Announcement {
        String id; // Unique ID
        String courseCode;
        String content;
        String postedBy;

        Announcement(String id, String courseCode, String content, String postedBy) {
            this.id = id;
            this.courseCode = courseCode;
            this.content = content;
            this.postedBy = postedBy;
        }
    }

    private static class Notification {
        String id; // Unique ID
        String content;

        Notification(String id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    private static class Content {
        String id; // Unique ID
        String title;
        String type; // Video or PDF
        String data; // Simulated content

        Content(String id, String title, String type, String data) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.data = data;
        }
    }

    private static class Message {
        String id; // Unique ID
        String courseCode;
        String sender;
        String content;
        long timestamp;

        Message(String id, String courseCode, String sender, String content) {
            this.id = id;
            this.courseCode = courseCode;
            this.sender = sender;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static class Assessment {
        String id; // Unique ID
        String courseCode;
        String title;
        String type; // MCQ or ShortAnswer
        double weight; // 0.0 to 1.0
        List<String> questions; // For MCQ: "Question|Opt1|Opt2|Opt3|Opt4|Correct", ShortAnswer: "Question"
        String postedBy;

        Assessment(String id, String courseCode, String title, String type, double weight, List<String> questions, String postedBy) {
            this.id = id;
            this.courseCode = courseCode;
            this.title = title;
            this.type = type;
            this.weight = weight;
            this.questions = questions;
            this.postedBy = postedBy;
        }
    }

    private static class Submission {
        String assessmentId;
        String studentUsername;
        List<String> answers; // For MCQ: selected options, ShortAnswer: text answers
        double score; // 0.0 to 100.0

        Submission(String assessmentId, String studentUsername, List<String> answers, double score) {
            this.assessmentId = assessmentId;
            this.studentUsername = studentUsername;
            this.answers = answers;
            this.score = score;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("University of Cape Town Learning Management System");

        // Initialize data
        users = new HashMap<>();
        users.put("student", new User("STU-1001", "student", "pass123", "Student User", "Student", null));
        users.put("lecturer", new User("LEC-1001", "lecturer", "pass456", "Lecturer User", "Lecturer", "Design"));

        enrolledCourses = new HashMap<>();
        allCourses = generateUCTCourses();
        announcements = new HashMap<>();
        notifications = new ArrayList<>();
        courseContent = new HashMap<>();
        messages = new HashMap<>();
        assessments = new HashMap<>();
        submissions = new HashMap<>();
        loggedInUser = null;

        // Create scenes
        homeScene = new Scene(createHomeRoot(), 1000, 700);
        courseScene = new Scene(createCourseRoot(), 1000, 700);
        dashboardScene = new Scene(createDashboardRoot(), 1000, 700);
        profileScene = new Scene(createProfileRoot(), 1000, 700);
        lecturerDashboardScene = new Scene(createLecturerDashboardRoot(), 1000, 700);

        primaryStage.setScene(homeScene);
        primaryStage.show();
    }

    private List<Course> generateUCTCourses() {
        List<Course> courses = new ArrayList<>();
        // Design Faculty
        courses.add(new Course("Architecture", "ARC101", "Design"));
        courses.add(new Course("Graphic Design", "GRD102", "Design"));
        courses.add(new Course("Fashion Design", "FAD103", "Design"));
        // Business Faculty
        courses.add(new Course("Business Management", "BUS201", "Business"));
        courses.add(new Course("Marketing", "MKT202", "Business"));
        courses.add(new Course("Accounting", "ACC203", "Business"));
        // IT Faculty
        courses.add(new Course("Computer Science", "CSC301", "IT"));
        courses.add(new Course("Information Systems", "INF302", "IT"));
        courses.add(new Course("Cybersecurity", "CYB303", "IT"));
        // Communication Faculty
        courses.add(new Course("Media Studies", "MED401", "Communication"));
        courses.add(new Course("Public Relations", "PRL402", "Communication"));
        courses.add(new Course("Journalism", "JRN403", "Communication"));
        return courses;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: white; -fx-font-size: 14;");

        Menu homeMenu = new Menu("Home");
        homeMenu.setStyle("-fx-text-fill: white;");
        MenuItem homeItem = new MenuItem("Go to Home");
        homeItem.setOnAction(e -> primaryStage.setScene(homeScene));
        homeMenu.getItems().add(homeItem);

        Menu coursesMenu = new Menu("Courses");
        coursesMenu.setStyle("-fx-text-fill: white;");
        MenuItem coursesItem = new MenuItem("View Courses");
        coursesItem.setOnAction(e -> primaryStage.setScene(courseScene));
        coursesMenu.getItems().add(coursesItem);

        Menu accountMenu = new Menu("Account");
        accountMenu.setStyle("-fx-text-fill: white;");

        MenuItem loginItem = new MenuItem("Sign In");
        loginItem.setOnAction(e -> showSignInForm());

        MenuItem signupItem = new MenuItem("Sign Up");
        signupItem.setOnAction(e -> showSignUpForm());

        MenuItem dashboardItem = new MenuItem("Dashboard");
        dashboardItem.setOnAction(e -> {
            if (loggedInUser != null) {
                primaryStage.setScene(users.get(loggedInUser).role.equals("Lecturer") ? lecturerDashboardScene : dashboardScene);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please sign in first");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        MenuItem profileItem = new MenuItem("Profile");
        profileItem.setOnAction(e -> {
            if (loggedInUser != null) {
                primaryStage.setScene(profileScene);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please sign in first");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        MenuItem logoutItem = new MenuItem("Sign Out");
        logoutItem.setOnAction(e -> {
            loggedInUser = null;
            enrolledCourses.remove("student"); // Reset for demo
            primaryStage.setScene(homeScene);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Signed out successfully");
            alert.setHeaderText(null);
            alert.showAndWait();
        });

        accountMenu.getItems().addAll(loginItem, signupItem, new SeparatorMenuItem(), dashboardItem, profileItem, logoutItem);
        menuBar.getMenus().addAll(homeMenu, coursesMenu, accountMenu);
        return menuBar;
    }

    private void showSignInForm() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Sign In");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Sign In");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-font-size: 14;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14;");

        Button signInButton = new Button("Sign In");
        signInButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, signInButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        signInButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Username and password are required");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (!users.containsKey(username)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Username not found");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (!users.get(username).password.equals(password)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Incorrect password");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                loggedInUser = username;
                notifications.add(new Notification(UUID.randomUUID().toString(), "User '" + username + "' signed in"));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Signed in successfully as " + users.get(username).role);
                alert.setHeaderText(null);
                alert.showAndWait();
                dialog.close();
                refreshScenes();
                primaryStage.setScene(users.get(loggedInUser).role.equals("Lecturer") ? lecturerDashboardScene : dashboardScene);
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, usernameField, passwordField, buttonBox);

        Scene dialogScene = new Scene(form, 300, 250);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showSignUpForm() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Sign Up");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Sign Up");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-font-size: 14;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setStyle("-fx-font-size: 14;");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Student", "Lecturer");
        roleCombo.setPromptText("Select Role");
        roleCombo.setStyle("-fx-font-size: 14;");

        ComboBox<String> facultyCombo = new ComboBox<>();
        facultyCombo.getItems().addAll("Design", "Business", "IT", "Communication");
        facultyCombo.setPromptText("Select Faculty (Lecturers only)");
        facultyCombo.setStyle("-fx-font-size: 14;");
        facultyCombo.setDisable(true); // Disabled by default until lecturer is selected

        // Enable faculty selection only when lecturer is selected
        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            facultyCombo.setDisable(!newVal.equals("Lecturer"));
            if (!newVal.equals("Lecturer")) {
                facultyCombo.getSelectionModel().clearSelection();
            }
        });

        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, signUpButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        signUpButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String role = roleCombo.getValue();
            String faculty = role != null && role.equals("Lecturer") ? facultyCombo.getValue() : null;

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || role == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "All fields are required");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (users.containsKey(username)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Username already exists");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (role.equals("Lecturer") && faculty == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Faculty is required for lecturers");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                // Create new user
                String userId = (role.equals("Student") ? "STU-" : "LEC-") + (1000 + users.size() + 1);
                users.put(username, new User(userId, username, password, fullName, role, faculty));

                // For demo, auto-enroll students in some courses
                if (role.equals("Student")) {
                    List<String> courses = new ArrayList<>();
                    courses.add("ARC101");
                    courses.add("BUS201");
                    enrolledCourses.put(username, courses);
                }

                notifications.add(new Notification(UUID.randomUUID().toString(), "New user '" + username + "' registered as " + role));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Account created successfully! Please sign in.");
                alert.setHeaderText(null);
                alert.showAndWait();
                dialog.close();
                showSignInForm();
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, usernameField, passwordField, fullNameField, roleCombo, facultyCombo, buttonBox);

        Scene dialogScene = new Scene(form, 400, 400);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private VBox createSidebar(String activePage, boolean isHome) {
        VBox sidebar = new VBox(10);
        sidebar.setStyle("-fx-background-color: #2d3e50; -fx-padding: 20;");
        sidebar.setPrefWidth(200);

        Label logo = new Label("LMS");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        logo.setTextFill(Color.WHITE);
        logo.setPadding(new Insets(0, 0, 20, 0));

        Button homeButton = new Button("Home");
        homeButton.setStyle(activePage.equals("Home") ?
                "-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;" :
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        homeButton.setMaxWidth(Double.MAX_VALUE);
        homeButton.setOnAction(e -> primaryStage.setScene(homeScene));

        Button coursesButton = new Button("Courses");
        coursesButton.setStyle(activePage.equals("Courses") ?
                "-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;" :
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        coursesButton.setMaxWidth(Double.MAX_VALUE);
        coursesButton.setOnAction(e -> primaryStage.setScene(courseScene));

        Button dashboardButton = new Button("Dashboard");
        dashboardButton.setStyle(activePage.equals("Dashboard") || activePage.equals("Lecturer Dashboard") ?
                "-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;" :
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        dashboardButton.setMaxWidth(Double.MAX_VALUE);
        dashboardButton.setOnAction(e -> {
            if (loggedInUser != null) {
                primaryStage.setScene(users.get(loggedInUser).role.equals("Lecturer") ? lecturerDashboardScene : dashboardScene);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please sign in first");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        Button profileButton = new Button("Profile");
        profileButton.setStyle(activePage.equals("Profile") ?
                "-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;" :
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        profileButton.setMaxWidth(Double.MAX_VALUE);
        profileButton.setOnAction(e -> {
            if (loggedInUser != null) {
                primaryStage.setScene(profileScene);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please sign in first");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        sidebar.getChildren().addAll(logo, homeButton, coursesButton, dashboardButton, profileButton);
        VBox.setVgrow(sidebar, Priority.ALWAYS);
        return sidebar;
    }

    private VBox createCourseCard(String courseName, String courseDisplay) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(250);

        Label title = new Label(courseDisplay);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#1a252f"));
        title.setWrapText(true);

        Button enrollButton = new Button(loggedInUser != null && enrolledCourses.getOrDefault(loggedInUser, new ArrayList<>()).contains(courseDisplay.substring(courseDisplay.lastIndexOf("(") + 1, courseDisplay.length() - 1)) ? "Enrolled" : "Enroll");
        enrollButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
        enrollButton.setDisable(loggedInUser != null && enrolledCourses.getOrDefault(loggedInUser, new ArrayList<>()).contains(courseDisplay.substring(courseDisplay.lastIndexOf("(") + 1, courseDisplay.length() - 1)));
        enrollButton.setOnAction(e -> {
            if (loggedInUser == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please sign in first");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            if (users.get(loggedInUser).role.equals("Lecturer")) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Lecturers cannot enroll in courses");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            String courseCode = courseDisplay.substring(courseDisplay.lastIndexOf("(") + 1, courseDisplay.length() - 1);
            List<String> userCourses = enrolledCourses.getOrDefault(loggedInUser, new ArrayList<>());
            if (!userCourses.contains(courseCode)) {
                userCourses.add(courseCode);
                enrolledCourses.put(loggedInUser, userCourses);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Enrolled in " + courseName);
                alert.setHeaderText(null);
                alert.showAndWait();
                refreshScenes();
                primaryStage.setScene(courseScene);
            }
        });

        card.getChildren().addAll(title, enrollButton);
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return card;
    }

    private VBox createSummaryCard(String title, String value) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#1a252f"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", 24));
        valueLabel.setTextFill(Color.web("#00c4b4"));

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private void showSubmitAssessmentDialog(Assessment assessment) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Submit Assessment: " + assessment.title);

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Submit Assessment: " + assessment.title);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        List<String> answers = new ArrayList<>();
        VBox questionBox = new VBox(10);
        ScrollPane questionScroll = new ScrollPane(questionBox);
        questionScroll.setFitToWidth(true);
        questionScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");

        if (assessment.type.equals("Multiple Choice")) {
            for (int i = 0; i < assessment.questions.size(); i++) {
                String[] parts = assessment.questions.get(i).split("\\|");
                Label questionLabel = new Label((i + 1) + ". " + parts[0]);
                questionLabel.setFont(Font.font("Arial", 14));
                questionLabel.setTextFill(Color.web("#1a252f"));
                questionLabel.setWrapText(true);
                ToggleGroup group = new ToggleGroup();
                VBox optionsBox = new VBox(5);
                for (int j = 1; j <= 4; j++) {
                    RadioButton option = new RadioButton(parts[j]);
                    option.setToggleGroup(group);
                    option.setStyle("-fx-font-size: 12;");
                    optionsBox.getChildren().add(option);
                }
                questionBox.getChildren().addAll(questionLabel, optionsBox);
                int finalI = i;
                group.selectedToggleProperty().addListener((obs, old, newToggle) -> {
                    while (answers.size() <= finalI) answers.add(null);
                    answers.set(finalI, newToggle != null ? ((RadioButton) newToggle).getText() : null);
                });
            }
        } else {
            for (int i = 0; i < assessment.questions.size(); i++) {
                Label questionLabel = new Label((i + 1) + ". " + assessment.questions.get(i));
                questionLabel.setFont(Font.font("Arial", 14));
                questionLabel.setTextFill(Color.web("#1a252f"));
                questionLabel.setWrapText(true);
                TextArea answerField = new TextArea();
                answerField.setPromptText("Enter your answer");
                answerField.setStyle("-fx-font-size: 12;");
                answerField.setPrefRowCount(3);
                int finalI = i;
                answerField.textProperty().addListener((obs, old, newText) -> {
                    while (answers.size() <= finalI) answers.add(null);
                    answers.set(finalI, newText.trim());
                });
                questionBox.getChildren().addAll(questionLabel, answerField);
            }
        }

        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, submitButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        submitButton.setOnAction(e -> {
            if (answers.stream().anyMatch(Objects::isNull)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please answer all questions");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            Map<String, List<Submission>> courseSubmissions = submissions.getOrDefault(assessment.courseCode, new HashMap<>());
            List<Submission> studentSubmissions = courseSubmissions.getOrDefault(loggedInUser, new ArrayList<>());
            studentSubmissions.add(new Submission(assessment.id, loggedInUser, answers, 0.0)); // Score to be graded later
            courseSubmissions.put(loggedInUser, studentSubmissions);
            submissions.put(assessment.courseCode, courseSubmissions);
            notifications.add(new Notification(UUID.randomUUID().toString(), "Assessment '" + assessment.title + "' submitted by " + loggedInUser));
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Assessment submitted successfully!");
            alert.setHeaderText(null);
            alert.showAndWait();
            dialog.close();
            refreshScenes();
            primaryStage.setScene(dashboardScene);
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, questionScroll, buttonBox);

        Scene dialogScene = new Scene(form, 500, 600);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void refreshScenes() {
        homeScene.setRoot(createHomeRoot());
        courseScene.setRoot(createCourseRoot());
        dashboardScene.setRoot(createDashboardRoot());
        profileScene.setRoot(createProfileRoot());
        lecturerDashboardScene.setRoot(createLecturerDashboardRoot());
    }

    private BorderPane createHomeRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Home", true);

        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #1a252f; -fx-padding: 20;");
        Label welcomeLabel = new Label("Welcome to University of Cape Town LMS");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.WHITE);
        Label subtitle = new Label("Explore our diverse range of courses across multiple faculties");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#cccccc"));
        header.getChildren().addAll(welcomeLabel, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);

        // Add login/signup buttons if not logged in
        if (loggedInUser == null) {
            HBox authButtons = new HBox(10);
            authButtons.setAlignment(Pos.CENTER);

            Button loginButton = new Button("Sign In");
            loginButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
            loginButton.setOnAction(e -> showSignInForm());

            Button signupButton = new Button("Sign Up");
            signupButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
            signupButton.setOnAction(e -> showSignUpForm());

            authButtons.getChildren().addAll(loginButton, signupButton);
            header.getChildren().add(authButtons);
        } else {
            Label userLabel = new Label("Welcome, " + users.get(loggedInUser).fullName + " (" + users.get(loggedInUser).role + ")");
            userLabel.setFont(Font.font("Arial", 14));
            userLabel.setTextFill(Color.web("#00c4b4"));
            header.getChildren().add(userLabel);
        }

        Accordion facultyAccordion = new Accordion();
        facultyAccordion.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        for (String faculty : Arrays.asList("Design", "Business", "IT", "Communication")) {
            TitledPane pane = new TitledPane();
            pane.setText("Faculty of " + faculty);
            pane.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a252f;");
            GridPane grid = createFacultyGrid(allCourses.stream()
                    .filter(c -> c.faculty.equals(faculty))
                    .map(c -> c.name + " (" + c.code + ")")
                    .collect(Collectors.toList()));
            pane.setContent(grid);
            facultyAccordion.getPanes().add(pane);
        }

        ScrollPane facultyScroll = new ScrollPane(facultyAccordion);
        facultyScroll.setFitToWidth(true);
        facultyScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");

        VBox mainContent = new VBox(10, header, facultyScroll);
        mainContent.setStyle("-fx-background-color: #ffffff;");
        mainContent.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(mainContent);
        BorderPane.setMargin(mainContent, new Insets(0, 20, 20, 20));

        return root;
    }

    private GridPane createFacultyGrid(List<String> facultyCourses) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10));
        int col = 0;
        int row = 0;
        for (String course : facultyCourses) {
            String courseName = course.substring(0, course.lastIndexOf(" ("));
            VBox courseCard = createCourseCard(courseName, course);
            grid.add(courseCard, col, row);
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
        return grid;
    }

    private BorderPane createCourseRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Courses", false);
        VBox courseContainer = new VBox(20);
        courseContainer.setPadding(new Insets(20));
        GridPane courseGrid = new GridPane();
        courseGrid.setHgap(20);
        courseGrid.setVgap(20);
        courseGrid.setPadding(new Insets(10));
        int col = 0;
        int row = 0;
        for (Course course : allCourses) {
            VBox courseCard = createCourseCard(course.name, course.name + " (" + course.code + ")");
            courseGrid.add(courseCard, col, row);
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
        courseContainer.getChildren().add(courseGrid);
        ScrollPane scrollPane = new ScrollPane(courseContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");

        VBox mainContent = new VBox(10, scrollPane);
        mainContent.setStyle("-fx-background-color: #ffffff;");

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(mainContent);
        BorderPane.setMargin(mainContent, new Insets(0, 20, 20, 20));

        return root;
    }

    private BorderPane createDashboardRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Dashboard", false);

        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #1a252f; -fx-padding: 20;");
        Label welcomeLabel = new Label("Welcome to Your Dashboard");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.WHITE);
        Label subtitle = new Label("Track your progress and manage your courses");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#cccccc"));
        header.getChildren().addAll(welcomeLabel, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(20);
        summaryGrid.setPadding(new Insets(20));
        List<String> userCourses = enrolledCourses.getOrDefault(loggedInUser, new ArrayList<>());
        summaryGrid.add(createSummaryCard("Enrolled Courses", userCourses.size() + ""), 0, 0);
        summaryGrid.add(createSummaryCard("Completed Courses", "0"), 1, 0);
        summaryGrid.add(createSummaryCard("Ongoing Courses", userCourses.size() + ""), 2, 0);

        VBox announcementsBox = new VBox(10);
        announcementsBox.setPadding(new Insets(20));
        announcementsBox.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        Label announcementsLabel = new Label("Course Announcements");
        announcementsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        announcementsLabel.setTextFill(Color.web("#1a252f"));
        VBox announcementList = new VBox(5);
        boolean hasAnnouncements = false;
        for (String courseCode : userCourses) {
            List<Announcement> courseAnnouncements = announcements.getOrDefault(courseCode, new ArrayList<>());
            if (!courseAnnouncements.isEmpty()) {
                hasAnnouncements = true;
                Course course = allCourses.stream().filter(c -> c.code.equals(courseCode)).findFirst().orElse(null);
                Label courseTitle = new Label(course != null ? course.name : courseCode);
                courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                courseTitle.setTextFill(Color.web("#1a252f"));
                announcementList.getChildren().add(courseTitle);
                for (Announcement ann : courseAnnouncements) {
                    Label annLabel = new Label("• " + ann.content + " (by " + ann.postedBy + ")");
                    annLabel.setFont(Font.font("Arial", 12));
                    annLabel.setTextFill(Color.web("#666666"));
                    annLabel.setWrapText(true);
                    announcementList.getChildren().add(annLabel);
                }
            }
        }
        if (!hasAnnouncements) {
            Label noAnnouncements = new Label("No announcements yet.");
            noAnnouncements.setFont(Font.font("Arial", 14));
            noAnnouncements.setTextFill(Color.web("#666666"));
            announcementList.getChildren().add(noAnnouncements);
        }
        ScrollPane announcementScroll = new ScrollPane(announcementList);
        announcementScroll.setFitToWidth(true);
        announcementScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        announcementsBox.getChildren().addAll(announcementsLabel, announcementScroll);

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        Label contentLabel = new Label("Course Content");
        contentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        contentLabel.setTextFill(Color.web("#1a252f"));
        VBox contentList = new VBox(5);
        boolean hasContent = false;
        for (String courseCode : userCourses) {
            List<Content> courseContents = courseContent.getOrDefault(courseCode, new ArrayList<>());
            if (!courseContents.isEmpty()) {
                hasContent = true;
                Course course = allCourses.stream().filter(c -> c.code.equals(courseCode)).findFirst().orElse(null);
                Label courseTitle = new Label(course != null ? course.name : courseCode);
                courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                courseTitle.setTextFill(Color.web("#1a252f"));
                contentList.getChildren().add(courseTitle);
                for (Content content : courseContents) {
                    Label contentLabelItem = new Label("• " + content.title + " (" + content.type + ")");
                    contentLabelItem.setFont(Font.font("Arial", 12));
                    contentLabelItem.setTextFill(Color.web("#666666"));
                    contentLabelItem.setWrapText(true);
                    contentList.getChildren().add(contentLabelItem);
                }
            }
        }
        if (!hasContent) {
            Label noContent = new Label("No content available yet.");
            noContent.setFont(Font.font("Arial", 14));
            noContent.setTextFill(Color.web("#666666"));
            contentList.getChildren().add(noContent);
        }
        ScrollPane contentScroll = new ScrollPane(contentList);
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        contentBox.getChildren().addAll(contentLabel, contentScroll);

        VBox assessmentsBox = new VBox(10);
        assessmentsBox.setPadding(new Insets(20));
        assessmentsBox.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        Label assessmentsLabel = new Label("Assessments");
        assessmentsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        assessmentsLabel.setTextFill(Color.web("#1a252f"));
        VBox assessmentList = new VBox(5);
        boolean hasAssessments = false;
        for (String courseCode : userCourses) {
            List<Assessment> courseAssessments = assessments.getOrDefault(courseCode, new ArrayList<>());
            if (!courseAssessments.isEmpty()) {
                hasAssessments = true;
                Course course = allCourses.stream().filter(c -> c.code.equals(courseCode)).findFirst().orElse(null);
                Label courseTitle = new Label(course != null ? course.name : courseCode);
                courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                courseTitle.setTextFill(Color.web("#1a252f"));
                assessmentList.getChildren().add(courseTitle);
                for (Assessment assessment : courseAssessments) {
                    VBox assessmentItem = new VBox(5);
                    Label titleLabel = new Label(assessment.title + " (" + assessment.type + ")");
                    titleLabel.setFont(Font.font("Arial", 12));
                    titleLabel.setTextFill(Color.web("#666666"));
                    Button submitButton = new Button("Submit");
                    submitButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                    final Assessment finalAssessment = assessment;
                    submitButton.setOnAction(e -> showSubmitAssessmentDialog(finalAssessment));
                    assessmentItem.getChildren().addAll(titleLabel, submitButton);
                    assessmentList.getChildren().add(assessmentItem);
                }
            }
        }
        if (!hasAssessments) {
            Label noAssessments = new Label("No assessments available yet.");
            noAssessments.setFont(Font.font("Arial", 14));
            noAssessments.setTextFill(Color.web("#666666"));
            assessmentList.getChildren().add(noAssessments);
        }
        ScrollPane assessmentScroll = new ScrollPane(assessmentList);
        assessmentScroll.setFitToWidth(true);
        assessmentScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        assessmentsBox.getChildren().addAll(assessmentsLabel, assessmentScroll);

        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(20));
        messagesBox.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        Label messagesLabel = new Label("Discussion Forum");
        messagesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        messagesLabel.setTextFill(Color.web("#1a252f"));
        VBox messageList = new VBox(5);
        boolean hasMessages = false;
        for (String courseCode : userCourses) {
            List<Message> courseMessages = messages.getOrDefault(courseCode, new ArrayList<>());
            if (!courseMessages.isEmpty()) {
                hasMessages = true;
                Course course = allCourses.stream().filter(c -> c.code.equals(courseCode)).findFirst().orElse(null);
                Label courseTitle = new Label(course != null ? course.name : courseCode);
                courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                courseTitle.setTextFill(Color.web("#1a252f"));
                messageList.getChildren().add(courseTitle);
                for (Message msg : courseMessages) {
                    Label msgLabel = new Label(msg.sender + ": " + msg.content);
                    msgLabel.setFont(Font.font("Arial", 12));
                    msgLabel.setTextFill(Color.web("#666666"));
                    msgLabel.setWrapText(true);
                    messageList.getChildren().add(msgLabel);
                }
            }
            Button postMessageButton = new Button("Post Message");
            postMessageButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
            final String finalCourseCode = courseCode;
            postMessageButton.setOnAction(e -> showPostMessageDialog(finalCourseCode));
            messageList.getChildren().add(postMessageButton);
        }
        if (!hasMessages && !userCourses.isEmpty()) {
            Label noMessages = new Label("No messages yet.");
            noMessages.setFont(Font.font("Arial", 14));
            noMessages.setTextFill(Color.web("#666666"));
            messageList.getChildren().add(noMessages);
        }
        ScrollPane messageScroll = new ScrollPane(messageList);
        messageScroll.setFitToWidth(true);
        messageScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        messagesBox.getChildren().addAll(messagesLabel, messageScroll);

        VBox mainContent = new VBox(10, header, summaryGrid, announcementsBox, contentBox, assessmentsBox, messagesBox);
        mainContent.setStyle("-fx-background-color: #ffffff;");

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(mainContent);
        BorderPane.setMargin(mainContent, new Insets(0, 20, 20, 20));

        return root;
    }

    private BorderPane createProfileRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Profile", false);

        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #1a252f; -fx-padding: 20;");
        Label welcomeLabel = new Label("Your Profile");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.WHITE);
        Label subtitle = new Label("Manage your account and enrolled courses");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#cccccc"));
        header.getChildren().addAll(welcomeLabel, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox profileDetails = new VBox(10);
        profileDetails.setPadding(new Insets(20));
        profileDetails.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        Label userIdLabel = new Label("User ID: " + (loggedInUser != null ? users.get(loggedInUser).userId : "N/A"));
        userIdLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        userIdLabel.setTextFill(Color.web("#1a252f"));
        Label usernameLabel = new Label("Username: " + (loggedInUser != null ? loggedInUser : "N/A"));
        usernameLabel.setFont(Font.font("Arial", 14));
        usernameLabel.setTextFill(Color.web("#1a252f"));
        Label fullNameLabel = new Label("Full Name: " + (loggedInUser != null ? users.get(loggedInUser).fullName : "N/A"));
        fullNameLabel.setFont(Font.font("Arial", 14));
        fullNameLabel.setTextFill(Color.web("#1a252f"));
        Label roleLabel = new Label("Role: " + (loggedInUser != null ? users.get(loggedInUser).role : "N/A"));
        roleLabel.setFont(Font.font("Arial", 14));
        roleLabel.setTextFill(Color.web("#1a252f"));
        Label facultyLabel = new Label("Faculty: " + (loggedInUser != null && users.get(loggedInUser).faculty != null ? users.get(loggedInUser).faculty : "N/A"));
        facultyLabel.setFont(Font.font("Arial", 14));
        facultyLabel.setTextFill(Color.web("#1a252f"));
        profileDetails.getChildren().addAll(userIdLabel, usernameLabel, fullNameLabel, roleLabel, facultyLabel);

        VBox enrolledCoursesBox = new VBox(10);
        enrolledCoursesBox.setPadding(new Insets(20));
        Label enrolledLabel = new Label("Enrolled Courses");
        enrolledLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        enrolledLabel.setTextFill(Color.web("#1a252f"));
        GridPane enrolledGrid = new GridPane();
        enrolledGrid.setHgap(20);
        enrolledGrid.setVgap(20);
        List<String> userCourses = enrolledCourses.getOrDefault(loggedInUser, new ArrayList<>());
        if (userCourses.isEmpty()) {
            Label noCoursesLabel = new Label("No enrolled courses yet.");
            noCoursesLabel.setFont(Font.font("Arial", 14));
            noCoursesLabel.setTextFill(Color.web("#666666"));
            enrolledGrid.add(noCoursesLabel, 0, 0);
        } else {
            int col = 0;
            int row = 0;
            for (String courseCode : userCourses) {
                Course course = allCourses.stream().filter(c -> c.code.equals(courseCode)).findFirst().orElse(null);
                if (course != null) {
                    VBox courseCard = new VBox(10);
                    courseCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
                    courseCard.setPrefWidth(250);
                    Label title = new Label(course.name + " (" + course.code + ")");
                    title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    title.setTextFill(Color.web("#1a252f"));
                    title.setWrapText(true);
                    Button unenrollButton = new Button("Unenroll");
                    unenrollButton.setStyle("-fx-background-color: #ff6f61; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                    final String finalCourseCode = courseCode;
                    unenrollButton.setOnAction(e -> {
                        enrolledCourses.get(loggedInUser).remove(finalCourseCode);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Unenrolled from " + course.name);
                        alert.setHeaderText(null);
                        alert.showAndWait();
                        refreshScenes();
                        primaryStage.setScene(profileScene);
                    });
                    courseCard.getChildren().addAll(title, unenrollButton);
                    enrolledGrid.add(courseCard, col, row);
                    col++;
                    if (col > 2) {
                        col = 0;
                        row++;
                    }
                }
            }
        }
        enrolledCoursesBox.getChildren().addAll(enrolledLabel, enrolledGrid);

        ScrollPane enrolledScrollPane = new ScrollPane(enrolledCoursesBox);
        enrolledScrollPane.setFitToWidth(true);
        enrolledScrollPane.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        enrolledScrollPane.setPrefHeight(400);

        VBox mainContent = new VBox(10, header, profileDetails, enrolledCoursesBox);
        mainContent.setStyle("-fx-background-color: #ffffff;");

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(mainContent);
        BorderPane.setMargin(mainContent, new Insets(0, 20, 20, 20));

        return root;
    }

    private BorderPane createLecturerDashboardRoot() {
        MenuBar menuBar = createMenuBar();
        VBox sidebar = createSidebar("Lecturer Dashboard", false);

        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #1a252f; -fx-padding: 20;");
        Label welcomeLabel = new Label("Lecturer Dashboard");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.WHITE);
        Label subtitle = new Label("Manage courses, content, assessments, and reports");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#cccccc"));
        header.getChildren().addAll(welcomeLabel, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        // Courses Tab
        Tab coursesTab = new Tab("Courses");
        coursesTab.setClosable(false);
        VBox courseManagementBox = new VBox(10);
        courseManagementBox.setPadding(new Insets(20));
        Label courseLabel = new Label("Course Management");
        courseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        courseLabel.setTextFill(Color.web("#1a252f"));
        Button createCourseButton = new Button("Create New Course");
        createCourseButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        createCourseButton.setOnAction(e -> showCreateCourseDialog());
        GridPane courseGrid = new GridPane();
        courseGrid.setHgap(20);
        courseGrid.setVgap(20);
        int col = 0;
        int row = 0;

        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            String lecturerFaculty = users.get(loggedInUser).faculty;
            for (Course course : allCourses.stream().filter(c -> c.faculty != null && c.faculty.equals(lecturerFaculty)).collect(Collectors.toList())) {
                VBox courseCard = new VBox(10);
                courseCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
                courseCard.setPrefWidth(250);
                Label title = new Label(course.name + " (" + course.code + ")");
                title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                title.setTextFill(Color.web("#1a252f"));
                title.setWrapText(true);
                Button editButton = new Button("Edit");
                editButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 12; -fx-border-color: #00c4b4; -fx-border-width: 1;");
                final Course finalCourse = course;
                editButton.setOnAction(e -> showEditCourseDialog(finalCourse));
                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-background-color: #ff6f61; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                deleteButton.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete " + finalCourse.name + "?");
                    confirm.setHeaderText(null);
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        allCourses.remove(finalCourse);
                        announcements.remove(finalCourse.code);
                        courseContent.remove(finalCourse.code);
                        messages.remove(finalCourse.code);
                        assessments.remove(finalCourse.code);
                        submissions.remove(finalCourse.code);
                        for (List<String> userCourses : enrolledCourses.values()) {
                            userCourses.remove(finalCourse.code);
                        }
                        notifications.add(new Notification(UUID.randomUUID().toString(), "Course '" + finalCourse.name + "' deleted by " + loggedInUser));
                        refreshScenes();
                        primaryStage.setScene(lecturerDashboardScene);
                    }
                });
                courseCard.getChildren().addAll(title, editButton, deleteButton);
                courseGrid.add(courseCard, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }
        }
        courseManagementBox.getChildren().addAll(courseLabel, createCourseButton, courseGrid);
        ScrollPane courseScroll = new ScrollPane(courseManagementBox);
        courseScroll.setFitToWidth(true);
        courseScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        coursesTab.setContent(courseScroll);

        // Announcements Tab
        Tab announcementsTab = new Tab("Announcements");
        announcementsTab.setClosable(false);
        VBox announcementsBox = new VBox(10);
        announcementsBox.setPadding(new Insets(20));
        Label announcementsLabel = new Label("Announcements");
        announcementsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        announcementsLabel.setTextFill(Color.web("#1a252f"));
        Button postAnnouncementButton = new Button("Post Announcement");
        postAnnouncementButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        postAnnouncementButton.setOnAction(e -> showPostAnnouncementDialog());
        VBox announcementList = new VBox(5);
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            String lecturerFaculty = users.get(loggedInUser).faculty;
            for (Course course : allCourses.stream().filter(c -> c.faculty != null && c.faculty.equals(lecturerFaculty)).collect(Collectors.toList())) {
                List<Announcement> courseAnnouncements = announcements.getOrDefault(course.code, new ArrayList<>());
                if (!courseAnnouncements.isEmpty()) {
                    Label courseTitle = new Label(course.name + " (" + course.code + ")");
                    courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    courseTitle.setTextFill(Color.web("#1a252f"));
                    announcementList.getChildren().add(courseTitle);
                    for (Announcement ann : courseAnnouncements) {
                        VBox annItem = new VBox(5);
                        Label annLabel = new Label("• " + ann.content);
                        annLabel.setFont(Font.font("Arial", 12));
                        annLabel.setTextFill(Color.web("#666666"));
                        annLabel.setWrapText(true);
                        Button editAnnButton = new Button("Edit");
                        editAnnButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 12; -fx-border-color: #00c4b4; -fx-border-width: 1;");
                        final Announcement finalAnn = ann;
                        editAnnButton.setOnAction(e -> showEditAnnouncementDialog(finalAnn));
                        Button deleteAnnButton = new Button("Delete");
                        deleteAnnButton.setStyle("-fx-background-color: #ff6f61; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                        deleteAnnButton.setOnAction(e -> {
                            announcements.get(finalAnn.courseCode).remove(finalAnn);
                            if (announcements.get(finalAnn.courseCode).isEmpty()) {
                                announcements.remove(finalAnn.courseCode);
                            }
                            notifications.add(new Notification(UUID.randomUUID().toString(), "Announcement deleted for '" + course.name + "' by " + loggedInUser));
                            refreshScenes();
                            primaryStage.setScene(lecturerDashboardScene);
                        });
                        annItem.getChildren().addAll(annLabel, editAnnButton, deleteAnnButton);
                        announcementList.getChildren().add(annItem);
                    }
                }
            }
        }
        ScrollPane announcementScroll = new ScrollPane(announcementList);
        announcementScroll.setFitToWidth(true);
        announcementScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        announcementsBox.getChildren().addAll(announcementsLabel, postAnnouncementButton, announcementScroll);
        announcementsTab.setContent(announcementsBox);

        // Content Tab
        Tab contentTab = new Tab("Content");
        contentTab.setClosable(false);
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(20));
        Label contentLabel = new Label("Content Management");
        contentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        contentLabel.setTextFill(Color.web("#1a252f"));
        Button uploadContentButton = new Button("Upload Content");
        uploadContentButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        uploadContentButton.setOnAction(e -> showUploadContentDialog());
        VBox contentList = new VBox(5);
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            String lecturerFaculty = users.get(loggedInUser).faculty;
            for (Course course : allCourses.stream().filter(c -> c.faculty != null && c.faculty.equals(lecturerFaculty)).collect(Collectors.toList())) {
                List<Content> courseContents = courseContent.getOrDefault(course.code, new ArrayList<>());
                if (!courseContents.isEmpty()) {
                    Label courseTitle = new Label(course.name + " (" + course.code + ")");
                    courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    courseTitle.setTextFill(Color.web("#1a252f"));
                    contentList.getChildren().add(courseTitle);
                    for (Content content : courseContents) {
                        VBox contentItem = new VBox(5);
                        Label contentLabelItem = new Label("• " + content.title + " (" + content.type + ")");
                        contentLabelItem.setFont(Font.font("Arial", 12));
                        contentLabelItem.setTextFill(Color.web("#666666"));
                        Button deleteContentButton = new Button("Delete");
                        deleteContentButton.setStyle("-fx-background-color: #ff6f61; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                        final Content finalContent = content;
                        deleteContentButton.setOnAction(e -> {
                            courseContent.get(course.code).remove(finalContent);
                            if (courseContent.get(course.code).isEmpty()) {
                                courseContent.remove(course.code);
                            }
                            notifications.add(new Notification(UUID.randomUUID().toString(), "Content '" + finalContent.title + "' deleted for '" + course.name + "' by " + loggedInUser));
                            refreshScenes();
                            primaryStage.setScene(lecturerDashboardScene);
                        });
                        contentItem.getChildren().addAll(contentLabelItem, deleteContentButton);
                        contentList.getChildren().add(contentItem);
                    }
                }
            }
        }
        ScrollPane contentScroll = new ScrollPane(contentList);
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        contentBox.getChildren().addAll(contentLabel, uploadContentButton, contentScroll);
        contentTab.setContent(contentBox);

        // Assessments Tab
        Tab assessmentsTab = new Tab("Assessments");
        assessmentsTab.setClosable(false);
        VBox assessmentsBox = new VBox(10);
        assessmentsBox.setPadding(new Insets(20));
        Label assessmentsLabel = new Label("Assessment Management");
        assessmentsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        assessmentsLabel.setTextFill(Color.web("#1a252f"));
        Button createAssessmentButton = new Button("Create Assessment");
        createAssessmentButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        createAssessmentButton.setOnAction(e -> showCreateAssessmentDialog());
        VBox assessmentList = new VBox(5);
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            String lecturerFaculty = users.get(loggedInUser).faculty;
            for (Course course : allCourses.stream().filter(c -> c.faculty != null && c.faculty.equals(lecturerFaculty)).collect(Collectors.toList())) {
                List<Assessment> courseAssessments = assessments.getOrDefault(course.code, new ArrayList<>());
                if (!courseAssessments.isEmpty()) {
                    Label courseTitle = new Label(course.name + " (" + course.code + ")");
                    courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    courseTitle.setTextFill(Color.web("#1a252f"));
                    assessmentList.getChildren().add(courseTitle);
                    for (Assessment assessment : courseAssessments) {
                        VBox assessmentItem = new VBox(5);
                        Label titleLabel = new Label(assessment.title + " (" + assessment.type + ", Weight: " + (assessment.weight * 100) + "%)");
                        titleLabel.setFont(Font.font("Arial", 12));
                        titleLabel.setTextFill(Color.web("#666666"));
                        Button editButton = new Button("Edit");
                        editButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 12; -fx-border-color: #00c4b4; -fx-border-width: 1;");
                        final Assessment finalAssessment = assessment;
                        editButton.setOnAction(e -> showEditAssessmentDialog(finalAssessment));
                        Button deleteButton = new Button("Delete");
                        deleteButton.setStyle("-fx-background-color: #ff6f61; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                        deleteButton.setOnAction(e -> {
                            assessments.get(finalAssessment.courseCode).remove(finalAssessment);
                            if (assessments.get(finalAssessment.courseCode).isEmpty()) {
                                assessments.remove(finalAssessment.courseCode);
                            }
                            submissions.getOrDefault(finalAssessment.courseCode, new HashMap<>()).values().forEach(subList ->
                                    subList.removeIf(sub -> sub.assessmentId.equals(finalAssessment.id)));
                            notifications.add(new Notification(UUID.randomUUID().toString(), "Assessment '" + finalAssessment.title + "' deleted for '" + course.name + "' by " + loggedInUser));
                            refreshScenes();
                            primaryStage.setScene(lecturerDashboardScene);
                        });
                        Button gradeButton = new Button("Grade");
                        gradeButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                        gradeButton.setOnAction(e -> showGradeAssessmentDialog(finalAssessment));
                        assessmentItem.getChildren().addAll(titleLabel, editButton, deleteButton, gradeButton);
                        assessmentList.getChildren().add(assessmentItem);
                    }
                }
            }
        }
        ScrollPane assessmentScroll = new ScrollPane(assessmentList);
        assessmentScroll.setFitToWidth(true);
        assessmentScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        assessmentsBox.getChildren().addAll(assessmentsLabel, createAssessmentButton, assessmentScroll);
        assessmentsTab.setContent(assessmentsBox);

        // Reports Tab
        Tab reportsTab = new Tab("Reports");
        reportsTab.setClosable(false);
        VBox reportsBox = new VBox(10);
        reportsBox.setPadding(new Insets(20));
        Label reportsLabel = new Label("Reports and Certifications");
        reportsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        reportsLabel.setTextFill(Color.web("#1a252f"));
        Button classPerformanceButton = new Button("Class Performance Report");
        classPerformanceButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        classPerformanceButton.setOnAction(e -> showClassPerformanceReport());
        Button transcriptButton = new Button("Student Transcripts");
        transcriptButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        transcriptButton.setOnAction(e -> showTranscriptReport());
        Button certificationButton = new Button("Issue Certifications");
        certificationButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        certificationButton.setOnAction(e -> showCertificationDialog());
        reportsBox.getChildren().addAll(reportsLabel, classPerformanceButton, transcriptButton, certificationButton);
        reportsTab.setContent(reportsBox);

        // Messages Tab
        Tab messagesTab = new Tab("Messages");
        messagesTab.setClosable(false);
        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(20));
        Label messagesLabel = new Label("Discussion Forum");
        messagesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        messagesLabel.setTextFill(Color.web("#1a252f"));
        VBox messageList = new VBox(5);
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            String lecturerFaculty = users.get(loggedInUser).faculty;
            for (Course course : allCourses.stream().filter(c -> c.faculty != null && c.faculty.equals(lecturerFaculty)).collect(Collectors.toList())) {
                List<Message> courseMessages = messages.getOrDefault(course.code, new ArrayList<>());
                if (!courseMessages.isEmpty()) {
                    Label courseTitle = new Label(course.name + " (" + course.code + ")");
                    courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    courseTitle.setTextFill(Color.web("#1a252f"));
                    messageList.getChildren().add(courseTitle);
                    for (Message msg : courseMessages) {
                        Label msgLabel = new Label(msg.sender + ": " + msg.content);
                        msgLabel.setFont(Font.font("Arial", 12));
                        msgLabel.setTextFill(Color.web("#666666"));
                        msgLabel.setWrapText(true);
                        messageList.getChildren().add(msgLabel);
                    }
                }
                Button postMessageButton = new Button("Post Message");
                postMessageButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                final String finalCourseCode = course.code;
                postMessageButton.setOnAction(e -> showPostMessageDialog(finalCourseCode));
                messageList.getChildren().add(postMessageButton);
            }
        }
        ScrollPane messageScroll = new ScrollPane(messageList);
        messageScroll.setFitToWidth(true);
        messageScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");
        messagesBox.getChildren().addAll(messagesLabel, messageScroll);
        messagesTab.setContent(messagesBox);

        tabPane.getTabs().addAll(coursesTab, announcementsTab, contentTab, assessmentsTab, messagesTab, reportsTab);

        VBox mainContent = new VBox(10, header, tabPane);
        mainContent.setStyle("-fx-background-color: #ffffff;");

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(mainContent);
        BorderPane.setMargin(mainContent, new Insets(0, 20, 20, 20));

        return root;
    }

    private void showCreateCourseDialog() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Create New Course");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Create New Course");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        TextField courseNameField = new TextField();
        courseNameField.setPromptText("Course Name");
        courseNameField.setStyle("-fx-font-size: 14;");

        TextField courseCodeField = new TextField();
        courseCodeField.setPromptText("Course Code (e.g., ARC101)");
        courseCodeField.setStyle("-fx-font-size: 14;");

        Button createButton = new Button("Create");
        createButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, createButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        createButton.setOnAction(e -> {
            String courseName = courseNameField.getText().trim();
            String courseCode = courseCodeField.getText().trim().toUpperCase();
            String faculty = users.get(loggedInUser).faculty;
            if (courseName.isEmpty() || courseCode.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Course name and code are required");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (allCourses.stream().anyMatch(c -> c.code.equals(courseCode))) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Course code already exists");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (allCourses.stream().anyMatch(c -> c.name.equals(courseName) && c.faculty.equals(faculty))) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Course name already exists in this faculty");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                allCourses.add(new Course(courseName, courseCode, faculty));
                notifications.add(new Notification(UUID.randomUUID().toString(), "New course '" + courseName + "' (" + courseCode + ") created by " + loggedInUser));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Course created successfully!");
                alert.setHeaderText(null);
                alert.showAndWait();
                dialog.close();
                refreshScenes();
                primaryStage.setScene(lecturerDashboardScene);
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, courseNameField, courseCodeField, buttonBox);

        Scene dialogScene = new Scene(form, 400, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showEditCourseDialog(Course course) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit Course");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Edit Course");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        TextField courseNameField = new TextField(course.name);
        courseNameField.setPromptText("Course Name");
        courseNameField.setStyle("-fx-font-size: 14;");

        TextField courseCodeField = new TextField(course.code);
        courseCodeField.setPromptText("Course Code");
        courseCodeField.setStyle("-fx-font-size: 14;");

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        saveButton.setOnAction(e -> {
            String newCourseName = courseNameField.getText().trim();
            String newCourseCode = courseCodeField.getText().trim().toUpperCase();
            String faculty = users.get(loggedInUser).faculty;
            if (newCourseName.isEmpty() || newCourseCode.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Course name and code are required");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (!newCourseCode.equals(course.code) && allCourses.stream().anyMatch(c -> c.code.equals(newCourseCode))) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Course code already exists");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (!newCourseName.equals(course.name) && allCourses.stream().anyMatch(c -> c.name.equals(newCourseName) && c.faculty.equals(faculty))) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Course name already exists in this faculty");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                course.name = newCourseName;
                String oldCourseCode = course.code;
                course.code = newCourseCode;
                for (List<String> userCourses : enrolledCourses.values()) {
                    if (userCourses.contains(oldCourseCode)) {
                        int index = userCourses.indexOf(oldCourseCode);
                        userCourses.set(index, newCourseCode);
                    }
                }
                updateCourseCodeInCollections(oldCourseCode, newCourseCode);
                notifications.add(new Notification(UUID.randomUUID().toString(), "Course '" + oldCourseCode + "' updated to '" + newCourseName + "' (" + newCourseCode + ") by " + loggedInUser));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Course updated successfully!");
                alert.setHeaderText(null);
                alert.showAndWait();
                dialog.close();
                refreshScenes();
                primaryStage.setScene(lecturerDashboardScene);
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, courseNameField, courseCodeField, buttonBox);

        Scene dialogScene = new Scene(form, 400, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void updateCourseCodeInCollections(String oldCode, String newCode) {
        if (announcements.containsKey(oldCode)) {
            announcements.put(newCode, announcements.remove(oldCode));
        }
        if (courseContent.containsKey(oldCode)) {
            courseContent.put(newCode, courseContent.remove(oldCode));
        }
        if (messages.containsKey(oldCode)) {
            messages.put(newCode, messages.remove(oldCode));
        }
        if (assessments.containsKey(oldCode)) {
            assessments.put(newCode, assessments.remove(oldCode));
        }
        if (submissions.containsKey(oldCode)) {
            submissions.put(newCode, submissions.remove(oldCode));
        }
    }

    private void showPostAnnouncementDialog() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Post Announcement");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Post Announcement");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        ComboBox<String> courseCombo = new ComboBox<>();
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            courseCombo.getItems().addAll(allCourses.stream()
                    .filter(c -> c.faculty != null && c.faculty.equals(users.get(loggedInUser).faculty))
                    .map(c -> c.name + " (" + c.code + ")")
                    .collect(Collectors.toList()));
        }
        courseCombo.setPromptText("Select Course");
        courseCombo.setStyle("-fx-font-size: 14;");

        TextArea announcementField = new TextArea();
        announcementField.setPromptText("Enter announcement");
        announcementField.setStyle("-fx-font-size: 14;");
        announcementField.setPrefRowCount(4);

        Button postButton = new Button("Post");
        postButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, postButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        postButton.setOnAction(e -> {
            String courseSelection = courseCombo.getValue();
            String announcement = announcementField.getText().trim();
            if (courseSelection == null || announcement.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a course and enter an announcement");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                String courseCode = courseSelection.substring(courseSelection.lastIndexOf("(") + 1, courseSelection.length() - 1);
                List<Announcement> courseAnnouncements = announcements.getOrDefault(courseCode, new ArrayList<>());
                courseAnnouncements.add(new Announcement(UUID.randomUUID().toString(), courseCode, announcement, loggedInUser));
                announcements.put(courseCode, courseAnnouncements);
                notifications.add(new Notification(UUID.randomUUID().toString(), "New announcement posted for '" + courseSelection + "' by " + loggedInUser));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Announcement posted successfully!");
                alert.setHeaderText(null);
                alert.showAndWait();
                dialog.close();
                refreshScenes();
                primaryStage.setScene(lecturerDashboardScene);
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, courseCombo, announcementField, buttonBox);

        Scene dialogScene = new Scene(form, 400, 400);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showPostMessageDialog(String courseCode) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Post Message");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Post Message");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        TextArea messageField = new TextArea();
        messageField.setPromptText("Enter your message");
        messageField.setStyle("-fx-font-size: 14;");
        messageField.setPrefRowCount(4);

        Button postButton = new Button("Post");
        postButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, postButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        postButton.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (message.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Message cannot be empty");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                List<Message> courseMessages = messages.getOrDefault(courseCode, new ArrayList<>());
                courseMessages.add(new Message(UUID.randomUUID().toString(), courseCode, loggedInUser, message));
                messages.put(courseCode, courseMessages);
                notifications.add(new Notification(UUID.randomUUID().toString(), "New message posted in '" + courseCode + "' by " + loggedInUser));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Message posted successfully!");
                alert.setHeaderText(null);
                alert.showAndWait();
                dialog.close();
                refreshScenes();
                primaryStage.setScene(users.get(loggedInUser).role.equals("Lecturer") ? lecturerDashboardScene : dashboardScene);
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, messageField, buttonBox);

        Scene dialogScene = new Scene(form, 400, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showEditAnnouncementDialog(Announcement announcement) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit Announcement");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Edit Announcement");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        TextArea announcementField = new TextArea(announcement.content);
        announcementField.setPromptText("Enter announcement");
        announcementField.setStyle("-fx-font-size: 14;");
        announcementField.setPrefRowCount(4);

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        saveButton.setOnAction(e -> {
            String newContent = announcementField.getText().trim();
            if (newContent.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Announcement cannot be empty");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                announcement.content = newContent;
                notifications.add(new Notification(UUID.randomUUID().toString(), "Announcement updated for '" + announcement.courseCode + "' by " + loggedInUser));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Announcement updated successfully!");
                alert.setHeaderText(null);
                alert.showAndWait();
                dialog.close();
                refreshScenes();
                primaryStage.setScene(lecturerDashboardScene);
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, announcementField, buttonBox);

        Scene dialogScene = new Scene(form, 400, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showUploadContentDialog() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Upload Content");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Upload Content");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        ComboBox<String> courseCombo = new ComboBox<>();
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            courseCombo.getItems().addAll(allCourses.stream()
                    .filter(c -> c.faculty != null && c.faculty.equals(users.get(loggedInUser).faculty))
                    .map(c -> c.name + " (" + c.code + ")")
                    .collect(Collectors.toList()));
        }
        courseCombo.setPromptText("Select Course");
        courseCombo.setStyle("-fx-font-size: 14;");

        TextField contentTitleField = new TextField();
        contentTitleField.setPromptText("Content Title");
        contentTitleField.setStyle("-fx-font-size: 14;");

        ComboBox<String> contentTypeCombo = new ComboBox<>();
        contentTypeCombo.getItems().addAll("Video", "PDF");
        contentTypeCombo.setPromptText("Select Content Type");
        contentTypeCombo.setStyle("-fx-font-size: 14;");

        Button uploadButton = new Button("Upload");
        uploadButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, uploadButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        uploadButton.setOnAction(e -> {
            String courseSelection = courseCombo.getValue();
            String contentTitle = contentTitleField.getText().trim();
            String contentType = contentTypeCombo.getValue();
            if (courseSelection == null || contentTitle.isEmpty() || contentType == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "All fields are required");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                String courseCode = courseSelection.substring(courseSelection.lastIndexOf("(") + 1, courseSelection.length() - 1);
                List<Content> courseContents = courseContent.getOrDefault(courseCode, new ArrayList<>());
                courseContents.add(new Content(UUID.randomUUID().toString(), contentTitle, contentType, "Sample data for " + contentTitle));
                courseContent.put(courseCode, courseContents);
                notifications.add(new Notification(UUID.randomUUID().toString(), contentType + " '" + contentTitle + "' uploaded for '" + courseSelection + "' by " + loggedInUser));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Content uploaded successfully!");
                alert.setHeaderText(null);
                alert.showAndWait();
                dialog.close();
                refreshScenes();
                primaryStage.setScene(lecturerDashboardScene);
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, courseCombo, contentTitleField, contentTypeCombo, buttonBox);

        Scene dialogScene = new Scene(form, 400, 400);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showCreateAssessmentDialog() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Create Assessment");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Create Assessment");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        ComboBox<String> courseCombo = new ComboBox<>();
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            courseCombo.getItems().addAll(allCourses.stream()
                    .filter(c -> c.faculty != null && c.faculty.equals(users.get(loggedInUser).faculty))
                    .map(c -> c.name + " (" + c.code + ")")
                    .collect(Collectors.toList()));
        }
        courseCombo.setPromptText("Select Course");
        courseCombo.setStyle("-fx-font-size: 14;");

        TextField assessmentTitleField = new TextField();
        assessmentTitleField.setPromptText("Assessment Title");
        assessmentTitleField.setStyle("-fx-font-size: 14;");

        ComboBox<String> assessmentTypeCombo = new ComboBox<>();
        assessmentTypeCombo.getItems().addAll("Multiple Choice", "Short Answer");
        assessmentTypeCombo.setPromptText("Select Assessment Type");
        assessmentTypeCombo.setStyle("-fx-font-size: 14;");

        TextField weightField = new TextField();
        weightField.setPromptText("Weight (0.0 to 1.0)");
        weightField.setStyle("-fx-font-size: 14;");

        TextArea questionsField = new TextArea();
        questionsField.setPromptText("Enter questions (one per line, for MCQ: Question|Opt1|Opt2|Opt3|Opt4|Correct)");
        questionsField.setStyle("-fx-font-size: 14;");
        questionsField.setPrefRowCount(5);

        Button createButton = new Button("Create");
        createButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, createButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        createButton.setOnAction(e -> {
            String courseSelection = courseCombo.getValue();
            String assessmentTitle = assessmentTitleField.getText().trim();
            String assessmentType = assessmentTypeCombo.getValue();
            String weightText = weightField.getText().trim();
            String questionsText = questionsField.getText().trim();

            if (courseSelection == null || assessmentTitle.isEmpty() || assessmentType == null || weightText.isEmpty() || questionsText.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "All fields are required");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightText);
                if (weight < 0.0 || weight > 1.0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Weight must be between 0.0 and 1.0");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid weight format");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            List<String> questions = Arrays.asList(questionsText.split("\n"));
            if (assessmentType.equals("Multiple Choice")) {
                for (String q : questions) {
                    if (q.split("\\|").length != 6) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "MCQ format: Question|Opt1|Opt2|Opt3|Opt4|Correct");
                        alert.setHeaderText(null);
                        alert.showAndWait();
                        return;
                    }
                }
            }

            String courseCode = courseSelection.substring(courseSelection.lastIndexOf("(") + 1, courseSelection.length() - 1);
            List<Assessment> courseAssessments = assessments.getOrDefault(courseCode, new ArrayList<>());
            courseAssessments.add(new Assessment(UUID.randomUUID().toString(), courseCode, assessmentTitle, assessmentType, weight, questions, loggedInUser));
            assessments.put(courseCode, courseAssessments);
            notifications.add(new Notification(UUID.randomUUID().toString(), "Assessment '" + assessmentTitle + "' created for '" + courseSelection + "' by " + loggedInUser));
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Assessment created successfully!");
            alert.setHeaderText(null);
            alert.showAndWait();
            dialog.close();
            refreshScenes();
            primaryStage.setScene(lecturerDashboardScene);
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, courseCombo, assessmentTitleField, assessmentTypeCombo, weightField, questionsField, buttonBox);

        Scene dialogScene = new Scene(form, 500, 500);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showEditAssessmentDialog(Assessment assessment) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit Assessment");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Edit Assessment");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        TextField assessmentTitleField = new TextField(assessment.title);
        assessmentTitleField.setPromptText("Assessment Title");
        assessmentTitleField.setStyle("-fx-font-size: 14;");

        TextField weightField = new TextField(String.valueOf(assessment.weight));
        weightField.setPromptText("Weight (0.0 to 1.0)");
        weightField.setStyle("-fx-font-size: 14;");

        TextArea questionsField = new TextArea(String.join("\n", assessment.questions));
        questionsField.setPromptText("Enter questions (one per line, for MCQ: Question|Opt1|Opt2|Opt3|Opt4|Correct)");
        questionsField.setStyle("-fx-font-size: 14;");
        questionsField.setPrefRowCount(5);

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        saveButton.setOnAction(e -> {
            String newTitle = assessmentTitleField.getText().trim();
            String weightText = weightField.getText().trim();
            String questionsText = questionsField.getText().trim();

            if (newTitle.isEmpty() || weightText.isEmpty() || questionsText.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "All fields are required");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightText);
                if (weight < 0.0 || weight > 1.0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Weight must be between 0.0 and 1.0");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid weight format");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            List<String> questions = Arrays.asList(questionsText.split("\n"));
            if (assessment.type.equals("Multiple Choice")) {
                for (String q : questions) {
                    if (q.split("\\|").length != 6) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "MCQ format: Question|Opt1|Opt2|Opt3|Opt4|Correct");
                        alert.setHeaderText(null);
                        alert.showAndWait();
                        return;
                    }
                }
            }

            assessment.title = newTitle;
            assessment.weight = weight;
            assessment.questions = questions;
            notifications.add(new Notification(UUID.randomUUID().toString(), "Assessment '" + newTitle + "' updated for '" + assessment.courseCode + "' by " + loggedInUser));
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Assessment updated successfully!");
            alert.setHeaderText(null);
            alert.showAndWait();
            dialog.close();
            refreshScenes();
            primaryStage.setScene(lecturerDashboardScene);
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, assessmentTitleField, weightField, questionsField, buttonBox);

        Scene dialogScene = new Scene(form, 500, 500);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showGradeAssessmentDialog(Assessment assessment) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Grade Assessment: " + assessment.title);

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");

        Label title = new Label("Grade Assessment: " + assessment.title);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        VBox submissionBox = new VBox(10);
        ScrollPane submissionScroll = new ScrollPane(submissionBox);
        submissionScroll.setFitToWidth(true);
        submissionScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");

        Map<String, List<Submission>> courseSubmissions = submissions.getOrDefault(assessment.courseCode, new HashMap<>());
        if (courseSubmissions.isEmpty()) {
            Label noSubmissions = new Label("No submissions available.");
            noSubmissions.setFont(Font.font("Arial", 14));
            noSubmissions.setTextFill(Color.web("#666666"));
            submissionBox.getChildren().add(noSubmissions);
        } else {
            for (Map.Entry<String, List<Submission>> entry : courseSubmissions.entrySet()) {
                String student = entry.getKey();
                List<Submission> studentSubmissions = entry.getValue();
                for (Submission submission : studentSubmissions) {
                    if (submission.assessmentId.equals(assessment.id)) {
                        VBox submissionItem = new VBox(5);
                        Label studentLabel = new Label("Student: " + student);
                        studentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                        studentLabel.setTextFill(Color.web("#1a252f"));
                        Label answersLabel = new Label("Answers: " + String.join("; ", submission.answers));
                        answersLabel.setFont(Font.font("Arial", 12));
                        answersLabel.setTextFill(Color.web("#666666"));
                        answersLabel.setWrapText(true);
                        TextField scoreField = new TextField(String.valueOf(submission.score));
                        scoreField.setPromptText("Enter score (0-100)");
                        scoreField.setStyle("-fx-font-size: 14;");
                        Button saveScoreButton = new Button("Save Score");
                        saveScoreButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10;");
                        saveScoreButton.setOnAction(e -> {
                            try {
                                double score = Double.parseDouble(scoreField.getText().trim());
                                if (score < 0 || score > 100) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "Score must be between 0 and 100");
                                    alert.setHeaderText(null);
                                    alert.showAndWait();
                                } else {
                                    submission.score = score;
                                    notifications.add(new Notification(UUID.randomUUID().toString(), "Assessment '" + assessment.title + "' graded for '" + student + "' by " + loggedInUser));
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Score saved successfully!");
                                    alert.setHeaderText(null);
                                    alert.showAndWait();
                                    refreshScenes();
                                }
                            } catch (NumberFormatException ex) {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid score format");
                                alert.setHeaderText(null);
                                alert.showAndWait();
                            }
                        });
                        submissionItem.getChildren().addAll(studentLabel, answersLabel, scoreField, saveScoreButton);
                        submissionBox.getChildren().add(submissionItem);
                    }
                }
            }
        }

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        closeButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, submissionScroll, closeButton);

        Scene dialogScene = new Scene(form, 600, 600);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showClassPerformanceReport() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Class Performance Report");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");

        Label title = new Label("Class Performance Report");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        ComboBox<String> courseCombo = new ComboBox<>();
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            courseCombo.getItems().addAll(allCourses.stream()
                    .filter(c -> c.faculty != null && c.faculty.equals(users.get(loggedInUser).faculty))
                    .map(c -> c.name + " (" + c.code + ")")
                    .collect(Collectors.toList()));
        }
        courseCombo.setPromptText("Select Course");
        courseCombo.setStyle("-fx-font-size: 14;");

        VBox reportBox = new VBox(10);
        ScrollPane reportScroll = new ScrollPane(reportBox);
        reportScroll.setFitToWidth(true);
        reportScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");

        courseCombo.setOnAction(e -> {
            reportBox.getChildren().clear();
            String courseSelection = courseCombo.getValue();
            if (courseSelection != null) {
                String courseCode = courseSelection.substring(courseSelection.lastIndexOf("(") + 1, courseSelection.length() - 1);
                Map<String, List<Submission>> courseSubmissions = submissions.getOrDefault(courseCode, new HashMap<>());
                if (courseSubmissions.isEmpty()) {
                    Label noData = new Label("No submissions available for this course.");
                    noData.setFont(Font.font("Arial", 14));
                    noData.setTextFill(Color.web("#666666"));
                    reportBox.getChildren().add(noData);
                } else {
                    for (Map.Entry<String, List<Submission>> entry : courseSubmissions.entrySet()) {
                        String student = entry.getKey();
                        List<Submission> studentSubmissions = entry.getValue();
                        double averageScore = studentSubmissions.stream()
                                .mapToDouble(s -> s.score)
                                .average()
                                .orElse(0.0);
                        Label studentLabel = new Label("Student: " + student + " - Average Score: " + String.format("%.2f", averageScore));
                        studentLabel.setFont(Font.font("Arial", 12));
                        studentLabel.setTextFill(Color.web("#666666"));
                        reportBox.getChildren().add(studentLabel);
                    }
                }
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        closeButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, courseCombo, reportScroll, closeButton);

        Scene dialogScene = new Scene(form, 600, 600);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showTranscriptReport() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Student Transcripts");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");

        Label title = new Label("Student Transcripts");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        TextField studentUsernameField = new TextField();
        studentUsernameField.setPromptText("Enter Student Username");
        studentUsernameField.setStyle("-fx-font-size: 14;");

        VBox transcriptBox = new VBox(10);
        ScrollPane transcriptScroll = new ScrollPane(transcriptBox);
        transcriptScroll.setFitToWidth(true);
        transcriptScroll.setStyle("-fx-background: #ffffff; -fx-border-color: #ffffff;");

        studentUsernameField.textProperty().addListener((obs, old, newValue) -> {
            transcriptBox.getChildren().clear();
            String studentUsername = newValue.trim();
            if (!studentUsername.isEmpty() && users.containsKey(studentUsername) && users.get(studentUsername).role.equals("Student")) {
                List<String> studentCourses = enrolledCourses.getOrDefault(studentUsername, new ArrayList<>());
                if (studentCourses.isEmpty()) {
                    Label noData = new Label("No enrolled courses for this student.");
                    noData.setFont(Font.font("Arial", 14));
                    noData.setTextFill(Color.web("#666666"));
                    transcriptBox.getChildren().add(noData);
                } else {
                    for (String courseCode : studentCourses) {
                        Course course = allCourses.stream().filter(c -> c.code.equals(courseCode)).findFirst().orElse(null);
                        if (course != null) {
                            Label courseLabel = new Label(course.name + " (" + course.code + ")");
                            courseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                            courseLabel.setTextFill(Color.web("#1a252f"));
                            transcriptBox.getChildren().add(courseLabel);
                            List<Submission> studentSubmissions = submissions.getOrDefault(courseCode, new HashMap<>())
                                    .getOrDefault(studentUsername, new ArrayList<>());
                            if (studentSubmissions.isEmpty()) {
                                Label noSubmissions = new Label("No submissions.");
                                noSubmissions.setFont(Font.font("Arial", 12));
                                noSubmissions.setTextFill(Color.web("#666666"));
                                transcriptBox.getChildren().add(noSubmissions);
                            } else {
                                for (Submission submission : studentSubmissions) {
                                    Assessment assessment = assessments.getOrDefault(courseCode, new ArrayList<>())
                                            .stream()
                                            .filter(a -> a.id.equals(submission.assessmentId))
                                            .findFirst()
                                            .orElse(null);
                                    if (assessment != null) {
                                        Label submissionLabel = new Label(assessment.title + ": " + submission.score);
                                        submissionLabel.setFont(Font.font("Arial", 12));
                                        submissionLabel.setTextFill(Color.web("#666666"));
                                        transcriptBox.getChildren().add(submissionLabel);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (!studentUsername.isEmpty()) {
                Label invalid = new Label("Invalid student username.");
                invalid.setFont(Font.font("Arial", 14));
                invalid.setTextFill(Color.web("#666666"));
                transcriptBox.getChildren().add(invalid);
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        closeButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, studentUsernameField, transcriptScroll, closeButton);

        Scene dialogScene = new Scene(form, 600, 600);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showCertificationDialog() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Issue Certifications");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f4f6f8;");

        Label title = new Label("Issue Certifications");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1a252f"));

        ComboBox<String> courseCombo = new ComboBox<>();
        if (loggedInUser != null && users.get(loggedInUser) != null && users.get(loggedInUser).faculty != null) {
            courseCombo.getItems().addAll(allCourses.stream()
                    .filter(c -> c.faculty != null && c.faculty.equals(users.get(loggedInUser).faculty))
                    .map(c -> c.name + " (" + c.code + ")")
                    .collect(Collectors.toList()));
        }
        courseCombo.setPromptText("Select Course");
        courseCombo.setStyle("-fx-font-size: 14;");

        TextField studentUsernameField = new TextField();
        studentUsernameField.setPromptText("Enter Student Username");
        studentUsernameField.setStyle("-fx-font-size: 14;");

        Button issueButton = new Button("Issue Certificate");
        issueButton.setStyle("-fx-background-color: #00c4b4; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #00c4b4; -fx-font-size: 14; -fx-border-color: #00c4b4; -fx-border-width: 1;");
        HBox buttonBox = new HBox(10, issueButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        issueButton.setOnAction(e -> {
            String courseSelection = courseCombo.getValue();
            String studentUsername = studentUsernameField.getText().trim();
            if (courseSelection == null || studentUsername.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a course and enter a student username");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else if (!users.containsKey(studentUsername) || !users.get(studentUsername).role.equals("Student")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid student username");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                String courseCode = courseSelection.substring(courseSelection.lastIndexOf("(") + 1, courseSelection.length() - 1);
                if (!enrolledCourses.getOrDefault(studentUsername, new ArrayList<>()).contains(courseCode)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Student is not enrolled in this course");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                } else {
                    notifications.add(new Notification(UUID.randomUUID().toString(), "Certificate issued to '" + studentUsername + "' for '" + courseSelection + "' by " + loggedInUser));
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Certificate issued successfully!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    dialog.close();
                }
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        form.getChildren().addAll(title, courseCombo, studentUsernameField, buttonBox);

        Scene dialogScene = new Scene(form, 400, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}