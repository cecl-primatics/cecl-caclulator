package com.primatics.ignite.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.primatics.ignite.model.AnalyzedLoan;
import com.primatics.ignite.model.Rates;

@Controller
public class IndexController {

	static Rates updated = new Rates();

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	MongoTemplate mongoTemplate;

	public static AnalyzedLoan loan;

	@GetMapping("/home")
	public String home(Model model) {

		if (loan == null) {
			BigDecimal[] totalLossAmounts = new BigDecimal[16];
			Arrays.fill(totalLossAmounts, new BigDecimal(0.0));
			AnalyzedLoan loanEmpty = new AnalyzedLoan(000, "not started", new BigDecimal(0.0), totalLossAmounts,
					new BigDecimal(0.0));
			Double[] survivals = new Double[16];
			Arrays.fill(survivals, 1.0);
			loanEmpty.setSurvival(survivals);
			Double[] lossRates = new Double[16];
			Arrays.fill(lossRates, 1.0);
			loanEmpty.setLossRate(lossRates);
			model.addAttribute("loanList", loanEmpty);
			model.addAttribute("scenarios", getRunlist());
		} else {
			model.addAttribute("loanList", loan);
			model.addAttribute("scenarios", getRunlist());
		}
		return "home";
	}

	@ResponseBody
	@RequestMapping(value = "/home/analysis", method = { RequestMethod.GET, RequestMethod.POST })
	public AnalyzedLoan home(Model model, @RequestBody(required = false) Rates rates, HttpServletResponse response)
			throws JSONException, IOException {

		if (rates == null) {

			Double[] survivals = new Double[16];
			Arrays.fill(survivals, 1.0);
			updated.setSurvival(survivals);
			Double[] lossRates = new Double[16];
			Arrays.fill(lossRates, 1.0);
			updated.setLossRate(lossRates);
			updated.setIndex(0);
			updated.setScenario("Not Started");
		} else {
			updated.setSurvival(rates.getSurvival());
			updated.setLossRate(rates.getLossRate());
			updated.setIndex(rates.getIndex());

			updated.setScenario(rates.getScenario());
		}
		HashMap requestBody = new HashMap();
		requestBody.put("survival", updated.getSurvival());
		requestBody.put("lossRate", updated.getLossRate());
		requestBody.put("index", updated.getIndex());
		requestBody.put("scenario", updated.getScenario());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String json = new ObjectMapper().writeValueAsString(requestBody);
		HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		ResponseEntity<AnalyzedLoan> l = restTemplate.postForEntity("http://localhost:8082/api/recalculate/", entity,
				AnalyzedLoan.class);

		loan = l.getBody();
		model.addAttribute("loanList", loan);
		model.addAttribute("scenarios", getRunlist());
		return loan;
	}

	@ResponseBody
	@RequestMapping(value = "/home/calculate", method = { RequestMethod.GET, RequestMethod.POST })
	public void calculate(@RequestBody(required = false) String scenario,
			HttpServletResponse response) throws IOException {

		HashMap requestBody = new HashMap();
		requestBody.put("run_name", scenario);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String json = new ObjectMapper().writeValueAsString(requestBody);
		HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		System.out.println("STEP 8 - Calling /home/calculate endpoint");
		Stopwatch imp1 = Stopwatch.createStarted();
		ResponseEntity<AnalyzedLoan> al = restTemplate.postForEntity("http://localhost:8082/api/cache", entity, AnalyzedLoan.class);
		imp1 = imp1.stop();
		
		loan = al.getBody();
	}

	public List<String> getRunlist() {

		DBCursor cur = mongoTemplate.getCollection("fs.files").find();

		List<String> scenarios = new ArrayList<String>();
		Iterator<DBObject> it = cur.iterator();
		while (it.hasNext()) {
			DBObject scen = it.next();
			DBObject scenario = (DBObject) scen.get("metadata");
			System.out.println(scenario.get("run_name").toString());
			scenarios.add((String) scenario.get("run_name"));
		}

		return scenarios;
	}
}