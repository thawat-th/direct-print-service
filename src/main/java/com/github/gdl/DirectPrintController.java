package com.github.gdl;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.validation.constraints.NotNull;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Base64;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = "*")
public class DirectPrintController {
    private static final Logger log = LoggerFactory.getLogger(DirectPrintController.class);

    @GetMapping("/printers")
    @Operation(summary = "Available printers")
    public ResponseEntity printers() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        log.info("Available printers: " + Arrays.asList(services));
        return new ResponseEntity<>(Arrays.stream(services).map(PrintService::getName), HttpStatus.OK);
    }

    @GetMapping("/printer/default")
    @Operation(summary = "Get Default printer")
    public ResponseEntity defaults() {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        return new ResponseEntity<>(service.getName(), HttpStatus.OK);
    }

    @PostMapping(value = "/print")
    @Operation(summary = "Printing raw data")
    public ResponseEntity print(
            @Parameter(description = "Base64 encoded") @RequestParam @NotNull String data) throws IOException, PrinterException {
        byte[] bytes = Base64.getDecoder().decode(data);
        this.printJob(bytes);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping(value = "/print/binary" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Printing binary data")
    public ResponseEntity printBinary(
            @RequestPart("file") MultipartFile file) throws IOException, PrinterException {
        this.printJob(file.getBytes());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping("/print/url")
    @Operation(summary = "Printing form URL")
    public ResponseEntity printUri(
            @RequestParam @NotNull String url) throws IOException, PrinterException {
        URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0");
        byte[] bytes = StreamUtils.copyToByteArray(urlConnection.getInputStream());
        this.printJob(bytes);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    private void printJob(byte[] bytes) throws IOException, PrinterException {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service == null) {
            throw new IllegalStateException("No default print service found.");
        }

        PDDocument doc = PDDocument.load(bytes);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setCopies(1);
        job.setJobName("Direct Print Service");
        job.setPrintService(service);
        job.setPrintable(new PDFPrintable(doc));
        job.print();
    }
}
