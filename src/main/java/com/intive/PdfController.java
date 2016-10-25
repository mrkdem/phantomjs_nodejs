package com.intive;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
    public void generate(HttpServletResponse response) throws IOException, TemplateException, URISyntaxException {
		try {
			Map data = new HashMap();
			ChartValues values = new ChartValues();
			generateRandomValues(values.getVals());
			data.put("values", values);

			Template temp = cfg.getTemplate("test.ftlh");
			// Generowanie pliku z html
/*			Writer file = new FileWriter (new File("test.html"));
			temp.process(data, file);
			file.flush();
			file.close();

			System.out.println("Wygenerowano plik test.html");
*/
			RestTemplate restTemplate = new RestTemplate();
			URI url = new URI("http://localhost:3000/generatehtml");
			ResourceReader reader = new StreamResourceReader(response);
			RequestCallback cb = clientHttpRequest -> {
				Writer writer = new OutputStreamWriter(clientHttpRequest.getBody());
				clientHttpRequest.getHeaders().setContentType(MediaType.TEXT_HTML);
				try {
					temp.process(data, writer);
				} catch (TemplateException e) {
					System.err.println("Something went terribly wrong.");
					e.printStackTrace();
				}
			};
			restTemplate.execute(url, HttpMethod.POST, cb, new StreamResponseExtractor(reader));

		} catch (IOException e) {
			e.printStackTrace();
		}
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
