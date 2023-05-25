insert into user_details(id,name,password)
values(1001,'user1','demo');
insert into user_details(id,name,password)
values(1002,'user2','demo');
insert into user_details(id,name,password)
values(1003,'user3','demo');

insert into task(id,status,target_date,user_id,description,title)
values (2001,true,current_date(),1001,'this is task 2001','task 2001');

insert into task(id,status,target_date,user_id,description,title)
values (2002,true,current_date(),1001,'this is task 2001','task 2001');

insert into task(id,status,target_date,user_id,description,title)
values (2003,true,current_date(),1002,'this is task 2001','task 2001');

insert into task(id,status,target_date,user_id,description,title)
values (2004,true,current_date(),1002,'this is task 2001','task 2001');