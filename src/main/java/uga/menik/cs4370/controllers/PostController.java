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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.BasicPost;
import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;

/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/post")
public class PostController {

    private final DataSource dataSource;

    private final UserService userService;

    @Autowired
    public PostController(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }
    /**
     * This function handles the /post/{postId} URL.
     * This handlers serves the web page for a specific post.
     * Note there is a path variable {postId}.
     * An example URL handled by this function looks like below:
     * http://localhost:8081/post/1
     * The above URL assigns 1 to postId.
     * 
     * See notes from HomeController.java regardig error URL parameter.
     * @throws SQLException 
     */
    @GetMapping("/{postId}")
    public ModelAndView webpage(@PathVariable("postId") String postId,
            @RequestParam(name = "error", required = false) String error) throws SQLException {
        System.out.println("The user is attempting to view post with id: " + postId);
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");
        
        List<Post> posts = new ArrayList<>();
        // Following line populates sample data.
        // You should replace it with actual data from the database.
        
        final String sql2 = "select * from post where postId = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                    pstmt.setString(1, postId);
                    
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                    String viewingPostId = rs.getString("postId");
                    
                    
                    String postDate = rs.getString("postDate");
                    String postText = rs.getString("postText");
                    String userId = rs.getString("userId");
                    final String usersql = "select * from user where userId = ?";
                    try (Connection conn2 = dataSource.getConnection();
                PreparedStatement pstmt2 = conn2.prepareStatement(usersql)) {
                    pstmt2.setString(1, userId);
                    ResultSet rsUser = pstmt2.executeQuery();
                    while (rsUser.next()) {
                    String fName = rsUser.getString("firstName");
                    String lName = rsUser.getString("lastName");
                    User x = new User(userId, fName, lName);
                    System.out.println("testing.....");
                    final String commentSql = "select * from comment where comment.postId = ? ORDER BY commentDate DESC";
                try (Connection connComment = dataSource.getConnection();
                PreparedStatement commentStmt = connComment.prepareStatement(commentSql)) {
                    commentStmt.setString(1, postId);
                    ResultSet commentSet = commentStmt.executeQuery();
                    int commentCount = 0;
                    List<Comment> commentList = new ArrayList<>();
                    while(commentSet.next()) {
                        commentCount++;
                        String content = commentSet.getString("commentText");
                        String date = commentSet.getString("commentDate");
                        String commentUserId = commentSet.getString("userID");
                        
                    try (Connection conn3 = dataSource.getConnection();
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
            Post newPost = new ExpandedPost(viewingPostId, postText, postDate, x, 0, commentCount, false, false, commentList);
                    
            posts.add(newPost);
                }
                    }

            }
                    }
                    
                    
                }
                mv.addObject("posts", posts);
        

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
     * Handles comments added on posts.
     * See comments on webpage function to see how path variables work here.
     * This function handles form posts.
     * See comments in HomeController.java regarding form submissions.
     * @throws SQLException 
     */
    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
            @RequestParam(name = "comment") String comment) throws SQLException {
        System.out.println("The user is attempting add a comment:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tcomment: " + comment);

        LocalDateTime l = java.time.LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");
        String parsedDate = l.format(formatter);
        final String commentSql = "insert into comment (postId, userId, commentDate, commentText) values (?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement commentStmt = conn.prepareStatement(commentSql)) {
            commentStmt.setString(1, postId);
            commentStmt.setString(2, userService.getLoggedInUser().getUserId());
            commentStmt.setString(3, parsedDate);
            commentStmt.setString(4, comment);

            // Execute the statement and check if rows are affected.
            int rowsAffected = commentStmt.executeUpdate();
            
        }

        // Redirect the user if the comment adding is a success.
        return "redirect:/post/" + postId;

        // Redirect the user with an error message if there was an error.
        //String message = URLEncoder.encode("Failed to post the comment. Please try again.",
               // StandardCharsets.UTF_8);
        //return "redirect:/post/" + postId + "?error=" + message;
    }

    /**
     * Handles likes added on posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions and how path variables work.
     */
    @GetMapping("/{postId}/heart/{isAdd}")
    public String addOrRemoveHeart(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a heart:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        // Redirect the user if the comment adding is a success.
        // return "redirect:/post/" + postId;

        // Redirect the user with an error message if there was an error.
        String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
                StandardCharsets.UTF_8);
        return "redirect:/post/" + postId + "?error=" + message;
    }

    /**
     * Handles bookmarking posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions.
     */
    @GetMapping("/{postId}/bookmark/{isAdd}")
    public String addOrRemoveBookmark(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a bookmark:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        // Redirect the user if the comment adding is a success.
        // return "redirect:/post/" + postId;

        // Redirect the user with an error message if there was an error.
        String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
                StandardCharsets.UTF_8);
        return "redirect:/post/" + postId + "?error=" + message;
    }

}
