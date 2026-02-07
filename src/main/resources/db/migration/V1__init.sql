create table carts (
  id bigserial primary key,
  user_id text not null,
  version bigint not null default 0,
  constraint uq_carts_user_id unique (user_id)
);

create table cart_items (
  id bigserial primary key,
  cart_id bigint not null,
  sku text not null,
  quantity int not null,
  constraint fk_cart_items_cart_id foreign key (cart_id) references carts(id) on delete cascade,
  constraint uq_cart_items_cart_id_sku unique (cart_id, sku)
);
