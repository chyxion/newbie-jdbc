#Newbie JDBC
**Newbie JDBC** is a simple JDBC tool, It supports array parameters expanding, connection sharing, transaction, pagination and so on.

## Usage

### Add Maven Dependency
```xml
<dependency>
    <groupId>me.chyxion</groupId>
    <artifactId>newbie-jdbc</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

### Use In Code
#### Create Newbie JDBC Object
```java
// init datasource, here use DruidDataSource as demo
DruidDataSource datasource = null;
datasource = new DruidDataSource();
datasource.setUrl("jdbc:mysql://127.0.0.1/demo");
datasource.setUsername("root");
datasource.setPassword("password");
datasource.init();

// create NewbieJdbc object
NewbieJdbc jdbc = new NewbieJdbcSupport(datasource);

```

#### Basic Query
```java
// count of users
int count = jdbc.findValue(
    "select count(1) from users");
    
// find name of user id is 2008110101
String name = jdbc.findValue(
	"select name from users where id = ?", 
	"2008110101");

// find names of user id is 101 or 102
// 0. array as params
List<String> names = jdbc.listValue(
	"select name from users where id in (?)", 
	"101", "102");
	
// 1. collection as params
names = jdbc.listValue(
    "select name from users where id in (?)", 
	Arrays.asList("101", "102"));
	
// 2. map as params
Map<String, Object> params = 
    new HashMap<String, Object>();
params.put("id", Arrays.asList("101", "102"));
// or: 
// params.put("id", new String[] {"101", "102"});
names = jdbc.listValue(
	"select name from users where id in (:id)", 
	params);
	
// find user of id is 101
Map<String, Object> mapUser = jdbc.findMap(
	"select id, name, gender from users where id = ?", "101");

// list users of age is 24
List<Map<String, Object>> listUsers = jdbc.listMap(
	"select id, name, gender from users where age = ?", 24);

```
### Advance Query
```java
// find id and name as a string array
String[] idAndName = jdbc.findOne(new Ro<String[]>() {
        public String[] exec(ResultSet rs) throws SQLException {
            return new String[] {
                rs.getString("id"), 
                rs.getString("name")};
        }
	},
	"select id, name from users where id = ?", 
	"101");
	
// find names of gender is M
String names = jdbc.list(new Ro<String>() {
        public String exec(ResultSet rs) throws SQLException {
            return rs.getString("name");
        }
    }, 
    "select name from users where gender = ?", 
    "M");

// find name of user id is 101, same as findValue
String name = jdbc.query(new Ro<String>() {
	public String exec(ResultSet rs) throws SQLException {
			return rs.next() ? rs.getString(1) : null;
		}
	}, 
	"select name from users where id = ?", 
	"101");
	
// list users of gender F offset 10 limit 16
List<Map<String, Object>> users =
	jdbc.listMapPage(
	   "select * from users where gender = ?", 
		Arrays.asList(new Order("date_created", Order.DESC)), 
		10, 16, "F");

```
### Insert And Update
```java
// insert one
Map<String, Object> mapUser = new HashMap<String, Object>();
mapUser.put("id", "103");
mapUser.put("name", "Shaun Chyxion");
mapUser.put("gender", "M");
mapUser.put("date_created", new Date());
jdbc.insert("users", mapUser);

// insert batch
Collection<Collection<?>> users = 
    Arrays.<Collection<?>>asList(
        Arrays.<Object>asList("104", "Xuir", "F", new Date()), 
        Arrays.<Object>asList("105", "Sorina Nyco", "F", new Date()), 
        Arrays.<Object>asList("106", "Gemily", "F", new Date()), 
        Arrays.<Object>asList("107", "Luffy", "M", new Date()), 
        Arrays.<Object>asList("108", "Zoro", "M", new Date()), 
        Arrays.<Object>asList("109", "Bruck", "M", new Date()));
jdbc.insert("users", 
    Arrays.asList("id", "name", "gender", "date_created"), 
    args, 3);

// update gender to F of user 102
jdbc.update("update users set gender = ? where id = ?", "F", "102");

```

### Reusble Connection And Transaction
```java
// find user of id is 101 and books uses same connection
Map<String, Object> mapUserWithBooks = 
jdbc.execute(new Co<Map<String, Object>>() {
	@Override
    protected Map<String, Object> run() throws SQLException {
        String userId = "101";
        Map<String, Object> mapRtn = findMap(
            "select * from users where id = ?", userId);
        mapRtn.put("books", 
            listMap("select * from books where user_id = ?", 
                userId));
        return mapRtn;
    }
});
	
// execute transaction
Map<String, Object> mapUser = 
jdbc.executeTransaction(new Co<Map<String, Object>>() {
    @Override
    protected Map<String, Object> run() throws SQLException {
        update("delete users where id = ?", "104");
        update("update users set age = ? where id = ?", 24, "103");
        return findMap("select * from users where id = ?", 106);
    }
});

```

### Execute SQL
```java
// create table users
jdbc.execute(
	"create table users (" + 
	"id varchar(36) not null, " + 
	"name varchar(36) not null, " + 
	"primary key (id))");
	
```

### Customize Newbie JDBC
```java
// create table users
CustomResolver customResolver = new CustomResolver() {
    // set StringBuilder as String
    public void setParam(PreparedStatement ps, 
            int index, Object param)
            throws SQLException {
        if (param instanceof StringBuilder) {
            ps.setString(index, param.toString());
        }
        else {
            ps.setObject(index, param);
        }
    }
    
    // read CLOB as String
    public Object readValue(ResultSet rs, int index) 
            throws SQLException {
        Object valueRtn = null;
        if (Types.CLOB == rs.getMetaData().getColumnType(index)) {
            valueRtn = rs.getClob(index).toString();
        }
        else {
            valueRtn = rs.getObject(index);
        }
        return valueRtn;
    }
    
    // use MySQLCompatiblePaginationProcessor to paginate
    public PaginationProcessor getPaginationProcessor(
            Connection conn) {
        return new MySQLCompatiblePaginationProcessor();
    }
};

jdbc = new NewbieJdbcSupport(dataSource, customResolver);
	
```

## Contacts

chyxion@163.com
