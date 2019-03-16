INSERT INTO dbo.userPref (user_id, cluster_tag,answers)
VALUES
  (
    '@{user_id}', '@{cluster_tag}' ,'@{answers}'
  );
