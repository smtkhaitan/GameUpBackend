INSERT INTO tournament_booked (user1_email, game_type,game_time, lat_long, paired)
VALUES
  (
    '@{user_email}', '@{game_type}' ,'@{game_time}', '@{lat_long}' , '@{paired}'
  );
