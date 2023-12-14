package com.example.demo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;

@RestController
@RequestMapping("/api/documentationController")
@PropertySource(value = { "classpath:dfcnames.properties" }, ignoreResourceNotFound = true)
public class DocumentController {

	@Autowired
	private Environment env;
    String keyval = "CM@_2022~F@U!";
	String m_userName = "";
	String m_password = "";
	String m_passwordtest = "";
	String m_docbase = "";
	IDfSessionManager sessionManager = null;
	IDfSession session = null;
	
    public int[] string2Arr(String str) {
        String[] sarr = str.split(",");
        int[] out = new int[sarr.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = Integer.valueOf(sarr[i]);
        }
        return out;
    }

    public String decrypt(int[] input, String key) {
        String output = "";        
        for(int i = 0; i < input.length; i++) {
            output += (char) ((input[i] - 48) ^ (int) key.charAt(i % (key.length() - 1)));
        }
        return output;
    }
    
	public void getDFCProperty() {
		m_userName = env.getProperty("m_userName");
		m_password = decrypt(string2Arr(env.getProperty("m_password")),keyval);
		m_docbase = env.getProperty("m_docbase");
	}

	public void connectToDFC() {
		try {
			getDFCProperty();
			sessionManager = login();
			session = sessionManager.getSession(m_docbase);
		} catch (DfException e) {
			e.printStackTrace();
		}
	}

	public InputStream getChartsJsonFile(String chronicle_id) throws DfException {
		InputStream stream = null;
		String dql = "select * from fas_casejsons where ref_chronicle_id='" + chronicle_id
				+ "' and doc_name in ('Output-grouped-statistics.json')";
		IDfQuery q = null;
		IDfCollection col = null;
		q = new DfQuery();
		q.setDQL(dql);
		IDfDocument statisticsObj = null;
		col = q.execute(session, DfQuery.DF_READ_QUERY);
		while (col.next()) {
			String reportId = col.getString("r_object_id");
			statisticsObj = (IDfDocument) session.getObject(new DfId(reportId));
			stream = statisticsObj.getContent();
		}
		return stream;
	}

	public InputStream getStatisticsJsonFile(String chronicle_id) throws DfException {
		InputStream stream = null;
		String dql = "select * from fas_casejsons where ref_chronicle_id='" + chronicle_id
				+ "' and doc_name in ('Output-flat-statistics.json')";
		IDfQuery q = null;
		IDfCollection col = null;
		q = new DfQuery();
		q.setDQL(dql);
		IDfDocument statisticsObj = null;
		col = q.execute(session, DfQuery.DF_READ_QUERY);
		while (col.next()) {
			String reportId = col.getString("r_object_id");
			statisticsObj = (IDfDocument) session.getObject(new DfId(reportId));
			stream = statisticsObj.getContent();
		}
		return stream;
	}
	
	protected IDfSessionManager login() throws DfException {
		if (m_docbase == null || m_userName == null)
			return null;
		IDfClient dfClient = DfClient.getLocalClient();
		if (dfClient != null) {
			IDfLoginInfo li = new DfLoginInfo();
			li.setUser(m_userName);
			li.setPassword(m_password);
			li.setDomain(null);
			IDfSessionManager sessionMgr = dfClient.newSessionManager();
			sessionMgr.setIdentity(m_docbase, li);
			return sessionMgr;
		} else {
			return null;
		}
	}
	
	@Cacheable(value="charts",key="#root.args[0]", unless="#result.length == 0")
	@GetMapping(path = "/charts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getCharts(@PathVariable String id) throws DfException {
		connectToDFC();
		String json = "";
		try (InputStream stream = getChartsJsonFile(id)) {
			json = StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		return json;
	}
	
	@Cacheable(value="documents",key="#root.args[0]", unless="#result.length == 0")
	@GetMapping(path = "/document/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getDocuments(@PathVariable String id) {
		connectToDFC();
		String json = "";
		try (InputStream stream = getStatisticsJsonFile(id)) {
			json = StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
		} catch (Exception ioe) {
			System.err.println(ioe);
		}
		return json;
	}
}
