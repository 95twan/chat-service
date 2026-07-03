create table if not exists message (
    message_sequence bigint not null,
    channel_id bigint not null,
    sender_user_id bigint not null,
    content varchar(1000) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    primary key (channel_id, message_sequence)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;
