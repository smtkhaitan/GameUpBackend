update dbo.tournament_booked set paired = 1
where user1_email = '@{user_email}' and game_type = '@{game_type}' and game_time = '@{game_time}' and paired = 0;

