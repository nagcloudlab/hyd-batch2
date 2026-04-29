

create table users (
    id serial primary key,
    username varchar(255) not null unique,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    active boolean default true
);

create table roles (
    id serial primary key,
    name varchar(255) not null unique,
    description text
);

insert into roles (name, description) values
('admin', 'Administrator with full access'),
('user', 'Regular user with limited access');

create table user_roles (
    user_id int references users(id) on delete cascade,
    role_id int references roles(id) on delete cascade,
    primary key (user_id, role_id)
);

create table todos (
    id serial primary key,
    title varchar(255) not null,
    description text,
    completed boolean default false,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    user_id int references users(id) on delete cascade
);