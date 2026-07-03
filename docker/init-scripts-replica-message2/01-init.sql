change replication source to
       source_host='mysql-source-message2',
       source_user='replica_user',
       source_password='replica_password',
       source_auto_position=1;
start replica;
