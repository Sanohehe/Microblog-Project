/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import uga.menik.cs4370.models.FollowableUser;


/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {

    private final DataSource dataSource;
    /**
     * This function should query and return all users that 
     * are followable. The list should not contain the user 
     * with id userIdToExclude.
     * @throws SQLException 
     */
   
     @Autowired
     public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
     }
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) throws SQLException {
        // Write an SQL query to find the users that are not the current user.
       DataSource dataSource = this.dataSource;
       List<FollowableUser> followableUsers = new ArrayList<>();
       Boolean followStatus = false;
       //query to find users 
       String noPostQuery = "select * from user where user.userId not in (select post.userId from post)";
                    try (Connection conn3 = dataSource.getConnection();
                PreparedStatement pstmt3 = conn3.prepareStatement(noPostQuery)) {
                    Boolean followStatusNoPosts = false;
                        ResultSet rs3 = pstmt3.executeQuery();
                          while(rs3.next()){
                            String userId = rs3.getString("userId");
                            String fName = rs3.getString("firstName");
                            String lName = rs3.getString("lastName");
                            //Query to find if the logged in user follows another user
                            final String followQuery = "select followerUserId from follow where followeeUserId = ?";
                        try (Connection conn4 = dataSource.getConnection();
                PreparedStatement pstmt4 = conn4.prepareStatement(followQuery)) {
                        pstmt4.setString(1, userId);
                        ResultSet followStatsRs = pstmt4.executeQuery();
                        while(followStatsRs.next()) {
                            if (followStatsRs.getString("followerUserID").equals(userIdToExclude)) {
                                followStatusNoPosts = true;
                            }
                        }
                    }
                            followableUsers.add(new FollowableUser(userId, fName, lName, followStatusNoPosts,"No Posts Yet"));
                }
                        
                }
                //query to find the users that are not the current user
        String query = "select * from user where userID != ?";
        String userId ="";
        String firstName ="";
        String lastName ="";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, userIdToExclude);       
            try (ResultSet rs = pstmt.executeQuery()) {
                // Traverse the result rows one at a time.
                // Note: This specific while loop will only run at most once 
                // since username is unique.
                while (rs.next()) {
                    userId = rs.getString("userId");
                    firstName = rs.getString("firstName");
                    lastName = rs.getString("lastName");
                //finds the most recent post time if the user has posted.
                    String postQuery = "select MAX(postDate) as postDate from post group by userId having userId = ?;";
        try (Connection conn2 = dataSource.getConnection();
                PreparedStatement pstmt2 = conn2.prepareStatement(postQuery)) {
                    pstmt2.setString(1, rs.getString("userId"));
                    ResultSet rs2 = pstmt2.executeQuery();
                    while (rs2.next()) {
                        //Query to find if the user already follows another user
                        final String followQuery = "select followeeUserId from follow where followerUserId = ?";
                        try (Connection conn3 = dataSource.getConnection();
                PreparedStatement pstmt3 = conn3.prepareStatement(followQuery)) {
                        pstmt3.setString(1, userIdToExclude);
                        ResultSet followStatsRs = pstmt3.executeQuery();
                        followStatus = false;
                        while(followStatsRs.next()) {
                            if (followStatsRs.getString("followeeUserID").equals(userId)) {
                                followStatus = true;
                            }
                        }
                }
                        //add the users into the following list
                        followableUsers.add(new FollowableUser(userId, firstName,
                        lastName, followStatus, rs2.getString("postDate") ));
                    }

                }
            }
        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.

        // Use the query result to create a list of followable users.
        // See UserService.java to see how to access rows and their attributes
        // from the query result.
        // Check the following createSampleFollowableUserList function to see 
        // how to create a list of FollowableUsers.

        // Replace the following line and return the list you created.
        return followableUsers;
    }

}
    }
}
