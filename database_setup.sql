-- Create the database.
create database if not exists cs4370_mb_platform;

-- Use the created database.
use cs4370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

create table if not exists post (
    postId int auto_increment,
    userId int not null,
    postDate varchar(255) not null,
    postText varchar(255) not null,
    primary key(postId),
    foreign key(userID) references user(userId)
    
);

create table if not exists comment (
    commentId int auto_increment,
    postId int not null,
    userId int not null,
    commentDate varchar(255) not null,
    commentText varchar(255) not null,
    primary key (commentId),
    foreign key(userId) references user(userId),
    foreign key(postId) references post(postId)

);


create table if not exists heart (
    postId int not null,
    userId int not null,
    primary key(postId, userId),
    foreign key(userId) references user(userId),
    foreign key(postId) references post(postId)
);

create table if not exists bookmark (
    postId int not null,
    userId int not null,
    primary key(postId, userId),
    foreign key(userId) references user(userId),
    foreign key(postId) references post(postId)
);

create table if not exists hashtag (
    hashTag varchar(255),
    postId int not null,
    primary key(hashTag, postId),
    foreign key(postId) references post(postId)
);

create table if not exists follow (
    followerUserId int,
    followeeUserId int,
    primary key(followerUserId, followeeUserId),
    foreign key(followerUserId) references user(userId),
    foreign key(followeeUSerId) references user(userId)
);

--insert sample data

--bookmark

--comment
insert into comment (commentId, postId, userId, commentDate, commentText) values (1, 14, 1, "Mar 23, 2024, 08:14 PM", 
"this is my first test comment")
insert into comment (commentId, postId, userId, commentDate, commentText) values (2, 11, 1, "Mar 23, 2024, 09:13 PM", 
"Hey Jill! I aslo love cats")
insert into comment (commentId, postId, userId, commentDate, commentText) values (3, 14, 1, "Mar 23, 2024, 09:15 PM", 
"this is my second test comment, at a different time")
insert into comment (commentId, postId, userId, commentDate, commentText) values (4, 14, 1, "Mar 23, 2024, 09:17 PM", 
"this is a third comment!")
insert into comment (commentId, postId, userId, commentDate, commentText) values (5, 13, 1, "Mar 24, 2024, 12:20 AM", 
"Hey!")

--follow
insert into follow (followerUserId, followeeUserId) values (2, 1)
insert into follow (followerUserId, followeeUserId) values (4, 1)
insert into follow (followerUserId, followeeUserId) values (1, 2)
insert into follow (followerUserId, followeeUserId) values (4, 2)
insert into follow (followerUserId, followeeUserId) values (1, 3)
insert into follow (followerUserId, followeeUserId) values (2, 3)
insert into follow (followerUserId, followeeUserId) values (4, 3)
insert into follow (followerUserId, followeeUserId) values (1, 4)
insert into follow (followerUserId, followeeUserId) values (2, 4)
insert into follow (followerUserId, followeeUserId) values (1, 5)

--hashtag

--heart
insert into heart (postId, userID) values (11, 1)
insert into heart (postId, userID) values (13, 1)
insert into heart (postId, userID) values (14, 1)
insert into heart (postId, userID) values (11, 4)
insert into heart (postId, userID) values (14, 4)

--post
