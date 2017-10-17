package com.primatics.ignite.controller;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheEntry;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.primatics.ignite.model.AnalyzedLoan;

@RestController
@RequestMapping("/cecl")
@CrossOrigin("*")
public class CacheController {
	
	static Ignite ignite = null;

	@GetMapping(value = "/loans/{key}")
	public void getLoan(@PathVariable("key") Integer key) {
		ignite = Ignition.start("classpath:loss-amount-config-client.xml");
		final IgniteCache<Integer, AnalyzedLoan> cache = ignite.getOrCreateCache("loanCacheAnalysis");
		CacheEntry<Integer, AnalyzedLoan> l = cache.getEntry(key);
	}
}
