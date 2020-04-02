package com.fire.core;

import com.fire.core.annotation.RequestMapping;
import com.fire.core.annotation.RequestMethod;
import com.fire.core.injection.controller.model.Controller;
import com.fire.core.injection.controller.model.ControllerMethod;
import com.fire.core.injection.controller.model.Path;
import com.fire.core.manager.model.ObjectManagerModel;
import com.fire.core.service.IWolfMapper;
import com.fire.core.transaction.IWolfTransaction;
import com.fire.core.transaction.WolfSqlSessionTemplate;
import com.fire.core.transaction.WolfTransactionTemplate;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.logging.log4j.util.Strings;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.osgi.framework.BundleContext;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * 在父类基础上，增加对web工程的数据库实例的依赖注入、session管理和连接管理,还有controller层的事务控制
 * 
 * @ClassName: AbstractServiceTracker
 * @author Administrator
 * @date 2016年10月18日 下午3:57:59
 */
public abstract class AbstractWebServiceTracker extends AbstractServiceTracker implements IWolfTransaction {

	/**
	 * mybatis配置文件地址
	 * 
	 * @return
	 */
	protected abstract String getMybatisConfigPath();

	/**
	 * TODO 返回需要实例化为web接口的服务列表，后期可以改为通过配置文件扫描
	 * 
	 * @return
	 */
	protected abstract List<Class<?>> regControllerClasses();

	private List<Class<?>> regControllerClasses;

	private final ObjectManagerModel wolfControllMap = new ObjectManagerModel();

	/**
	 * 存放所有web接口，stop时请清理该容器
	 */
	private List<Controller> controllers;

	/**
	 * 事务管理器
	 */
	private DataSourceTransactionManager dstm;

	/**
	 * 数据库会话
	 */
	protected SqlSession session;

	public AbstractWebServiceTracker() {
		String mybatisConfig = getMybatisConfigPath();
		if (null == mybatisConfig || mybatisConfig.length() == 0) {
			return;
		}
		try {
			// 为mybatis注入映射模型，并且打开session,例如："config/Configure.xml"
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			WolfClassLoader loader = WolfClassLoader.getClassLoader(System.getProperty("karaf.home"));
			Thread.currentThread().setContextClassLoader(loader);
			Reader reader = Resources.getResourceAsReader(mybatisConfig);
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			Configuration configuration = sqlSessionFactory.getConfiguration();

			// 重置mybatis默认事务，换成SpringManagedTransactionFactory用于支持spring事务
			Environment env = configuration.getEnvironment();
			Environment environment = new Environment.Builder(env.getId()).dataSource(env.getDataSource())
					.transactionFactory(new SpringManagedTransactionFactory()).build();
			sqlSessionFactory.getConfiguration().setEnvironment(environment);

			// 初始化事务管理器
			dstm = new DataSourceTransactionManager(configuration.getEnvironment().getDataSource());

			// 初始化会话代理
			session = new WolfSqlSessionTemplate(sqlSessionFactory, ExecutorType.REUSE);

			Thread.currentThread().setContextClassLoader(cl);

			// 检查schema是否已经创建好，没有则自动创建schema
			this.createSchemaName();

		} catch (IOException e) {
			LOG.error("{}", e);
		} catch (Exception e) {
			LOG.error("{}", e);
		}
	}

	/**
	 * 手动获取 mybatis映射实现
	 * 
	 * @param c
	 * @return
	 */
	public final <T> T getMapper(Class<T> c) {
		if (null == session) {
			return null;
		}
		return (T) session.getMapper(c);
	}

	/**
	 * 获取当前请求对应的controller方法
	 * 
	 * @param url
	 *            请求地址
	 * @param method
	 *            请求方法 POST GET ...
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public final ControllerMethod getControllerMethod(String url, String method)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		for (Controller con : this.controllers) {
			Path p = con.getPath();
			String rootPath = p.getPath();

			if (url.startsWith(rootPath) || "/".equals(rootPath) || "".equals(rootPath)) {
				Map<Path, ControllerMethod> methods = con.getMethods();
				for (Path key : methods.keySet()) {
						if (url.equals(key.getPath()) || url.startsWith(key.getPath())) {
							if(url.startsWith(key.getPath())) {
								LOG.warn("The method is carried out by fuzzy matching.");
							}
							RequestMethod rm = key.getMethod();
							if (rm.name().equals(method)) {
								ControllerMethod cm = con.getMethods().get(key);
								// 加入事务
								Transactional transactional = cm.getMethod().getAnnotation(Transactional.class);
								if (transactional != null) {
									cm.setTransactionTemplate(new WolfTransactionTemplate(dstm));
								}
	
								return cm;
							}
						}
				}
			}
		}

		return null;
	}

	/**
	 * 更新数据库脚本 主要用于系统升级时使用 业务系统每次升级都指定一个SQL升级脚本，在初始化的时候自己调用该方法来进行升级
	 * 如果没有传入需要执行的脚本文件，那么将只备份数据，不做其它任何处理
	 * 每次update工程都是执行备份。因为需要注意数据量大的时候，备份文件占用过多硬盘空间的问题 FIXME
	 * 
	 * @param backupSqlPath
	 *            备份SQL的文件路径
	 * @param execSqlPaths
	 *            执行SQL的文件路径，这通常是一个相对路径
	 * @return
	 */
	public boolean initOrUpgradeOrBackupSQL(String backupSqlPath, String... execSqlPaths) {
		// 备份SQL数据
		Configuration config = session.getConfiguration();
		Properties prep = config.getVariables();
		String username = prep.getProperty("username");
		String pwd = prep.getProperty("password");
		String port = prep.getProperty("port");
		SqlSession sqlSession = ((WolfSqlSessionTemplate) session).getSqlSessionFactory().openSession();
		try {
			Connection conn = sqlSession.getConnection();
			conn.setAutoCommit(true);
			String schemaName = parseDatabaseUrlConnection(config.getEnvironment().getDataSource(), port);
			try {
				backup(username, pwd, port, schemaName, backupSqlPath);
				try {
					return runScript(conn, execSqlPaths);
				} catch (RuntimeException e) {
					LOG.error("[{}] script exec error : {}", execSqlPaths, e);
					try {
						// 脚本执行出错时，还原已经执行完成脚本文件名称。
						for (int i = 0; i < execSqlPaths.length; i++) {
							File file = new File(execSqlPaths[i] + ".bak");
							if (file.exists()) {
								file.renameTo(new File(execSqlPaths[i]));
							}
						}
						// 脚本执行出错时，直接还原备份SQL
						recover(username, pwd, port, schemaName, backupSqlPath);
						// TODO 后期考虑还原后是否需要删除备份文件，当数据量大时，该文件可能很大，浪费生产环境硬盘容量
					} catch (IOException e1) {
						LOG.error("[{}] recover error : {}", schemaName, e1);
					}
				}
			} catch (IOException e2) {
				LOG.error("[{}] backup error : {}", schemaName, e2);
			}
		} catch (SQLException e2) {
			LOG.error("[{}] sql error : {}", execSqlPaths, e2);
		} finally {
			sqlSession.close();
		}
		return false;
	}

	/**
	 * 获取dbname
	 * 
	 * @return
	 */
	public String getDbName() {
		Properties prep = session.getConfiguration().getVariables();
		String port = prep.getProperty("port");
		DataSource ds = session.getConfiguration().getEnvironment().getDataSource();
		String url = getDatabaseUrl(ds);
		String database = parseDatabaseUrlConnection(url, port);
		return database;
	}

	@Override
	public final WolfTransactionTemplate getWolfTransactionTemplate() {
		if (this instanceof IWolfTransaction) {
			return new WolfTransactionTemplate(dstm);
		}
		return null;
	}

	@Override
	protected final boolean autoIniect(BundleContext context) {
		super.autoIniect(context);
		// 初始化所有controller
		autoInstanceController();
		// 解析controller，理清请求映射关系
		parseController();

		List<Class<?>> list = getRegControllerClasses();

		if (list == null || list.isEmpty()) {
			return true;
		}

		iniect(list, (t -> {
			return wolfControllMap.get(t);
		}), this::findInstanceByFieldType);

		return true;
	}

	@Override
	protected final Object findInstanceByFieldType(Field field) {
		Object instance = super.findInstanceByFieldType(field);

		if (instance == null) {
			Class<?> c = field.getType();
			if (AbstractWebServiceTracker.class.equals(c) || AbstractServiceTracker.class.equals(c)) {
				instance = AbstractWebServiceTracker.this;
			} else {
				// 到mybatis session中找mapper映射实例
				if (getAllInterfaces(c).contains(IWolfMapper.class)) {
					if (null != session) {
						instance = session.getMapper(c);
					}
					if (null == instance) {
						LOG.error("mapper {} 未从容器中获取到相应实例！", c.getSimpleName());
					}
				}
			}
		}
		return instance;
	}

	@Override
	public final void autoClear() {
		super.autoClear();

		wolfControllMap.clear();

		if (null != controllers) {
			controllers.clear();
		}
		controllers = null;

		// 关闭mybatis-spring session无须手动关闭
		try {
			if (null != session) {
				Connection conn = session.getConnection();
				conn.close();
			}
		} catch (SQLException e) {
			LOG.error("[{}] sql connect close error {}", context.getBundle(), e);
		}
	}

	/**
	 * 一次性方法，只在检查schema时使用
	 * 
	 * @param ds
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private Object getDataSource(DataSource ds)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = ds.getClass().getDeclaredField("dataSource");
		field.setAccessible(true);
		return field.get(ds);
	}

	/**
	 * 一次性方法，只在检查schema时使用
	 */
	private void createSchemaName() {
		Properties prep = session.getConfiguration().getVariables();
		String port = prep.getProperty("port");
		DataSource ds = session.getConfiguration().getEnvironment().getDataSource();
		String url = getDatabaseUrl(ds);
		String database = parseDatabaseUrlConnection(url, port);
		Connection conn = null;
		try {
			SqlSession sqlSession = ((WolfSqlSessionTemplate) session).getSqlSessionFactory().openSession();
			conn = sqlSession.getConnection();
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (CannotGetJdbcConnectionException e) {
			// 连接不成功说明还没有创建该数据库，那么需要自动创建该数据库，方便后面执行sql脚本进行升级
			Field urlField = null;
			Object urlO = null;
			Statement smt = null;
			try {
				urlO = getDataSource(ds);
				urlField = urlO.getClass().getDeclaredField("url");
				urlField.setAccessible(true);
				urlField.set(urlO, url.split(port)[0] + port);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e2) {
				e2.printStackTrace();
			}
			try {
				conn = ds.getConnection();
				smt = conn.createStatement();
				smt.executeUpdate("create database `" + database + "`");
				try {
					if(urlField != null)
						urlField.set(urlO, url);
				} catch (IllegalArgumentException | IllegalAccessException e2) {
					e2.printStackTrace();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			} finally {
				try {
					if (conn != null) {
						conn.close();
					}
					if (smt != null) {
						smt.close();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private List<Class<?>> getRegControllerClasses() {
		if (this.regControllerClasses == null) {
			this.regControllerClasses = regControllerClasses();
		}
		return this.regControllerClasses;
	}

	/**
	 * 实例化所有controller，TODO 后期需要换成代理模式，方面管理。建议换成用到时加载
	 */
	private void autoInstanceController() {
		List<Class<?>> list = getRegControllerClasses();
		if (null != list) {
			list.stream().forEach(c -> {
				if (null == wolfControllMap.get(c)) {
					try {
						wolfControllMap.put(c, c.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						LOG.error("{} instance error： {}", c.getSimpleName(), e);
					}
				}
			});
		}
	}

	/**
	 * 解析controller，分解出请求路径和请求方法
	 */
	private void parseController() {
		if (this.controllers != null) {
			// 已经解析过了
			return;
		}

		if (this.wolfControllMap.isEmpty()) {
			autoInstanceController();
		}

		List<Class<?>> list = getRegControllerClasses();

		if (list == null || list.isEmpty()) {
			return;
		}

		this.controllers = new ArrayList<Controller>(list.size());

		Map<String, RequestMethod> map = new HashMap<String, RequestMethod>(list.size() * 5);

		for (Class<?> c : list) {
			RequestMapping rm = c.getAnnotation(RequestMapping.class);
			String rootPath = "";
			if (null != rm) {
				rootPath = rm.value();
				rootPath = rootPath.startsWith("/", 0) ? rootPath : ("/" + rootPath);
			}

			Path rp = new Path(rootPath);
			if (rm != null) {
				rp.setMethod(rm.method());
			}

			Object controller = wolfControllMap.get(c);

			Method[] methods = c.getDeclaredMethods();
			Map<Path, ControllerMethod> cms = new HashMap<Path, ControllerMethod>(6);

			Arrays.asList(methods).stream().filter(m -> {
				RequestMapping rmMethod = m.getAnnotation(RequestMapping.class);
				return null != rmMethod;
			}).forEach(m -> {
				RequestMapping rmMethod = m.getAnnotation(RequestMapping.class);
				String path = rmMethod.value();
				path = rp.getPath() + (path.startsWith("/", 0) ? path : ("/" + path));
				if (map.containsKey(path)) {
					if (rmMethod.method().equals(map.get(path))) {
						LOG.error("存在请求接口冲突,两个url路径一样" + path);
						throw new RuntimeException("存在请求接口冲突,两个url路径一样");
					}
				} else {
					map.put(path, rmMethod.method());
				}
				Path p = new Path(path, rmMethod.method());
				ControllerMethod cm = new ControllerMethod(p, m, controller);
				cm.setRequestMapping(rmMethod);
				if (LOG.isDebugEnabled()) {
					if (null != cms.get(p)) {
						LOG.warn("controller对外接口不只一个方法实现，请检查：{}", p);
					}
				}
				cms.put(p, cm);
			});

			controllers.add(new Controller(rp, cms));
		}
	}

	/**
	 * 执行SQS脚本
	 * 
	 * @param conn
	 * @param execSqlPaths
	 * @return
	 */
	private boolean runScript(Connection conn, String... execSqlPaths) {
		if (execSqlPaths != null && execSqlPaths.length > 0) {
			ScriptRunner runner = new ScriptRunner(conn);
			// 自动提交
			runner.setAutoCommit(true);
			runner.setFullLineDelimiter(false);
			runner.setSendFullScript(false);
			runner.setStopOnError(false);
			try {
				for (int i = 0; i < execSqlPaths.length; i++) {
					File file = new File(execSqlPaths[i]);
					if (file.exists()) {
						FileInputStream fis = new FileInputStream(file);
						InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
						runner.runScript(isr);
						try {
							fis.close();
							isr.close();
							// 重命名sql文件，防止重复执行
							file.renameTo(new File(execSqlPaths[i] + ".bak"));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				LOG.error("[{}] script file encoding error : {}", execSqlPaths, e);
				return false;
			} catch (FileNotFoundException e) {
				LOG.error("[{}] script file not found error : {}", execSqlPaths, e);
				return false;
			} finally {
				runner.closeConnection();
			}
		}
		return true;
	}

	/**
	 * 可以反射拿database名称，也可以通过url截取，该方法通过url截取
	 * 
	 * @param port
	 * @return
	 * @throws SQLException
	 */
	private String parseDatabaseUrlConnection(DataSource ds, String port) {
		String url = getDatabaseUrl(ds);
		return parseDatabaseUrlConnection(url, port);
	}

	/**
	 * 获取配置文件连接数据库使用的url
	 * 
	 * @param ds
	 * @return
	 */
	private String getDatabaseUrl(DataSource ds) {
		try {
			Object o = getDataSource(ds);
			Field f = o.getClass().getDeclaredField("url");
			f.setAccessible(true);
			String url = (String) f.get(o);
			return url;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		return "";
	}

	/**
	 * 可以反射拿database名称，也可以通过url截取，该方法通过url截取
	 * 
	 * @param url
	 * @param port
	 * @return
	 * @throws SQLException
	 */
	private String parseDatabaseUrlConnection(String url, String port) {
		if (Strings.isEmpty(url)) {
			return url;
		}
		String s = "\\?";
		try {
			return url.split(port)[1].split(s)[0].split("/")[1];
		} catch (Exception e) {
			if (url.indexOf(s) > -1) {
				return url.split(port)[1].split(s)[0];
			} else if (url.indexOf(port) > -1) {
				return url.split(port)[1];
			} else {
				return url;
			}
		}
	}

	/**
	 * 备份指定数据库
	 * 
	 * @param username
	 * @param pwd
	 * @param port
	 * @param schemaName
	 * @param path
	 * @throws IOException
	 */
	private void backup(String username, String pwd, String port, String schemaName, String path) throws IOException {
		File file = new File(path);
		if (file.exists()) {
			LOG.warn("[{}] is exists", path);
			file.renameTo(new File(path + System.currentTimeMillis() + ".bak"));
		}
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime
				.exec("mysqldump -u " + username + " -p" + pwd + " -h localhost --port " + port + " -B " + schemaName);
		InputStream inputStream = process.getInputStream();
		InputStreamReader reader = new InputStreamReader(inputStream, "utf-8");
		BufferedReader br = new BufferedReader(reader);
		String s = null;
		StringBuffer sb = new StringBuffer();
		String lineSep = System.getProperty("line.separator");
		while ((s = br.readLine()) != null) {
			sb.append(s);
			sb.append(lineSep);
			if (s.contains(
					"mysqldump: Got error: 1049: Unknown database '" + schemaName + "' when selecting the database")) {
				LOG.warn("[{}]", s);
			}
		}
		s = sb.toString();
		file.getParentFile().mkdirs();
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(s.getBytes());
		fileOutputStream.close();
		br.close();
		reader.close();
		inputStream.close();
	}

	/**
	 * 恢复指定数据库
	 * 
	 * @param username
	 * @param pwd
	 * @param port
	 * @param schemaName
	 * @param path
	 * @throws IOException
	 */
	private void recover(String username, String pwd, String port, String schemaName, String path) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("mysql -u " + username + " -p" + pwd + " -h localhost --port " + port
				+ " --default-character-set=utf8 " + schemaName);
		OutputStream outputStream = process.getOutputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		String str = null;
		String lineSep = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		while ((str = br.readLine()) != null) {
			sb.append(str);
			sb.append(lineSep);
		}
		str = sb.toString();
		OutputStreamWriter writer = new OutputStreamWriter(outputStream, "utf-8");
		writer.write(str);
		writer.flush();
		outputStream.close();
		br.close();
		writer.close();
	}

}
