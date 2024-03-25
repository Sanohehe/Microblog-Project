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
import java.sql.Statement;
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
import uga.menik.cs4370.services.UserService;

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
        List<Post> posts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                    String userId = userService.getLoggedInUser().getUserId();
                    String viewingPostId = "";
                    String postText ="";
                    String postDate ="";
                    pstmt.setString(1, userId);
                    pstmt.setString(2, userId);
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
                    String commentCount = "";
                    int comments = 0;
                    final String commentCountSql = "select COUNT(commentId) from comment where postId = ?";
                    try (Connection conn3 = dataSource.getConnection();
                PreparedStatement pstmt3 = conn3.prepareStatement(commentCountSql)) {
                    pstmt3.setString(1, viewingPostId);
                    ResultSet rsComment = pstmt3.executeQuery();
                    while(rsComment.next()) {
                    commentCount = rsComment.getString("COUNT(commentID)");
                    comments = Integer.valueOf(commentCount);
                    }


                }
                Boolean isHeart = false;
                int heartCount = 0;
                Boolean isBookmarked = false;
                final String heartSql = "select * from heart where postId = ?";
            try (Connection connHeart = dataSource.getConnection();
                PreparedStatement heartStmt = connHeart.prepareStatement(heartSql)) {
                    heartStmt.setString(1, viewingPostId);
                    ResultSet heartSet = heartStmt.executeQuery();
                    while (heartSet.next()) {
                    heartCount++;
                    
                    }

                }
                final String heartUserSql = "select * from heart where userId = ? AND postId = ?";
                try (Connection connHeartUser = dataSource.getConnection();
                PreparedStatement heartUserStmt = connHeartUser.prepareStatement(heartUserSql)) {
                    heartUserStmt.setString(1, userService.getLoggedInUser().getUserId());
                    heartUserStmt.setString(2, viewingPostId);
                    ResultSet heartSet = heartUserStmt.executeQuery();
                    while (heartSet.next()) {
                    isHeart = true;
                    
                }

                }
                //Query to find if the post is bookmarked by the current user
                final String bookmarkUserSql = "select * from bookmark where userId = ? AND postId = ?";
                try (Connection connBookUser = dataSource.getConnection();
                PreparedStatement bookUserStmt = connBookUser.prepareStatement(bookmarkUserSql)) {
                    bookUserStmt.setString(1, userService.getLoggedInUser().getUserId());
                    bookUserStmt.setString(2, viewingPostId);
                    ResultSet bookSet = bookUserStmt.executeQuery();
                    while (bookSet.next()) {
                    isBookmarked = true;
                    
                }

                }
                    User userX = new User(postUser, fName, lName);
                    Post x = new Post(viewingPostId, postText, postDate, userX, heartCount, comments, isHeart, isBookmarked);
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
        if (posts.isEmpty()) {
        mv.addObject("isNoContent", true);
        }

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

        // Extract user ID from the logged-in user
        String userId = userService.getLoggedInUser().getUserId();

        // Add post to database and get post ID
        int postId = addPost(userId, postText, parsedDate);

        if (postId > 0) {
            // If the post was added, extract hashtags
            List<String> hashtags = extractHashtags(postText);
            addHashtags(postId, hashtags);
            return "redirect:/";
        } else {
            // Error case
            String message = URLEncoder.encode("Failed to create the post. Please try again.", StandardCharsets.UTF_8);
            return "redirect:/?error=" + message;
        }

    }

    private int addPost(String userID, String postText, String date) throws SQLException {
        final String insertPostSql = "INSERT INTO post (userId, postDate, postText) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement insertPostStmt = conn.prepareStatement(insertPostSql, Statement.RETURN_GENERATED_KEYS)) {
            insertPostStmt.setString(1, userID);
            insertPostStmt.setString(2, date);
            insertPostStmt.setString(3, postText);
            int rowsAffected = insertPostStmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = insertPostStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the generated post ID
                    }
                }
            }
        }
        return -1; // Return an invalid ID if insertion failed
    }


    private void addHashtags(int postId, List<String> hashtags) throws SQLException {
        final String insertHashtagSql = "INSERT INTO hashtag (hashTag, postId) VALUES (?, ?)";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement insertHashtagStmt = conn.prepareStatement(insertHashtagSql)) {
            for (String hashtag : hashtags) {
                insertHashtagStmt.setString(1, hashtag);
                insertHashtagStmt.setInt(2, postId);
                insertHashtagStmt.executeUpdate();
            }
        }
    }

    private List<String> extractHashtags(String content) {
        List<String> hashtags = new ArrayList<>();
        // Split the content by spaces
        String[] words = content.split("\\s+");
        // Iterate over words to find hashtags
        for (String word : words) {
            // If a word starts with a hashtag, add it to the list
            if (word.startsWith("#") && word.length() > 1) {
                hashtags.add(word.substring(1)); // Remove the '#' when adding
            }
        }
        return hashtags;
    }

}
