/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;

/**
 * Handles /profile URL and its sub URLs.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    // UserService has user login and registration related functions.
    private final UserService userService;

    private final DataSource dataSource;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public ProfileController(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    /**
     * This function handles /profile URL itself.
     * This serves the webpage that shows posts of the logged in user.
     * @throws SQLException 
     */
    @GetMapping
    public ModelAndView profileOfLoggedInUser() throws SQLException {
        System.out.println("User is attempting to view profile of the logged in user.");
        return profileOfSpecificUser(userService.getLoggedInUser().getUserId());
    }

    /**
     * This function handles /profile/{userId} URL.
     * This serves the webpage that shows posts of a speific user given by userId.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * how path variables work.
     * @throws SQLException 
     */
    @GetMapping("/{userId}")
    public ModelAndView profileOfSpecificUser(@PathVariable("userId") String userId) throws SQLException {
        System.out.println("User is attempting to view profile: " + userId);
        
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        
        
         final String sql2 = "select * from post where userId = ? ORDER BY postDate DESC";
        //first sql query that grabs the data from the post table.
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                    String viewingPostId = "";
                    String postText ="";
                    String postDate ="";
                    pstmt.setString(1, userId);
                    List<Post> posts = new ArrayList<>();
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                    viewingPostId = rs.getString("postId");
                    postDate = rs.getString("postDate");
                    postText = rs.getString("postText");
                    
                    String fName ="";
                    String lName ="";
                    final String usersql = "select * from user where userId = ?";
                    //second sql query that identifies the user whose profile is clicked on.
                    System.out.println("Attempting to identify user..");
                    try (Connection conn2 = dataSource.getConnection();
                PreparedStatement pstmt2 = conn2.prepareStatement(usersql)) {
                    pstmt2.setString(1, userId);
                    ResultSet rsUser = pstmt2.executeQuery();
                    while (rsUser.next()) {
                         fName = rsUser.getString("firstName");
                          lName = rsUser.getString("lastName");
                    
                
                    User userX = new User(userId, fName, lName);
                    Post x = new Post(viewingPostId, postText, postDate, userX, 0, 0, false, false);
                    posts.add(x);
                    }
            }
            }
                    mv.addObject("posts", posts);
                }
                    
                

        

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // String errorMessage = "Some error occured!";
        // mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);
        
        return mv;
    }
    
    
}