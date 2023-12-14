package com.example.demo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/documentationController")
public class DocumentController {

	@Cacheable(value="charts",key="#root.args[0]")
	@GetMapping(path = "/charts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getCharts(@PathVariable String id) {
		Resource resource = new ClassPathResource("Statistics-grouped.json");
		String json = "";
		try (InputStream stream = resource.getInputStream()) {
			json = StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		System.out.println("Charts Document Requested.");
		return json;
	}

	@Cacheable(value="documents",key="#root.args[0]")
	@GetMapping(path = "/document/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getDocuments(@PathVariable String id) {
		Resource resource = new ClassPathResource("Statistics-flat.json");
		String json = "";
		try (InputStream stream = resource.getInputStream()) {
			json = StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		System.out.println("Statistics Document Requested.");
		return json;
	}

}
