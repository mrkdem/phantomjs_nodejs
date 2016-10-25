package com.intive;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Random;

@RestController
@RequestMapping("pdf")
public class PdfController {

    private final Configuration cfg;

    public PdfController() {
        cfg = new Configuration();
        cfg.setClassForTemplateLoading(getClass(), "/templates/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

	@RequestMapping("/generate")
    public void random(Writer responseWriter) throws IOException, TemplateException, URISyntaxException {
		try {
			/* ------------------------------------------------------------------------ */
			/* You usually do these for MULTIPLE TIMES in the application life-cycle:   */

			/* Create a data-model */
			Map data = new HashMap();
			ChartValues values = new ChartValues();
			generateRandomValues(values.getVals());
			data.put("values", values);

			/* Get the template (uses cache internally) */
			Template temp = cfg.getTemplate("test.ftlh");

			/* Merge data-model with template */
//			Writer out = new OutputStreamWriter(System.out);
//			temp.process(data, out);
//			out.flush();

			// Generowanie pliku z html
			Writer file = new FileWriter (new File("test.html"));
			temp.process(data, file);
			file.flush();
			file.close();

			System.out.println("Wygenerowano plik test.html");

			RestTemplate restTemplate = new RestTemplate();
			URI url = new URI("http://localhost:3000/generatehtml");
			RequestCallback cb = clientHttpRequest -> {
				Writer writer = new OutputStreamWriter(clientHttpRequest.getBody());
				try {
					temp.process(data, writer);
				} catch (TemplateException e) {
					System.err.println("Something went terribly wrong.");
					e.printStackTrace();
				}
			};
			final HttpMessageConverterExtractor<String> responseExtractor =
					new HttpMessageConverterExtractor<String>(String.class, restTemplate.getMessageConverters());
			restTemplate.execute(url, HttpMethod.POST, cb, responseExtractor);

		} catch (IOException e) {
			e.printStackTrace();
//		} catch (TemplateException e) {
//			e.printStackTrace();
		}
    }

    @RequestMapping("/resp")
    public void getResp() throws MalformedURLException, URISyntaxException {
        RestTemplate template = new RestTemplate();
        Map model = new HashMap();
        model.put("hi", "I am sad");
        model.put("sad", "I am hi");
        URI url = new URI("http://localhost:3000/generatehtml");
        String response = template.postForObject(url, model, String.class);
        System.out.println(response);
    }

	
	private static void generateRandomValues(List<String> vals) {
		for (int i=0; i<getRandomNumberInRange(); i++) {
			vals.add(Integer.toString(getRandomNumberInRange()));
		}
	}
	
	private static int getRandomNumberInRange() {
        Random r = new Random();
        return r.nextInt((200 - 0) + 1) + 0;
    }
}
