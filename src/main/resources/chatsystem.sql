create table if not exists chat_user (
    user_id bigint auto_increment,
    username varchar(20) not null,
    password varchar(255) not null,
    invite_code varchar(32) not null,
    connection_count int not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    primary key (user_id),
    constraint unique_username unique (username),
    constraint unique_invite_code unique (invite_code)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;

create table if not exists user_connection (
    partner_a_user_id bigint not null,
    partner_b_user_id bigint not null,
    status varchar(20) not null,
    inviter_user_id bigint not null,
    created_at timestamp not null,
    updated_at timestamp not null,

    primary key (partner_a_user_id, partner_b_user_id),
    index idx_partner_b_user_id (partner_b_user_id),
    index idx_partner_a_user_id_status (partner_a_user_id, status),
    index idx_partner_b_user_id_status (partner_b_user_id, status),
    index idx_partner_a_b_user_id_status (partner_a_user_id, partner_b_user_id, status)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;

create table if not exists channel (
    channel_id bigint auto_increment,
    title varchar(30) not null,
    invite_code varchar(32) not null,
    head_count int not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    primary key (channel_id),
    constraint unique_invite_code unique (invite_code)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;

create table if not exists user_channel (
    user_id bigint not null,
    channel_id bigint not null,
    last_read_message_seq bigint not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    primary key (user_id, channel_id),
    index idx_channel_id (channel_id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;
