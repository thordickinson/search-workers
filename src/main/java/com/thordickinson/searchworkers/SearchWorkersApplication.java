package com.thordickinson.searchworkers;

import com.thordickinson.searchworkers.cli.SearchCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

/**
 * Main class.
 */
@SpringBootApplication
public class SearchWorkersApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(SearchWorkersApplication.class, args);
		new CommandLine(new SearchCommand(ctx)).execute(args); //You should see the SearchCommand class
	}
}
