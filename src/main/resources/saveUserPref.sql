INSERT INTO dbo.UserPreference (user_id, cluster_tag,answers)
VALUES
  (
    '@{user_id}', '@{cluster_tag}' ,'@{answers}'
  );
