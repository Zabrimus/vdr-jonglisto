package vdr.jonglisto.lib.svdrp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler implements Runnable {

	private Logger log = LoggerFactory.getLogger(Handler.class);
	
	private final Socket client;

	public Handler(Socket client) {
		this.client = client;
	}

	public void run() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
			
			String input;

			while ((input = in.readLine()) != null) {
				log.debug("Received message from " + Thread.currentThread().getName() + " : " + input);

				switch (input.toLowerCase()) {
				case "help": 
					writer.write("Help requested. Have to be implemented :)");
					writer.newLine();
					writer.flush();
				}
			}
		} catch (Exception e) {
			log.error("Error", e);
		}
	}

}