/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;

import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles /hashtagsearch URL and possibly others.
 * At this point no other URLs.
 */
@Controller
@RequestMapping("/hashtagsearch")
public class HashtagSearchController {

    @Autowired
    private DataSource dataSource;

    /**
     * This function handles the /hashtagsearch URL itself.
     * This URL can process a request parameter with name hashtags.
     * In the browser the URL will look something like below:
     * http://localhost:8081/hashtagsearch?hashtags=%23amazing+%23fireworks
     * Note: the value of the hashtags is URL encoded.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "hashtags") String hashtags) {
        System.out.println("User is searching: " + hashtags);

        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

        // Gets hashtags into individuals
        String decodedHashtags = URLDecoder.decode(hashtags, StandardCharsets.UTF_8);
        String[] hashtagArray = decodedHashtags.split("\\s+");

        // Prepare SQL query to select posts that have all the requested hashtags
        String sql = "SELECT p.*, COUNT(DISTINCT h.hashTag) as tagCount " +
                "FROM post p " +
                "JOIN hashtag h ON p.postId = h.postId " +
                "WHERE h.hashTag IN (%s) " +
                "GROUP BY p.postId " +
                "HAVING COUNT(DISTINCT h.hashTag) = ?";

        // Prepare the IN clause with the correct number of placeholders
        String inSql = String.join(",", Collections.nCopies(hashtagArray.length, "?"));

        sql = String.format(sql, inSql);

        List<Post> posts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the IN clause values
            for (int i = 0; i < hashtagArray.length; i++) {
                pstmt.setString(i + 1, hashtagArray[i].replace("#", ""));
            }
            // Set the count for the HAVING clause
            pstmt.setInt(hashtagArray.length + 1, hashtagArray.length);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Extract post data from the result set
                String postId = rs.getString("postId");
                String content = rs.getString("postText"); // Assuming the column is named 'postText'
                String postDate = rs.getString("postDate");
                String userId = rs.getString("userId");
                // Fetch the user data from the database or cache, if needed
                User user = fetchUserById(userId); // Implement fetchUserById according to your application logic

                // Initialize heart count, comment count, and flags. You will need to adjust
                // according to actual query.
                int heartsCount = 0; // Replace with the actual heart count if available in the result set
                int commentsCount = 0; // Replace with the actual comment count if available in the result set
                boolean isHearted = false; // Determine if the post is hearted by the current user
                boolean isBookmarked = false; // Determine if the post is bookmarked by the current user

                // Create a new Post object using the extracted data
                Post post = new Post(postId, content, postDate, user, heartsCount, commentsCount, isHearted,
                        isBookmarked);

                // Add the newly created Post object to the list
                posts.add(post);
            }
            mv.addObject("posts", posts);
        } catch (SQLException e) {
            e.printStackTrace();
            mv.addObject("errorMessage", "An error occurred while searching for hashtags.");
        }

        if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        }

        return mv;
    }

    private User fetchUserById(String userId) throws SQLException {
        final String userSql = "SELECT * FROM user WHERE userId = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(userSql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                String id = rs.getString("userId");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                return new User(id, firstName, lastName);
            } else {
                // Handle the case where no user is found
                return null; 
            }
        }
    }

}
