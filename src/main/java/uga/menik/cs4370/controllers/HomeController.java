/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.PeopleService;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;

/**
 * This controller handles the home page and some of it's sub URLs.
 */
@Controller
@RequestMapping
public class HomeController {

    private final UserService userService;

    private final DataSource dataSource;

 @Autowired
    public HomeController(UserService userservice, DataSource dataSource) {
        this.userService = userservice;
        this.dataSource = dataSource;
       
    }


    /**
     * This is the specific function that handles the root URL itself.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     * @throws SQLException 
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) throws SQLException {
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("home_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        final String sql2 = "select * from post,follow where userId != ? AND followerUserId = ? AND followeeUserId = userId ORDER BY postDate DESC";
        //first sql query that grabs the data from the post table.
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                    String userId = userService.getLoggedInUser().getUserId();
                    String viewingPostId = "";
                    String postText ="";
                    String postDate ="";
                    pstmt.setString(1, userId);
                    pstmt.setString(2, userId);
                    List<Post> posts = new ArrayList<>();
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                    viewingPostId = rs.getString("postId");
                    postDate = rs.getString("postDate");
                    postText = rs.getString("postText");
                    String postUser = rs.getString("userId");
                    
                    String fName ="";
                    String lName ="";
                    final String usersql = "select * from user where userId = ?";
                    try (Connection conn2 = dataSource.getConnection();
                PreparedStatement pstmt2 = conn2.prepareStatement(usersql)) {
                    pstmt2.setString(1, postUser);
                    ResultSet rsUser = pstmt2.executeQuery();
                    while (rsUser.next()) {
                         fName = rsUser.getString("firstName");
                          lName = rsUser.getString("lastName");
                    
                
                    User userX = new User(postUser, fName, lName);
                    Post x = new Post(viewingPostId, postText, postDate, userX, 0, 0, false, false);
                    posts.add(x);
                    }
            }
            }
        
        //sql query that grabs the data from the post table.
        
        mv.addObject("posts", posts);
                }

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);

        return mv;
                
            
    }

    /**
     * This function handles the /createpost URL.
     * This handles a post request that is going to be a form submission.
     * The form for this can be found in the home page. The form has a
     * input field with name = posttext. Note that the @RequestParam
     * annotation has the same name. This makes it possible to access the value
     * from the input from the form after it is submitted.
     * @throws SQLException 
     */
    @PostMapping("/createpost")
    public String createPost(@RequestParam(name = "posttext") String postText) throws SQLException {
        LocalDateTime l = java.time.LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");
        String parsedDate = l.format(formatter);
        System.out.println("User is creating post: " + postText);
        Post x = new Post("1", postText, parsedDate, userService.getLoggedInUser(), 10, 4, false, false);
        addPost(x.getUser().getUserId(), postText, x.getPostDate());
        // Redirect the user if the post creation is a success.
        return "redirect:/";
        

        // Redirect the user with an error message if there was an error.
        //String message = URLEncoder.encode("Failed to create the post. Please try again.",
                //StandardCharsets.UTF_8);
        //return "redirect:/?error=" + message;
    }

    public boolean addPost(String userID, String postText, String date) throws SQLException {
        final String registerSql = "insert into post (userId, postDate, postText) values (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement registerStmt = conn.prepareStatement(registerSql)) {
            registerStmt.setString(1, userID);
            registerStmt.setString(2, date);
            registerStmt.setString(3, postText);

            // Execute the statement and check if rows are affected.
            int rowsAffected = registerStmt.executeUpdate();
            return rowsAffected > 0;
        }
        
    }
}
