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
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.FollowableUser;
import uga.menik.cs4370.services.PeopleService;
import uga.menik.cs4370.services.UserService;


/**
 * Handles /people URL and its sub URL paths.
 */
@Controller
@RequestMapping("/people")
public class PeopleController {

    // Inject UserService and PeopleService instances.
    // See LoginController.java to see how to do this.
    // Hint: Add a constructor with @Autowired annotation.
    private final UserService userService;
    private final PeopleService peopleService;
    private final DataSource dataSource;

    @Autowired
    public PeopleController(UserService userservice, PeopleService peopleservice, DataSource dataSource) {
        this.userService = userservice;
        this.peopleService = peopleservice;
        this.dataSource = dataSource;
    }

    /**
     * Serves the /people web page.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     * @throws SQLException 
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) throws SQLException {
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("people_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        // Use the PeopleService instance to find followable users.
        // Use UserService to access logged in userId to exclude.
        uga.menik.cs4370.models.User x;
        x = userService.getLoggedInUser();
        String id= x.getUserId();


        List<FollowableUser> followableUsers = peopleService.getFollowableUsers(id);
        mv.addObject("users", followableUsers);
        
        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        if (followableUsers.isEmpty()) {
        mv.addObject("isNoContent", true);
        }
        
        return mv;
    }

    /**
     * This function handles user follow and unfollow.
     * Note the URL has parameters defined as variables ie: {userId} and {isFollow}.
     * Follow and unfollow is handled by submitting a get type form to this URL 
     * by specifing the userId and the isFollow variables.
     * Learn more here: https://www.w3schools.com/tags/att_form_method.asp
     * An example URL that is handled by this function looks like below:
     * http://localhost:8081/people/1/follow/false
     * The above URL assigns 1 to userId and false to isFollow.
     * @throws SQLException 
     */
    @GetMapping("{userId}/follow/{isFollow}")
    public String followUnfollowUser(@PathVariable("userId") String userId,
            @PathVariable("isFollow") Boolean isFollow) throws SQLException {
        
        System.out.println("User is attempting to follow/unfollow a user:");
        System.out.println("\tuserId: " + userId);
        System.out.println("\tisFollow: " + isFollow);
        

        //checks if the user is already following the selected user
        final String checkFollowing = "select * from follow";
        try (Connection conn2 = dataSource.getConnection();
                PreparedStatement pstmt2 = conn2.prepareStatement(checkFollowing)) {
                    ResultSet rs = pstmt2.executeQuery();
                    while(rs.next()) {
                        String followerUserId = rs.getString("followerUserId");
                        String followeeUserId = rs.getString("followeeUSerId");
                        if (followerUserId.equals(userService.getLoggedInUser().getUserId()) && 
                        followeeUserId.equals(userId)) {
                            //if the user is already following the selected user, then we change the operation
                            //to an unfollowing, by deleting the row in the follow table that describes the 
                            //following relationship between the two users.
                            final String deleteFollowing = "delete from follow where followerUserId = ? AND followeeUserId = ?";
                            try (Connection conn3 = dataSource.getConnection();
                                PreparedStatement deletingPstmt = conn3.prepareStatement(deleteFollowing)) {
                                    deletingPstmt.setString(1, userService.getLoggedInUser().getUserId());
                                    deletingPstmt.setString(2, userId);
                                    deletingPstmt.executeUpdate();
                        }
                        catch (SQLException e) {
                            String message = URLEncoder.encode("Failed to (un)follow the user. Please try again.",
                            StandardCharsets.UTF_8);
                    return "redirect:/people?error=" + message;
                        }
                    }
                }
        //if the users do not have a previous follow relationship in the follow table,
        //then we can insert it here through this sql statement.
        if (isFollow == true) {
        final String followerSql = "insert ignore into follow (followerUserId, followeeUserId) values (?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement followerStmt = conn.prepareStatement(followerSql)) {
                followerStmt.setString(1, userService.getLoggedInUser().getUserId());
                followerStmt.setString(2, userId);
                followerStmt.executeUpdate();
        }
        catch (SQLException e) {
            String message = URLEncoder.encode("Failed to (un)follow the user. Please try again.",
                StandardCharsets.UTF_8);
        return "redirect:/people?error=" + message;
        }
    }
        

        // Redirect the user if the comment adding is a success.
        
        return "redirect:/people";

        
    }

}
}
