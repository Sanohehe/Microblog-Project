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
--NOTE: the passwords for the users are as follows:
--bbarnes30: glass888
--J_jackson: password123
--J_jameson: password456
--S_Sinai: password789
--D_Awesome: password000
--NOTE: INSERT THE TABLES STARTING FROM POST (at the bottom)
--bookmark

--comment
insert into comment (commentId, postId, userId, commentDate, commentText) values 
(1, 14, 1, "Mar 23, 2024, 08:14 PM", 
"this is my first test comment"),
 (2, 11, 1, "Mar 23, 2024, 09:13 PM", 
"Hey Jill! I aslo love cats"),
 (3, 14, 1, "Mar 23, 2024, 09:15 PM", 
"this is my second test comment, at a different time"),
 (4, 14, 1, "Mar 23, 2024, 09:17 PM", 
"this is a third comment!"),
 (5, 13, 1, "Mar 24, 2024, 12:20 AM", 
"Hey!");

--follow
insert into follow (followerUserId, followeeUserId) values 
(2, 1),
 (4, 1),
 (1, 2),
 (4, 2),
 (1, 3),
 (2, 3),
 (4, 3),
 (1, 4),
 (2, 4),
 (1, 5);

--hashtag
insert into hashtag (hashTag, postId) values 
 ("cats", 15),
 ("dogs", 15),
 ("test", 15),
 ("cats", 10),
 ("dogs", 10),
 ("cats", 11),
 ("books", 11),
 ("books", 12),
 ("dogs", 12);


--heart
insert into heart (postId, userID) values 
(11, 1),
(13, 1),
(14, 1),
(11, 4),
(14, 4);


--post
insert into post (postId, userId, postDate, postText) values
(9,1,"Mar 20, 2024, 06:42 PM", "Hi! I am Brandon #test"),
(10,2,"Mar 20, 2024, 06:42 PM", "Hi! I am James i love #cats and #dogs"),
(11,3,"Mar 20, 2024, 06:43 PM", "Hi! I am Jill. i love #cats and #books"),
(12,4,"Mar 20, 2024, 06:43 PM", "Hi! I am Sam! i love #books and #dogs"),
(13,1,"Mar 20, 2024, 06:50 PM", "This is a second post from me, at a different time! #test"),
(14,2,"Mar 20, 2024, 07:39 PM", "This is a second post by me, James. #test"),
(15,1,"Mar 24, 2024, 01:41 PM", "#cats #test #dogs");

--user
INSERT INTO `user` (`userId`, `username`, `password`, `firstName`, `lastName`) 
VALUES (1,'bbarnes30','$2a$10$Iyks/I4lxFQh5Je4vuk.J.yoXOkRnpFt/a5WImm2FtDaW0NLHCWv6','Brandon','Barnes'),
(2,'J_Jackson','$2a$10$vLcO0Q2XDvvinnMrym3YpOF9F8wGo.3J.ul5b3UG8eIzOig739SMW','John ','Jackson'),
(3,'J_Jameson','$2a$10$jXF2.0/tw5xybTarM16r/uULh.AnQW4yudQ1fiaVtjEM1.cj9gET6','Jill','Jameson'),
(4,'S_Sin','$2a$10$bWCPrWNrhnlAlXQ6Jojt2eNfcYlaYLi8vFWFdbuHaGCvE/YGk3ebm','Sam','Sinai'),
(5,'D_Awesome','$2a$10$pGqwLOp2RZXjC/hPs5dVwu4gZ/QSAJKbwluDDlisUm7kmskdGiTSa','Dude','Awesome');