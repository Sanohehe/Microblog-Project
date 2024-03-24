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
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.ExpandedPost;
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
                    System.out.println("postId:" + viewingPostId);
                    postDate = rs.getString("postDate");
                    postText = rs.getString("postText");
                    String fName ="";
                    String lName ="";
                    final String usersql = "select * from user where userId = ?";
                    //second sql query that identifies the user whose profile is clicked on.
                    try (Connection conn2 = dataSource.getConnection();
                PreparedStatement pstmt2 = conn2.prepareStatement(usersql)) {
                    pstmt2.setString(1, userId);
                    ResultSet rsUser = pstmt2.executeQuery();
                    while (rsUser.next()) {
                         fName = rsUser.getString("firstName");
                          lName = rsUser.getString("lastName");
                          int commentCount = 0;
                          List<Comment> commentList = new ArrayList<>();
                          //finds all the comments related to the posts on the user's profile
                          final String commentSql = "select * from comment where comment.postId = ? ORDER BY commentDate DESC";
                          try (Connection connComment = dataSource.getConnection();
                          PreparedStatement commentStmt = connComment.prepareStatement(commentSql)) {
                              commentStmt.setString(1, viewingPostId);
                              ResultSet commentSet = commentStmt.executeQuery();
                              while(commentSet.next()) {
                                  commentCount++;
                                  String content = commentSet.getString("commentText");
                                  String date = commentSet.getString("commentDate");
                                  String commentUserId = commentSet.getString("userID");
                                  try (Connection conn3 = dataSource.getConnection();
                                  //Query to find the user who left the comments
                PreparedStatement pstmt3 = conn3.prepareStatement(usersql)) {
                    pstmt3.setString(1, commentUserId);
                    ResultSet rsCommentUser = pstmt3.executeQuery();
                    while (rsCommentUser.next()) {
                        String commentFirstName = rsCommentUser.getString("firstName");
                        String commentLastName = rsCommentUser.getString("lastName");
                        User userX = new User(commentUserId,commentFirstName, commentLastName);
                        Comment commentX = new Comment(viewingPostId, content, date, userX);
                        commentList.add(commentX); 
                    }
                }
            }
                }
                Boolean isHeart = false;
                int heartCount = 0;
                //Query to find how many hearts are on a post
                final String heartSql = "select * from heart where postId = ?";
                try (Connection connHeart = dataSource.getConnection();
                    PreparedStatement heartStmt = connHeart.prepareStatement(heartSql)) {
                        heartStmt.setString(1, viewingPostId);
                        ResultSet heartSet = heartStmt.executeQuery();
                        while (heartSet.next()) {
                        heartCount++;
                        
                    }
    
                    }
                    //Query to find if the logged in user has hearted the post
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
                    User userX = new User(userId, fName, lName);
                    Post x = new ExpandedPost(viewingPostId, postText, postDate, userX, heartCount, commentCount, isHeart, false, commentList);
                    posts.add(x);   
                    }
            }
            }
                    mv.addObject("posts", posts);
                    
                }
                    
                

        

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        //String errorMessage = "Some error occured!";
        //mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        //mv.addObject("isNoContent", true);
        
        
        return mv;
    }
    
    
}
