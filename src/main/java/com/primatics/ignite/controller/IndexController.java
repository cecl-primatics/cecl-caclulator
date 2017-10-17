package com.primatics.ignite.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primatics.ignite.model.AnalyzedLoan;
import com.primatics.ignite.model.Rates;

@Controller
public class IndexController {

	static Rates updated = new Rates();

	@Autowired
	RestTemplate restTemplate;

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
		} else {
			model.addAttribute("loanList", loan);
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
		} else {
			updated.setSurvival(rates.getSurvival());
			updated.setLossRate(rates.getLossRate());
		}
		HashMap requestBody = new HashMap();
		requestBody.put("survival", updated.getSurvival());
		requestBody.put("lossRate", updated.getLossRate());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String json = new ObjectMapper().writeValueAsString(requestBody);
		HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		ResponseEntity<AnalyzedLoan> l = restTemplate.postForEntity("http://localhost:8082/api/recalculate/", entity,
				AnalyzedLoan.class);

		AnalyzedLoan al = new AnalyzedLoan();

		al.setKey(l.getBody().getKey());
		al.setPortfolio(l.getBody().getPortfolio());
		al.setTotalBalance(l.getBody().getTotalBalance().setScale(2, RoundingMode.HALF_EVEN));
		al.setTotalLossAmounts(l.getBody().getTotalLossAmounts());

		BigDecimal totalLoss = Arrays.asList(l.getBody().getTotalLossAmounts()).stream().reduce(BigDecimal.ZERO,
				BigDecimal::add);
		al.setTotalLoss(totalLoss.setScale(2, RoundingMode.HALF_EVEN));
		al.setSurvival(updated.getSurvival());
		al.setLossRate(updated.getLossRate());

		System.out.println(
				" ***********************************************************************************************************************************");
		System.out.println(al.getPortfolio());
		System.out.println(Arrays.asList(al.getLossRate()).toString());
		System.out.println(Arrays.asList(al.getSurvival()).toString());
		System.out.println(Arrays.asList(al.getTotalLossAmounts()).toString());
		System.out.println(al.getKey());
		System.out.println(al.getTotalBalance());
		System.out.println(al.getTotalLoss());
		System.out.println(
				" ***********************************************************************************************************************************");

		loan = al;
		model.addAttribute("loanList", al);
		response.sendRedirect("home");
		return loan;
	}
}