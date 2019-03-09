INSERT INTO dbo.tournament_booked (user1_email, game_type,game_time, lat_long)
VALUES
  (
    '@{user_email}', '@{game_type}' ,'@{game_time}', '@{lat_long}'
  );
