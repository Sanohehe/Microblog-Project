--SQL Queries used in project:

--Query that selects all the data from the posts and follow tables where the userId is not the current logged
--in user, and the posts are from users that the current logged in user follows.
--Found in HomeController and corresponds to http://localhost:8081/
select * from post,follow where userId != ? AND followerUserId = ? AND followeeUserId = userId ORDER BY postDate DESC

--Query that selects the user data where the userId equals a certain id, usually gotten from a previous query
--and this query is used to fetch the first and last name attributes required to make a user object to 
--assign to a post, comment, heart, or bookmark.
--Found in HomeController and corresponds to http://localhost:8081/
--Found in PostController and corresponds to http://localhost:8081/post/{postId}
--Found in ProfileController and corresponds to http://localhost:8081/profile/{userId}
select * from user where userId = ?

--Query that finds the amount of comments a certain post with the specified postId has.
--Found in HomeController and corresponds to http://localhost:8081/
select COUNT(commentId) from comment where postId = ?

--Query that finds the amount of hearts on a certain post with the specified postId.
--Found in HomeController and corresponds to http://localhost:8081/
--Found in PostController and corresponds to http://localhost:8081/post/{postId}
select * from heart where postId = ?

--Query that finds if the specified user has left a heart on the specified post.
--Found in HomeController and corresponds to http://localhost:8081/
--Found in PostController and corresponds to http://localhost:8081/post/{postId}
select * from heart where userId = ? AND postId = ?

--Query that inserts data into the post table after a user creates a post. The values
--to insert are the logged in user, the current date, and the content the user has typed in
--the form.
--Found in HomeController and corresponds to http://localhost:8081/createpost
INSERT INTO post (userId, postDate, postText) VALUES (?, ?, ?)

--To be written
INSERT INTO hashtag (hashTag, postId) VALUES (?, ?)

--Query to find user that have not made any posts yet. This needs to be done because
--not having any posts is a special case when creating the people page
--Found in PeopleService and corresponds to http://localhost:8081/people
select * from user where user.userId not in (select post.userId from post)

--Query that checks if the logged in user currently follows another user in question
--whose userId is plugged into this query.
--Found in PeopleService and corresponds to http://localhost:8081/people
select followerUserId from follow where followeeUserId = ?

--Query that finds all users that are not the current user
--Found in PeopleService and corresponds to http://localhost:8081/people
select * from user where userID != ?

--Query that finds the most recent postDate of a user, by searching through
--the posts of the user and selecting the MAX postDate.
--Found in PeopleService and corresponds to http://localhost:8081/people
select MAX(postDate) as postDate from post group by userId having userId = ?

--Query that finds if the specified user follows any another user
--Found in PeopleService and corresponds to http://localhost:8081/people
select followeeUserId from follow where followerUserId = ?

--Query to find userId, postDate, and postText of post given its postId
--Found in PostController and corresponds to http://localhost:8081/post/{postId}
select * from post where postId = ?

--Query to find all the comments of a specified post and list them from
--most recent to last
--Found in PostController and corresponds to http://localhost:8081/post/{postId}
--Found in ProfileController and corresponds to http://localhost:8081/profile/{userId}
select * from comment where comment.postId = ? ORDER BY commentDate DESC

--Query that adds into the comment table, the values from the postId and the logged
--in user making the comments
--Found in PostController and corresponds to http://localhost:8081/post/{postId}/comment
insert into comment (postId, userId, commentDate, commentText) values (?,?,?,?)

--Query to remove a heart record from the table if the current user un-hearts a 
--message.
--Found in PostController and corresponds to http://localhost:8081/post/{postId}/heart/false
--Found in ProfileController and corresponds to http://localhost:8081/profile/{userId}
delete from heart where postId = ? AND userId = ?

--Query that inserts the record of a user hearting a post into the heart table
--Found in PostController and corresponds to http://localhost:8081/post/{postId}/heart/true
--Found in ProfileController and corresponds to http://localhost:8081/profile/{userId}
insert into heart (postId, userId) values (?,?)

--Query to find all the posts made by a specific user and list them from most recent
--to last.
--Found in ProfileController and corresponds to http://localhost:8081/profile/{userId}
select * from post where userId = ? ORDER BY postDate DESC

--Query to select all the data from follow to then iterate through to find if the user
--is already following the selected user.
--Found in PeopleController and corresponds to http://localhost:8081/people/{userID}/follow/{isFollow}
select * from follow

--Query to delete the record from the follow table if the user decides to unfollow another user
--Found in PeopleController and corresponds to http://localhost:8081/people/{userID}/follow/{isFollow}
delete from follow where followerUserId = ? AND followeeUserId = ?

--Query that updates the record in the follow table to show that a user has started following
--another user
--Found in PeopleController and corresponds to http://localhost:8081/people/{userID}/follow/true
insert ignore into follow (followerUserId, followeeUserId) values (?, ?)

--Query that finds the post information of posts that the user has 
select post.postId, post.userId, postDate, postText from post,bookmark where bookmark.userId = ?  AND bookmark.postId = post.postId


