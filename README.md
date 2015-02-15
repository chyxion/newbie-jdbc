# Base DAO 
* 基础Java JDBC数据库访问，查询、插入
* 更新数据类型支持，Map，JSONObject
* 目前支持数据库方言有Oracle，MySQL，以及SQLServer，
* PreparedStatement封装解析，支持不定参数，集合参数传入
* 数据库连接操作支持
* ResultSet结果集操作支持
* 事务支持

## 依赖包 
* commons-lang
* commons-io
* log4j
* org.json

## 初始化工作 

    // 配置数据库连接，用户名，密码，可以通过如下代码
    // 数据库方言，目前支持，Oracle，MySQL，SQLServer
    DbManager.DIALECT = "oralce";
    // JDBC连接驱动
    DbManager.DRIVER = "oracle.jdbc.OracleDriver";
    // 连接URL
    DbManager.URL = "jdbc:oracle:thin:@chyxion-pad:1521:oracle";
    // 连接用户名
    DbManager.USER_NAME = "chyxion";
    // 连接密码，你的密码
    DbManager.PASSWORD = "secret";
    // 实例化DAO
    BaseDAO dao = new BaseDAO(); 
    // 默认字段名小写，Oracle会将所有数据库表字段大写，此处强制转换为小写
    dao.setLowerCase(true);
## 查询结果数据类型 

    // 查询返回String
    // 返回值为： "Shaun Chyxion"，系列方法有，findInt, findDouble, findObj
    dao.findStr("select name from demo_users where id = ?", 
        // 传入参数可选，可多个
        "110101");

    // 查询返回JSONObject、Map<String, Object>
    // 返回值为： {"id": "110101", "name": "Shaun Chyxion", "gender": "M"}
    dao.findJSONObject("select id, name, gender from demo_users where id = ?", "110101");

    // 返回值为Map<String, Object>: {"id": "110101", "name": "Shaun Chyxion", "gender": "M"}
    dao.findMap("select id, name, gender from demo_users where id = ?", "110101");

    // 查询返回JSONArray, List<Map<String, Object>>
    // 返回值为： [{"id": "110101", "name": "Shaun Chyxion", "gender": "M"}]
    dao.findJSONArray("select id, name, gender from demo_users where id = ?", "110101");

    // 返回值为ListMap<String, Object>>: [{"id": "110101", "name": "Shaun Chyxion", "gender": "M"}]
    dao.findMapList("select id, name, gender from demo_users where id = ?", "110101");

    // 分页查询
    // 返回结果为： [{...}, {...}, {...}]
    dao.findJSONArrayPage(
        "id", // 排序列
        "asc" // 排序方向
        1, // 起始序号
        50, // 页长
        // 查询语句
        "select id, name, gender from demo_users");
    // 返回结果为： [{...}, {...}, {...}]
    dao.findMapListPage(
        "id", // 排序列
        "asc" // 排序方向
        1, // 起始序号
        50, // 页长
        // 查询语句
        "select id, name, gender from demo_users");

## 插入，更新数据类型 

    // 插入JSONObject，Map<String, Object>
    // 创建用户JSONObject
    JSONObject joUser = 
        new JSONObject()
            .put("id", "110102")
            .put("name", "New JSONObject User")
            .put("gender", "M");
    // 插入到数据库
    dao.insert("demo_users", joUser);

    // 创建用户Map
    Map<String, Object> mapUser = new HashMap<String, Object>();
    mapUser.put("id", "110103");
    mapUser.put("name", "New Map User");
    mapUser.put("gender", "F");
    // 插入数据库
    dao.insert("demo_users", mapUser);

    // 可以批量插入，JSONArray，List<Map<String, Object>>，代码类似
    // 略。。。

    // 更新数据
    dao.update("update demo_users set name = ? where id = ?", "Update Name", "110103");

    // 删除数据
    dao.update("delete from demo_users where id = ?", "110103");
    
    // JSONObject 格式更新
    JSONObject joUpdate = 
                new JSONObject()
                .put("name", "Update Name By JSONObject");
    JSONObject joWhere = new JSONObject().put("id", "110104");
    // 执行更新，生成结果为 update demo_users set name = ? where id = ?，"Update Name By JSONObject", "110104"
    dao.update("demo_users", joUpdate, joWhere);

## Prepared Statement 参数支持

    // PreparedStatement 扩展样例 
    // Object[] 作为查询参数
    // 请注意这里是1个 ? 并且有括号()
    dao.findJSONArray("select id, name, gender from demo_users where id in (?)", 
        // 参数集合
        new String[]{"110101", "110102", "110102"});

    // List<Object>集合单数
    List<Object> listP = new LinkedList<Object>();
    listP.add("110101");
    listP.add("110102");
    listP.add("110103");
    // 请注意这里是1个 ? 并且有括号()
    dao.findJSONArray("select id, name, gender from demo_users where id in (?)", 
        // 参数集合
        listP);
    // JSONArray集合参数 
    JSONArray jaP = new JSONArray()
                        .put("110101") 
                        .put("110102") 
                        .put("110103");
    // 请注意这里是1个 ? 并且有括号()
    dao.findJSONArray("select id, name, gender from demo_users where id in (?)", 
        // 参数集合
        jaP);
    // 不定参数
    // 请注意这里是3个?
    dao.findJSONArray("select id, name, gender from demo_users where id in (?, ?, ?)", 
        // 不定单数
        "110101", "110102", "110102");
    // 除了，查询，其调用方式类似，如
    dao.update("delete from demo_users where id in (?)", new String[]{"110102", "110103"});

## 共享连接

    // 同一个业务，无需启动事务时候可以使用共享连接
    // 启动共享连接
    JSONObject joResult = dao.execute(new ConnectionOperator() {
        @Override
        public void run() throws Exception {
            // 查询用户名，请注意，这里使用的是findStr，不是dao.findStr（如果这样，会启动新连接）
            String userName = findStr("select name from demo_users where id = ?", "110101");
            // 这里是同样的处理方式，findJSONArray，不是dao.findJSONArray!!!
            JSONArray jaBooks = findJSONArray("select name, isbn from demo_books");
            // 其他逻辑代码
            // update，insert，。。。。
            // 返回值
            result = new JSONObject()
                .put("user_name", userName)
                .put("books", jaBooks);
        }
    });

## 事务支持

    // 同一个连接，启动事务，异常回滚
    // 启动事务
    JSONObject joResult = dao.executeTransaction(new ConnectionOperator() {
        @Override
        public void run() throws Exception {
            // 查询用户名，请注意，这里使用的是findStr，不是dao.findStr（如果这样，会启动新连接）
            String userName = findStr("select name from demo_users where id = ?", "110101");
            // 这里是同样的处理方式，findJSONArray，不是dao.findJSONArray!!!
            JSONArray jaBooks = findJSONArray("select name, isbn from demo_books");
            // 其他逻辑代码，这里已经处在事务中
            update("delete from demo_users where id in (?)", 
                    new String[]{"110103", "110104", "110105", "110106"});
            update("update demo_users set name = ? where id = ?", "a nice name", "110110");
            // 插入到数据库
            insert("demo_users", 
                        new JSONObject()
                        .put("id", "110102")
                        .put("name", "New JSONObject User")
                        .put("gender", "M"));
            // 返回值
            result = new JSONObject()
                .put("user_name", userName)
                .put("books", jaBooks);
        }
    });

## 操作ResultSet

    // 执行查询，操作ResultSet
    JSONArray jaResult = 
        dao.query(new ResultSetOperator() {
            @Override
            protected void run() throws Exception {
                JSONArray jaResult = new JSONArray();
                while (resultSet.next()) {
                    jaResult.put(resultSet.getString(1));
                }
                result = jaResult;
            }
        }, "select name from demo_users"); // 后面可以传入参数，参见其他findXX方法

    // 上面的方式和findStrList结果类似

## License

* 这是本人工作中积累的一些东西，如果能对这个世界有点作用，就拿去使用吧！
* 许可证， GPL2 
* 有什么需要支持或者帮助或者介绍工作机会请联系 chyxion@163.com
