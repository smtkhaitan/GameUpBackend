CREATE TABLE users (
user_id INT NOT NULL IDENTITY(1,1),
NAME VARCHAR(100),
email VARCHAR(100),
PASSWORD VARCHAR(20),
dob VARCHAR(100),
age_grp VARCHAR(20),
gender CHAR(1),
phone VARCHAR(10),
indoor VARCHAR(1000),
outdoor VARCHAR(1000)
)

CREATE TABLE tournament_paired (
user1_email VARCHAR(100),
user2_email VARCHAR(100),
game_type VARCHAR(100),
game_time VARCHAR(100)
)

CREATE TABLE tournament_booked (
user1_email VARCHAR(100),
game_type VARCHAR(100),
game_time VARCHAR(100),
lat_long VARCHAR(1000),
paired int default 0,
)