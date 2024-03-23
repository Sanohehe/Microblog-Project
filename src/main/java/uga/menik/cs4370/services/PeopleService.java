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
import uga.menik.cs4370.utility.Utility;

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
                    String postQuery = "select MAX(postDate) as postDate from post group by userId having userId = ?;";
        try (Connection conn2 = dataSource.getConnection();
                PreparedStatement pstmt2 = conn.prepareStatement(postQuery)) {
                    pstmt2.setString(1, rs.getString("userId"));
                    ResultSet rs2 = pstmt2.executeQuery();
                    while (rs2.next()) {

                        followableUsers.add(new FollowableUser(userId, firstName,
                        lastName, false, rs2.getString("postDate") ));
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
