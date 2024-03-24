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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;

/**
 * Handles /bookmarks and its sub URLs.
 * No other URLs at this point.
 * 
 * Learn more about @Controller here: 
 * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    private final DataSource dataSource;

    private final UserService userService;
    
    @Autowired
    public BookmarksController(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }
    /**
     * /bookmarks URL itself is handled by this.
     * @throws SQLException 
     */
    @GetMapping
    public ModelAndView webpage() throws SQLException {
        // posts_page is a mustache template from src/main/resources/templates.
        // ModelAndView class enables initializing one and populating placeholders
        // in the template using Java objects assigned to named properties.
        ModelAndView mv = new ModelAndView("posts_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        List<Post> posts = new ArrayList<>();
        
        final String sql = "select post.postId, post.userId, postDate, postText from post,bookmark where bookmark.userId = ?  AND bookmark.postId = post.postId";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, userService.getLoggedInUser().getUserId());
                    ResultSet rs = pstmt.executeQuery();
                    while(rs.next()) {
                        String postId = rs.getString("post.postId");
                        String userId = rs.getString("post.userId");
                        String postDate = rs.getString("postDate");
                        String postText = rs.getString("postText");
                        String fName ="";
                    String lName ="";
                    final String usersql = "select * from user where userId = ?";
                    //second sql query that identifies the user whose made the bookmarked post.
                    try (Connection conn2 = dataSource.getConnection();
                PreparedStatement pstmt2 = conn2.prepareStatement(usersql)) {
                    pstmt2.setString(1, userId);
                    ResultSet rsUser = pstmt2.executeQuery();
                    while (rsUser.next()) {
                         fName = rsUser.getString("firstName");
                          lName = rsUser.getString("lastName");
                    }
                }         int commentCount = 0;
                        List<Comment> commentList = new ArrayList<>();
                          //finds all the comments related to the posts on the user's profile
                          final String commentSql = "select * from comment where comment.postId = ? ORDER BY commentDate DESC";
                          try (Connection connComment = dataSource.getConnection();
                          PreparedStatement commentStmt = connComment.prepareStatement(commentSql)) {
                              commentStmt.setString(1, postId);
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
                        Comment commentX = new Comment(postId, content, date, userX);
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
                        heartStmt.setString(1, postId);
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
                        heartUserStmt.setString(2, postId);
                        ResultSet heartSet = heartUserStmt.executeQuery();
                        while (heartSet.next()) {
                        isHeart = true;
                        
                    }
    
                    }



                    User userX = new User(userId, fName, lName);
                    Post x = new ExpandedPost(postId, postText, postDate, userX, heartCount, commentCount, isHeart, true, commentList);
                    posts.add(x);   
                    }

                    }

                    mv.addObject("posts", posts);
            

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


        
    

