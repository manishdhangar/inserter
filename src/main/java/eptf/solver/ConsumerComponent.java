package eptf.solver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerComponent implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ConsumerComponent.class);
	int cpuCount = Runtime.getRuntime().availableProcessors();
	public ConcurrentHashMap<String, ServerEvent> eventsFromFile = new ConcurrentHashMap();
	Connection conn;
	Statement st = null;
	BufferedWriter writer = null;
	String csvName = "";

	public ConcurrentHashMap<String, ServerEvent> getEventsFromFile() {
		return eventsFromFile;
	}

	protected ArrayBlockingQueue<ServerEvent> transferQueue;
	private int dbCounter;

	public ConsumerComponent(ArrayBlockingQueue<ServerEvent> idQueue) {
		this.transferQueue = idQueue;
		// openDbConnection();
		try {
			csvName = "/tmp/" + System.nanoTime() + ".csv";
			writer = new BufferedWriter(new FileWriter(csvName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void process(String id) throws Exception {
		ServerEvent seStarted = eventsFromFile.get(id + "STARTED");
		ServerEvent seFinished = eventsFromFile.get(id + "FINISHED");

		if (seFinished != null && seStarted != null) {
			long abs = Math.abs(seFinished.getTimestamp() - seStarted.getTimestamp());
			if (abs > 4) {
				seStarted.setAlert(true);
				seFinished.setAlert(true);
			}
			writeToFile(seStarted);
			writeToFile(seFinished);
			eventsFromFile.remove(seStarted.getId() + seStarted.getState());
			eventsFromFile.remove(seFinished.getId() + seFinished.getState());

		}
	}

	private void writeToFile(ServerEvent se) throws Exception {
		int al = 0;
		if (se.getAlert() != null)
			al = 1;
		writer.write(se.getId() + ";" + se.getState() + ";" + se.getTimestamp() + ";" + se.getType() + ";" + se.getHost() + ";" + al + "\n");
	}

	@Override
	public void run() {
		try {

			Collection<ServerEvent> c = new ArrayList();
			transferQueue.drainTo(c, 50000);
			for (ServerEvent se : c) {
				eventsFromFile.putIfAbsent(se.getId() + se.getState(), se);
				process(se.getId());
			}
			writer.flush();
			writer.close();
			openDbConnection();
			insert("LOAD DATA local INFILE '" + csvName + "' INTO TABLE test.event FIELDS TERMINATED BY ';' ;");
			conn.commit();
			conn.close();
			FileUtils.deleteQuietly(new File(csvName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setMap(ConcurrentHashMap<String, ServerEvent> ll) {
		this.eventsFromFile = ll;
	}

	public void openDbConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql:" + Fast.dbFileName, "root", "sa");
			st = conn.createStatement();
			st.execute("SET GLOBAL binlog_format = 'ROW';");
			st.execute("SET GLOBAL TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;");
			st.execute("SET GLOBAL concurrent_insert = 2;");
			conn.setAutoCommit(false);
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void insert(String expression) {
		try {
			st.execute(expression);
		} catch (SQLException e) {
			log.error(expression);
			e.printStackTrace();
			System.exit(1);
		}

	}

}
