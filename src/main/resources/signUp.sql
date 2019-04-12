INSERT INTO dbo.users (NAME, email,PASSWORD, dob, age_grp, gender, phone, indoor, outdoor)
VALUES
  (
    '@{NAME}', '@{email}' ,'@{PASSWORD}', '@{dob}' ,'@{age_grp}', '@{gender}' ,'@{phone}', '@{indoor}' ,'@{outdoor}'
  );
