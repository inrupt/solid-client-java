package com.inrupt.client.examples.springboot;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.solid.Metadata;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidResource;
import com.inrupt.client.solid.SolidResourceHandlers;

@RestController
public class SolidController {

	@Value("${storageUri}")
	private String uriString;

	@GetMapping("/")
	public String index() {
		return "Hello World!";
	}

    @GetMapping("/public-container")
	public List<URI> solid(){

		final SolidClient client = SolidClient.getClient();
		final Request request =
                Request.newBuilder().uri(URI.create(uriString)).header("Accept", "text/turtle").GET().build();

		final Response<SolidContainer> response =
                client.send(request, SolidResourceHandlers.ofSolidContainer()).toCompletableFuture().join();
		final SolidContainer container = response.body();

		return container.getContainedResources()
				.map(SolidResource::getIdentifier).toList();
	}
}
