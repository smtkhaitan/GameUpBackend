select *  from dbo.tournament_booked join dbo.users on dbo.tournament_booked.user1_email = dbo.users.email where email <> '@{email}' and paired = 0;